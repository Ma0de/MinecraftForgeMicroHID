package com.maode.microhid.item;

import com.maode.microhid.MicroHIDMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class RailgunItem extends Item {

    private static final Map<UUID, ChargeData> playerChargeData = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> playerDrawSoundPlayed = new ConcurrentHashMap<>();

    // 时间配置
    private static final int INITIAL_CHARGE_TIME = 100; // 5秒 (20 ticks/秒)
    private static final int DISCHARGE_TIME = 180;      // 9秒
    private static final int COOLDOWN_TIME = 80;        // 4秒
    private static final float MOVEMENT_SPEED_BONUS = 0.3F; // 30%移速加成

    // 移速加成 modifier 的固定UUID
    private static final UUID MOVEMENT_BONUS_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public RailgunItem(Properties properties) {
        super(properties);
    }

    // 明确的状态机
    private enum State {
        IDLE, CHARGING, DISCHARGING, COOLDOWN
    }

    private static class ChargeData {
        State state = State.IDLE;
        int timer = 0;
        InteractionHand hand;

        ChargeData(InteractionHand hand) {
            this.hand = hand;
        }
    }

    // ==== 移速加成 ====
    private static void applyMovementSpeedBonus(Player player, boolean apply) {
        if (player.level().isClientSide()) return;

        var movementAttribute = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (movementAttribute != null) {
            if (apply) {
                if (movementAttribute.getModifier(MOVEMENT_BONUS_UUID) == null) {
                    movementAttribute.addTransientModifier(new AttributeModifier(
                            MOVEMENT_BONUS_UUID, "railgun_movement_bonus", MOVEMENT_SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL
                    ));
                }
            } else {
                movementAttribute.removeModifier(MOVEMENT_BONUS_UUID);
            }
        }
    }

    // ==== 右键使用 ====
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.consume(stack); // 客户端直接消耗，交由服务端处理

        UUID playerId = player.getUUID();
        ChargeData data = playerChargeData.computeIfAbsent(playerId, id -> new ChargeData(hand));
        data.hand = hand;

        if (data.state == State.COOLDOWN) {
            sendActionBarMessage(player, String.format("冷却中... %d秒", data.timer / 20), ChatFormatting.RED);
            return InteractionResultHolder.fail(stack);
        }

        if (data.state == State.IDLE) {
            data.state = State.CHARGING;
            data.timer = INITIAL_CHARGE_TIME;
            
            sendActionBarMessage(player, "开始充能 - 5秒", ChatFormatting.GREEN);
            applyMovementSpeedBonus(player, true);
            playSound(level, player, MicroHIDMod.RAILGUN_WIND_UP_HEAVY.get());
            
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    // ==== Tick事件处理 ====
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 仅在服务端逻辑执行
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();
        Level level = player.level();

        ChargeData data = playerChargeData.get(playerId);
        if (data == null) return;

        // 如果不再持有电磁炮
        if (!isPlayerHoldingRailgun(player, data.hand)) {
            if (data.state != State.IDLE) {
                sendActionBarMessage(player, "物品已切换 - 电磁炮动作取消", ChatFormatting.RED);
                cancelAllActions(player, level, data);
            }
            return;
        }

        boolean isUsingItem = player.isUsingItem() && player.getUsedItemHand() == data.hand;

        switch (data.state) {
            case CHARGING:
                if (!isUsingItem) {
                    handleChargeInterrupted(player, level, data);
                } else {
                    handleCharging(player, level, data);
                }
                break;
            case DISCHARGING:
                handleDischarging(player, level, data);
                break;
            case COOLDOWN:
                handleCooldown(player, level, data, isUsingItem);
                break;
        }
    }

    private static void handleCharging(Player player, Level level, ChargeData data) {
        data.timer--;

        if (data.timer % 20 == 0 && data.timer > 0) {
            int secondsLeft = data.timer / 20;
            sendActionBarMessage(player, String.format("充能中... %d秒", secondsLeft), 
                    secondsLeft > 2 ? ChatFormatting.YELLOW : ChatFormatting.GOLD);
        }

        if (data.timer <= 0) {
            sendActionBarMessage(player, "充能完成！开始放电！", ChatFormatting.GREEN);
            transitionToDischarge(player, level, data);
        }
    }

    private static void handleChargeInterrupted(Player player, Level level, ChargeData data) {
        sendActionBarMessage(player, "充能中断", ChatFormatting.YELLOW);
        // 关键：停止充能音效，播放中断音效
        stopSound(level, player, MicroHIDMod.RAILGUN_WIND_UP_HEAVY.get());
        playSound(level, player, MicroHIDMod.RAILGUN_WIND_UP_BROKEN.get());
        startCooldown(player, level, data);
    }

    private static void transitionToDischarge(Player player, Level level, ChargeData data) {
        data.state = State.DISCHARGING;
        data.timer = DISCHARGE_TIME;
        // 关键：停止充能音效，播放放电音效
        stopSound(level, player, MicroHIDMod.RAILGUN_WIND_UP_HEAVY.get());
        playSound(level, player, MicroHIDMod.RAILGUN_FIRE_HEAVY.get());
        applyMovementSpeedBonus(player, true);
    }

    private static void handleDischarging(Player player, Level level, ChargeData data) {
        data.timer--;
        
        performContinuousDischarge(player, level);

        if (data.timer % 20 == 0 && data.timer > 0) {
            sendActionBarMessage(player, String.format("放电中... %d秒", data.timer / 20), ChatFormatting.LIGHT_PURPLE);
        }

        if (data.timer <= 0) {
            sendActionBarMessage(player, "放电已完成", ChatFormatting.GREEN);
            startCooldown(player, level, data);
        }
    }

    private static void handleCooldown(Player player, Level level, ChargeData data, boolean isUsingItem) {
        data.timer--;

        if (data.timer % 20 == 0 && data.timer > 0) {
            sendActionBarMessage(player, String.format("冷却中... %d秒", data.timer / 20), ChatFormatting.RED);
        }

        if (data.timer <= 0) {
            data.state = State.IDLE;
            sendActionBarMessage(player, "冷却完成", ChatFormatting.GREEN);

            // 如果玩家一直按着右键，冷却完直接开始新一轮充能
            if (isUsingItem && isPlayerHoldingRailgun(player, data.hand)) {
                data.state = State.CHARGING;
                data.timer = INITIAL_CHARGE_TIME;
                sendActionBarMessage(player, "检测到持续按压 - 开始充能", ChatFormatting.GREEN);
                applyMovementSpeedBonus(player, true);
                playSound(level, player, MicroHIDMod.RAILGUN_WIND_UP_HEAVY.get());
                player.startUsingItem(data.hand);
            } else {
                cancelAllActions(player, level, data);
            }
        }
    }

    // ==== 伤害与特效 ====
    private static void performContinuousDischarge(Player player, Level level) {
        double range = 8.0D;
        double damagePerTick = 15.0F; // 每tick伤害

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        HitResult hitResult = player.pick(range, 1.0F, false);
        double actualDistance = range;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            actualDistance = hitResult.getLocation().distanceTo(playerPos);
        }

        spawnBeamEffect(level, player, actualDistance);

        AABB area = new AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        int hitCount = 0;

        for (LivingEntity entity : entities) {
            if (entity == player) continue;

            Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
            Vec3 toEntity = entityPos.subtract(playerPos);

            double distance = toEntity.length();
            if (distance <= range && distance > 0.1) {
                double dotProduct = lookVec.dot(toEntity.normalize());
                // dotProduct > 0.5 表示夹角小于60度
                if (dotProduct > 0.5) {
                    entity.hurt(player.damageSources().playerAttack(player), (float) damagePerTick);

                    // 拉回效果
                    Vec3 pullDirection = playerPos.subtract(entityPos).normalize().scale(0.1D);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(pullDirection.x, -0.1D, pullDirection.z));

                    // 防止被拉得太近
                    if (distance < 2.0D) {
                        Vec3 pushAway = toEntity.normalize().scale(0.1D);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(pushAway.x, 0, pushAway.z));
                    }
                    hitCount++;
                }
            }
        }

        if (hitCount > 0) {
            // 减少刷屏，可以在此时显示命中
        }
    }

    private static void spawnBeamEffect(Level level, Player player, double distance) {
        if (!level.isClientSide()) return;

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 startPos = playerPos.add(lookVec.x * 0.5, lookVec.y * 0.5 - 0.2, lookVec.z * 0.5);
        Vec3 endPos = startPos.add(lookVec.x * distance, lookVec.y * distance, lookVec.z * distance);

        int particleCount = 20;
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / (particleCount - 1);
            Vec3 basePos = startPos.add(endPos.subtract(startPos).scale(progress));

            double offsetX = (Math.random() - 0.5) * 0.5;
            double offsetY = (Math.random() - 0.5) * 0.5;
            double offsetZ = (Math.random() - 0.5) * 0.5;

            Vec3 particlePos = basePos.add(offsetX, offsetY, offsetZ);

            level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                    particlePos.x, particlePos.y, particlePos.z, 0.02, 0.02, 0.02);

            if (i % 2 == 0) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        particlePos.x, particlePos.y, particlePos.z, 0.01, 0.01, 0.01);
            }

            if (Math.random() > 0.7) {
                level.addParticle(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z,
                        (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1, (Math.random() - 0.5) * 0.1);
            }
        }

        level.addParticle(net.minecraft.core.particles.ParticleTypes.FLASH, startPos.x, startPos.y, startPos.z, 0, 0, 0);
        level.addParticle(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE, endPos.x, endPos.y, endPos.z, 0, 0.1, 0);
    }

    // ==== 冷却与清理 ====
    private static void startCooldown(Player player, Level level, ChargeData data) {
        data.state = State.COOLDOWN;
        data.timer = COOLDOWN_TIME;
        applyMovementSpeedBonus(player, false);
        player.stopUsingItem();
    }

    private static void cancelAllActions(Player player, Level level, ChargeData data) {
        // 根据离开时的状态停止对应的音效
        if (data.state == State.CHARGING) {
            stopSound(level, player, MicroHIDMod.RAILGUN_WIND_UP_HEAVY.get());
        } else if (data.state == State.DISCHARGING) {
            stopSound(level, player, MicroHIDMod.RAILGUN_FIRE_HEAVY.get());
        }

        playerChargeData.remove(player.getUUID());
        applyMovementSpeedBonus(player, false);
        player.stopUsingItem();
    }

    // ==== 音频系统重构 ====
    private static void playSound(Level level, Player player, SoundEvent sound) {
        if (level.isClientSide()) return;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // 核心修复：通过向所有玩家发送 StopSound 包，强制停止无法自动停止的持续音效
    private static void stopSound(Level level, Player player, SoundEvent sound) {
        if (level.isClientSide()) return;
        ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(sound.getLocation(), SoundSource.PLAYERS);
        if (level instanceof ServerLevel serverLevel) {
            // 直接使用 broadcastAll 向服务器上所有玩家广播这个停止声音的数据包
            serverLevel.getServer().getPlayerList().broadcastAll(packet);
        }
    }

    // ==== 物品栏与展示 ====
    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (level.isClientSide()) return;

        boolean isSelected = slotIndex == selectedIndex;
        UUID playerId = player.getUUID();

        if (isSelected) {
            ChargeData data = playerChargeData.get(playerId);
            boolean notActive = data == null || data.state == State.IDLE;

            if (!playerDrawSoundPlayed.getOrDefault(playerId, false) && notActive) {
                playSound(level, player, MicroHIDMod.RAILGUN_DRAW.get());
                playerDrawSoundPlayed.put(playerId, true);
            }
        } else {
            playerDrawSoundPlayed.remove(playerId);
            ChargeData data = playerChargeData.get(playerId);
            if (data != null && data.state != State.IDLE) {
                sendActionBarMessage(player, "电磁炮已收起 - 动作取消", ChatFormatting.YELLOW);
                cancelAllActions(player, level, data);
            }
        }
    }

    private static boolean isPlayerHoldingRailgun(Player player, InteractionHand hand) {
        if (hand == null) return false;
        ItemStack stack = player.getItemInHand(hand);
        return !stack.isEmpty() && stack.getItem() instanceof RailgunItem;
    }

    private static void sendActionBarMessage(Player player, String message, ChatFormatting color) {
        if (!player.level().isClientSide()) {
            Component text = Component.literal("[MicroHID] " + message).withStyle(color);
            player.displayClientMessage(text, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        applyMovementSpeedBonus(player, false);
        playerChargeData.remove(player.getUUID());
        playerDrawSoundPlayed.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            applyMovementSpeedBonus(player, false);
            playerChargeData.remove(player.getUUID());
            playerDrawSoundPlayed.remove(player.getUUID());
        }
    }
}
package com.maode.microhid.item;

import com.maode.microhid.MicroHIDMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class RailgunItem extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(RailgunItem.class);
    private static final Map<UUID, ChargeData> playerChargeData = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> playerDrawSoundPlayed = new ConcurrentHashMap<>();

    private static final int INITIAL_CHARGE_TIME = 100; // 5秒
    private static final int DISCHARGE_TIME = 180; // 9秒（9段）
    private static final int COOLDOWN_TIME = 80; // 4秒
    private static final int CHARGE_SEGMENTS = 15; // 按实际分段数量调整
    private static final int DISCHARGE_SEGMENTS = 9; // 按实际分段数量调整

    private static final String SOUND_DRAW = "railgun_draw";
    private static final String SOUND_DISCHARGE_START = "railgun_high_discharge";
    private static final String SOUND_DISCHARGE_COMPLETE = "railgun_high_discharge_complete";

    public RailgunItem(Properties properties) {
        super(properties);
    }

    private static class ChargeData {
        int chargeTimer = 0;
        int dischargeTimer = 0;
        int cooldownTimer = 0;
        boolean isCharging = false;
        boolean isDischarging = false;
        boolean isCooldown = false;
        InteractionHand hand;
        int lastPlayChargeSecond = 0;
        int lastPlayDischargeSecond = 0;
        boolean dischargeStartSoundPlayed = false;
        boolean chargeInterrupted = false;     // 新增：充能阶段被打断标志
        boolean dischargeInterrupted = false;  // 新增：放电阶段被打断标志

        ChargeData(InteractionHand hand) {
            this.hand = hand;
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            UUID playerId = player.getUUID();
            ChargeData data = playerChargeData.get(playerId);

            LOGGER.info("Player {} attempting to use railgun", player.getName().getString());
            sendActionBarMessage(player, "电磁炮启动...", ChatFormatting.YELLOW);

            if (data != null && data.isCooldown) {
                int secondsLeft = data.cooldownTimer / 20;
                LOGGER.info("Railgun is on cooldown, {} seconds left", secondsLeft);
                sendActionBarMessage(player, String.format("冷却中... %d秒", secondsLeft), ChatFormatting.RED);
                return InteractionResultHolder.fail(itemStack);
            }

            if (data != null && (data.isCharging || data.isDischarging)) {
                LOGGER.info("Railgun is already active, usage blocked");
                sendActionBarMessage(player, "电磁炮已在激活状态！", ChatFormatting.RED);
                return InteractionResultHolder.fail(itemStack);
            }

            if (data == null || (!data.isCharging && !data.isDischarging && !data.isCooldown)) {
                data = new ChargeData(hand);
                playerChargeData.put(playerId, data);

                data.isCharging = true;
                data.chargeTimer = INITIAL_CHARGE_TIME;
                data.lastPlayChargeSecond = 0;
                data.lastPlayDischargeSecond = 0;
                data.dischargeStartSoundPlayed = false;
                data.isCooldown = false;
                data.chargeInterrupted = false;
                data.dischargeInterrupted = false;

                LOGGER.info("Starting 5-second charge timer");
                sendActionBarMessage(player, "开始充能 - 5秒", ChatFormatting.GREEN);

                player.startUsingItem(hand);

                return InteractionResultHolder.consume(itemStack);
            }
        }

        return InteractionResultHolder.fail(itemStack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START || event.player.level().isClientSide()) return;

        Player player = event.player;
        UUID playerId = player.getUUID();
        Level level = player.level();

        ChargeData data = playerChargeData.get(playerId);
        if (data == null) return;

        if (!isPlayerHoldingRailgun(player, data.hand)) {
            LOGGER.info("Player switched items, cancelling all actions");
            sendActionBarMessage(player, "物品已切换 - 电磁炮动作取消", ChatFormatting.RED);
            cancelAllActions(player, level, data);
            return;
        }

        if (data.isCooldown) {
            handleCooldownPhase(player, level, data);
            return;
        }

        boolean isUsingRailgun = player.isUsingItem() && player.getUsedItemHand() == data.hand;

        if (!isUsingRailgun) {
            handleReleaseRightClick(player, level, data);
            return;
        }

        if (data.isCharging && !data.isDischarging) {
            handleChargingPhase(player, level, data);
        }

        if (data.isDischarging && !data.isCharging) {
            handleDischargingPhase(player, level, data);
        }
    }

    private static void handleCooldownPhase(Player player, Level level, ChargeData data) {
        data.cooldownTimer--;

        if (data.cooldownTimer % 20 == 0) {
            int secondsLeft = data.cooldownTimer / 20;
            LOGGER.debug("Cooldown timer: {} ticks ({} seconds)", data.cooldownTimer, secondsLeft);
            sendActionBarMessage(player, String.format("冷却中... %d秒", secondsLeft),
                    secondsLeft > 2 ? ChatFormatting.YELLOW : ChatFormatting.GOLD);
        }

        if (data.cooldownTimer <= 0) {
            LOGGER.info("4-second cooldown completed");
            sendActionBarMessage(player, "冷却完成", ChatFormatting.GREEN);
            data.isCooldown = false;

            boolean isUsingRailgun = player.isUsingItem() && player.getUsedItemHand() == data.hand;
            if (isUsingRailgun && isPlayerHoldingRailgun(player, data.hand)) {
                LOGGER.info("Player still holding right click, starting new charge");
                sendActionBarMessage(player, "检测到持续按压 - 开始充能", ChatFormatting.GREEN);
                data.isCharging = true;
                data.chargeTimer = INITIAL_CHARGE_TIME;
                data.lastPlayChargeSecond = 0;
                data.lastPlayDischargeSecond = 0;
                data.chargeInterrupted = false;
                data.dischargeInterrupted = false;
            } else {
                cancelAllActions(player, level, data);
            }
        }
    }

    private static void handleChargingPhase(Player player, Level level, ChargeData data) {
        if (data.chargeInterrupted) return; // 被中断则不再播放
        data.chargeTimer--;

        int chargeSecond = (INITIAL_CHARGE_TIME - data.chargeTimer - 1) / 20 + 1;
        if (chargeSecond != data.lastPlayChargeSecond && data.chargeTimer >= 0 && chargeSecond <= CHARGE_SEGMENTS) {
            String segSound = "railgun_high_charge_" + chargeSecond;
            playSegmentedSound(level, player, segSound);
            LOGGER.info("Playing segmented charge sound: {}", segSound);
            data.lastPlayChargeSecond = chargeSecond;
        }

        if (data.chargeTimer % 20 == 0 && data.chargeTimer > 0) {
            int secondsLeft = data.chargeTimer / 20;
            LOGGER.debug("Charge timer: {} ticks ({} seconds)", data.chargeTimer, secondsLeft);
            sendActionBarMessage(player, String.format("充能中... %d秒", secondsLeft),
                    secondsLeft > 2 ? ChatFormatting.YELLOW : ChatFormatting.GOLD);
        }

        if (data.chargeTimer <= 0) {
            LOGGER.info("5-second charge completed, starting discharge");
            sendActionBarMessage(player, "充能完成！开始放电！", ChatFormatting.GREEN);

            transitionToDischargePhase(player, level, data);
        }

        if (data.isCharging && data.chargeTimer > 0) {
            if (data.isDischarging) {
                LOGGER.error("INCONSISTENT STATE: Charging but also discharging! Fixing...");
                data.isDischarging = false;
                sendActionBarMessage(player, "状态错误修复: 强制取消放电", ChatFormatting.RED);
            }
        }
    }

    private static void handleDischargingPhase(Player player, Level level, ChargeData data) {
        if (data.dischargeInterrupted) return; // 被中断则不再播放

        if (!data.dischargeStartSoundPlayed) {
            playSoundWithCheck(level, player, SOUND_DISCHARGE_START, data.hand);
            data.dischargeStartSoundPlayed = true;
            LOGGER.info("Playing discharge start sound");
            sendActionBarMessage(player, "放电声音播放", ChatFormatting.AQUA);
        }

        data.dischargeTimer--;

        int dischargeSecond = (DISCHARGE_TIME - data.dischargeTimer - 1) / 20 + 1;
        if (dischargeSecond != data.lastPlayDischargeSecond && data.dischargeTimer >= 0 && dischargeSecond <= DISCHARGE_SEGMENTS) {
            String segSound = "railgun_high_discharge_" + dischargeSecond;
            playSegmentedSound(level, player, segSound);
            LOGGER.info("Playing segmented discharge sound: {}", segSound);
            data.lastPlayDischargeSecond = dischargeSecond;
        }

        if (data.dischargeTimer % 20 == 0) {
            int secondsLeft = data.dischargeTimer / 20;
            LOGGER.debug("Discharge timer: {} ticks ({} seconds)", data.dischargeTimer, secondsLeft);
            sendActionBarMessage(player, String.format("放电中... %d秒", secondsLeft),
                    secondsLeft > 3 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE);
        }

        if (data.dischargeTimer % 20 == 0 && data.dischargeTimer > 0) {
            performDischargeEffect(player, level);
        }

        if (data.dischargeTimer <= 0) {
            LOGGER.info("9-second discharge completed");
            sendActionBarMessage(player, "放电完成", ChatFormatting.GREEN);
            completeDischarge(player, level, data);
        }
    }

    private static void transitionToDischargePhase(Player player, Level level, ChargeData data) {
        if (!isPlayerHoldingRailgun(player, data.hand)) {
            LOGGER.info("Player switched items during charge-discharge transition, cancelling");
            sendActionBarMessage(player, "物品已切换 - 充能中断", ChatFormatting.RED);
            cancelAllActions(player, level, data);
            return;
        }

        if (data.chargeTimer > 0) {
            LOGGER.error("INVALID TRANSITION: Attempted to transition to discharge while chargeTimer > 0: {}", data.chargeTimer);
            sendActionBarMessage(player, "错误: 充能未完成！", ChatFormatting.RED);
            return;
        }

        data.isCharging = false;
        data.isDischarging = true;
        data.dischargeTimer = DISCHARGE_TIME;
        data.dischargeStartSoundPlayed = false;
        data.lastPlayDischargeSecond = 0;
        data.dischargeInterrupted = false;
    }

    private static void handleReleaseRightClick(Player player, Level level, ChargeData data) {
        if (data.isDischarging) {
            LOGGER.info("Player released right click during discharge, completing discharge");
            sendActionBarMessage(player, "右键释放 - 放电完成", ChatFormatting.YELLOW);
            // 新增：标记被中断
            data.dischargeInterrupted = true;
            completeDischarge(player, level, data);
        } else if (data.isCharging) {
            LOGGER.info("Player released right click during charge, interrupting");
            sendActionBarMessage(player, "充能中断", ChatFormatting.YELLOW);
            data.chargeInterrupted = true;
            interruptCharge(player, level, data);
        }
    }

    private static void interruptCharge(Player player, Level level, ChargeData data) {
        LOGGER.info("Charge interrupted");
        sendActionBarMessage(player, "充能已中断", ChatFormatting.RED);
        startCooldown(player, level, data);
    }

    private static void completeDischarge(Player player, Level level, ChargeData data) {
        LOGGER.info("Discharge completed");
        sendActionBarMessage(player, "放电已完成", ChatFormatting.GREEN);
        playSoundWithCheck(level, player, SOUND_DISCHARGE_COMPLETE, data.hand);
        startCooldown(player, level, data);
    }

    private static void startCooldown(Player player, Level level, ChargeData data) {
        LOGGER.info("Starting 4-second cooldown");
        sendActionBarMessage(player, "开始4秒冷却", ChatFormatting.YELLOW);

        data.isCharging = false;
        data.isDischarging = false;
        data.isCooldown = true;
        data.cooldownTimer = COOLDOWN_TIME;
        data.lastPlayChargeSecond = 0;
        data.lastPlayDischargeSecond = 0;
        data.dischargeStartSoundPlayed = false;
        data.chargeInterrupted = false;
        data.dischargeInterrupted = false;

        player.stopUsingItem();
    }

    private static void cancelAllActions(Player player, Level level, ChargeData data) {
        UUID playerId = player.getUUID();
        playerChargeData.remove(playerId);
        resetDrawSoundState(playerId);
        player.stopUsingItem();
        LOGGER.info("All railgun actions cancelled for player {}", player.getName().getString());
    }

    private static void performDischargeEffect(Player player, Level level) {
        if (!isPlayerHoldingRailgun(player, player.getUsedItemHand())) {
            LOGGER.info("Player not holding railgun during discharge effect, cancelling");
            return;
        }

        double range = 8.0D;
        double damage = 100.0F;

        net.minecraft.world.phys.Vec3 lookVec = player.getLookAngle();
        net.minecraft.world.phys.Vec3 playerPos = player.position();

        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
                playerPos.x - range, playerPos.y - range, playerPos.z - range,
                playerPos.x + range, playerPos.y + range, playerPos.z + range
        );

        java.util.List<net.minecraft.world.entity.Entity> entities = level.getEntities(player, area);
        LOGGER.debug("Discharge effect: found {} entities in range", entities.size());

        int hitCount = 0;
        for (net.minecraft.world.entity.Entity entity : entities) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity && entity != player) {
                net.minecraft.world.phys.Vec3 entityPos = entity.position();
                net.minecraft.world.phys.Vec3 toEntity = entityPos.subtract(playerPos);

                double distance = toEntity.length();
                double dotProduct = lookVec.dot(toEntity.normalize());

                if (distance <= range && dotProduct > 0.7) {
                    entity.hurt(player.damageSources().playerAttack(player), (float) damage);

                    net.minecraft.world.phys.Vec3 knockback = toEntity.normalize().scale(1.0D);
                    entity.setDeltaMovement(knockback.x, 0.5D, knockback.z);

                    hitCount++;
                    LOGGER.info("Dealt {} damage to entity {}", damage, entity.getName().getString());
                }
            }
        }

        if (hitCount > 0) {
            sendActionBarMessage(player, String.format("放电命中: %d 个目标", hitCount), ChatFormatting.LIGHT_PURPLE);
        }
    }

    // ==== 音频管理 ====
    private static void playSoundWithCheck(Level level, Player player, String soundName, InteractionHand hand) {
        SoundEvent soundEvent = null;
        switch (soundName) {
            case SOUND_DRAW: soundEvent = MicroHIDMod.RAILGUN_DRAW.get(); break;
            case SOUND_DISCHARGE_START: soundEvent = MicroHIDMod.RAILGUN_HIGH_DISCHARGE.get(); break;
            case SOUND_DISCHARGE_COMPLETE: soundEvent = MicroHIDMod.RAILGUN_HIGH_DISCHARGE_COMPLETE.get(); break;
            default:
                // 不处理分段音效
                break;
        }
        LOGGER.info("soundEvent for {} is: {}", soundName, soundEvent);
        if (soundEvent != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            LOGGER.warn("Sound {} not found!", soundName);
        }
    }

    // 新增：分段音效播放（充能和放电共用）
    private static void playSegmentedSound(Level level, Player player, String segSoundName) {
        ResourceLocation soundLoc = new ResourceLocation("micro_hid", segSoundName);
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(soundLoc);
        LOGGER.info("segmented soundEvent for {} is: {}", segSoundName, soundEvent);
        if (soundEvent != null) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else {
            LOGGER.warn("Segmented Sound {} not found!", segSoundName);
        }
    }

    @Override
    public void onInventoryTick(ItemStack stack, Level level, Player player, int slotIndex, int selectedIndex) {
        if (!level.isClientSide()) {
            UUID playerId = player.getUUID();

            boolean isSelected = slotIndex == player.getInventory().selected;

            if (isSelected) {
                ChargeData chargeData = playerChargeData.get(playerId);
                boolean notActive = chargeData == null || (!chargeData.isCharging && !chargeData.isDischarging && !chargeData.isCooldown);

                if (!playerDrawSoundPlayed.getOrDefault(playerId, false) && notActive) {
                    LOGGER.info("Player drew railgun - playing draw sound");
                    playSoundWithCheck(level, player, SOUND_DRAW, InteractionHand.MAIN_HAND);
                    playerDrawSoundPlayed.put(playerId, true);
                }
            } else {
                resetDrawSoundState(playerId);

                ChargeData chargeData = playerChargeData.get(playerId);
                if (chargeData != null) {
                    LOGGER.info("Player no longer holding railgun in selected slot, cancelling actions");
                    sendActionBarMessage(player, "电磁炮已收起 - 动作取消", ChatFormatting.YELLOW);
                    cancelAllActions(player, level, chargeData);
                }
            }
        }
    }

    private static void resetDrawSoundState(UUID playerId) {
        playerDrawSoundPlayed.put(playerId, false);
    }

    private static boolean isPlayerHoldingRailgun(Player player, InteractionHand hand) {
        if (hand == null) return false;
        ItemStack stack = player.getItemInHand(hand);
        return stack != null && stack.getItem() instanceof RailgunItem;
    }

    private static void sendActionBarMessage(Player player, String message, ChatFormatting color) {
        if (!player.level().isClientSide()) {
            Component text = Component.literal("[电磁炮] " + message).withStyle(color);
            player.displayClientMessage(text, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        playerChargeData.remove(playerId);
        playerDrawSoundPlayed.remove(playerId);
        LOGGER.info("Player logged out, cleaned railgun data");
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            UUID playerId = player.getUUID();
            playerChargeData.remove(playerId);
            playerDrawSoundPlayed.remove(playerId);
            LOGGER.info("Player died, cleaned railgun data");
        }
    }
}
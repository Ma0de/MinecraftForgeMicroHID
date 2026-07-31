package com.maode.microhid.item;

import com.maode.microhid.item.controller.HIDController;
import com.maode.microhid.item.data.HIDData;
import com.maode.microhid.network.HIDInputCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MicroHIDItem extends Item {
    public MicroHIDItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.micro_hid.micro_hid.desc"));

        // 电量显示（如果还没初始化过，显示 100%，避免在箱子里看是 0%）
        int charge = stack.getOrCreateTag().contains("Charge") ? HIDData.getCharge(stack) : 100;
        ChatFormatting color = charge > 60 ? ChatFormatting.GREEN
                : charge > 30 ? ChatFormatting.YELLOW
                : charge > 10 ? ChatFormatting.RED
                : ChatFormatting.DARK_RED;
        tooltip.add(Component.literal("电量: " + charge + "%").withStyle(color));

        // 损坏状态提示
        if (HIDData.isBroken(stack)) {
            tooltip.add(Component.literal("⚠ 已损坏").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;

        // ============================================
        // 核心：新物品默认初始化（只执行一次，NBT 终身跟随）
        // ============================================
        if (!stack.getOrCreateTag().contains("Charge")) {
            HIDData.setCharge(stack, 100);
            HIDData.setBroken(stack, false);
        }

        // 只有手持时才执行射击逻辑
        if (!isSelected) return;

        boolean leftDown = HIDInputCache.isLeftDown(player.getUUID());
        boolean rightDown = HIDInputCache.isRightDown(player.getUUID());
        HIDController.tick(player, stack, leftDown, rightDown);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
}
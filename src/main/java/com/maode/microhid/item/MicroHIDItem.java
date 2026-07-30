package com.maode.microhid.item;

import com.maode.microhid.MicroHIDMod;
import com.maode.microhid.item.controller.HIDController;
import com.maode.microhid.item.data.HIDData;
import com.maode.microhid.network.HIDInputCache;
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

    if (isSelected) {
        // 测试阶段：电量为 0 或没有 Charge 标签时自动回满
        if (!stack.getOrCreateTag().contains("Charge") || HIDData.getCharge(stack) <= 0) {
            HIDData.setCharge(stack, 100);
            HIDData.setBroken(stack, false); // 顺便重置损坏状态
        }

        boolean leftDown = HIDInputCache.isLeftDown(player.getUUID());
        boolean rightDown = HIDInputCache.isRightDown(player.getUUID());
        
        MicroHIDMod.LOGGER.info("[HID Item] Tick | left={} right={} | state={} | charge={}", 
            leftDown, rightDown, HIDData.getState(stack), HIDData.getCharge(stack));
        
        HIDController.tick(player, stack, leftDown, rightDown);
    }
}

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }
}
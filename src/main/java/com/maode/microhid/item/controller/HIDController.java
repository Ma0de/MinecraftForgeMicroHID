package com.maode.microhid.item.controller;

import com.maode.microhid.init.ModSounds;
import com.maode.microhid.item.data.HIDData;
import com.maode.microhid.item.data.HIDState;
import com.maode.microhid.item.target.HIDTargeting;
import com.maode.microhid.sound.HIDAudioManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class HIDController {
    private static final UUID SLOW_UUID = UUID.fromString("c1b2c3d4-e5f6-7890-abcd-ef1234567890");

    public static void tick(Player player, ItemStack stack, boolean leftDown, boolean rightDown) {
        if (player.level().isClientSide()) return;

        HIDState state = HIDData.getState(stack);
        int timer = HIDData.getTimer(stack);
        int charge = HIDData.getCharge(stack);
        boolean broken = HIDData.isBroken(stack);

        applySlowdown(player, state != HIDState.IDLE && state != HIDState.COOLDOWN);

        switch (state) {
            case IDLE -> handleIdle(player, stack, leftDown, rightDown, broken, charge);
            case PRIMARY_WINDUP -> handlePrimaryWindup(player, stack, leftDown, timer);
            case PRIMARY_FIRING -> handlePrimaryFiring(player, stack, leftDown, charge, broken);
            case CHARGE_WINDUP -> handleChargeWindup(player, stack, rightDown, timer, charge);
            case CHARGE_HOLDING -> handleChargeHolding(player, stack, leftDown, rightDown, HIDData.getHoldTimer(stack), charge);
            case CHARGE_FIRING -> handleChargeFiring(player, stack, rightDown, charge);
            case OVERCHARGE_EXPLODING -> handleOvercharge(player, stack);
            case COOLDOWN -> handleCooldown(player, stack, timer);
        }
    }

    private static void handleIdle(Player player, ItemStack stack, boolean leftDown, boolean rightDown, boolean broken, int charge) {
        if (charge <= 0) return;
        if (leftDown) {
            HIDAudioManager.onStateChange(player.level(), player, HIDState.PRIMARY_WINDUP);
            if (broken) HIDAudioManager.play(player.level(), player, ModSounds.WIND_UP_BROKEN);
            HIDData.setState(stack, HIDState.PRIMARY_WINDUP);
            HIDData.setTimer(stack, 60);
        } else if (rightDown && !broken) {
            HIDAudioManager.onStateChange(player.level(), player, HIDState.CHARGE_WINDUP);
            HIDData.setState(stack, HIDState.CHARGE_WINDUP);
            HIDData.setTimer(stack, 140);
        }
    }

    private static void handlePrimaryWindup(Player player, ItemStack stack, boolean leftDown, int timer) {
        if (!leftDown) { transitionToCooldown(player, stack, 20); return; }
        if (timer > 0) HIDData.setTimer(stack, timer - 1);
        if (timer <= 0) {
            HIDAudioManager.onStateChange(player.level(), player, HIDState.PRIMARY_FIRING);
            HIDData.setState(stack, HIDState.PRIMARY_FIRING);
        }
    }

    private static void handlePrimaryFiring(Player player, ItemStack stack, boolean leftDown, int charge, boolean broken) {
        if (!leftDown || charge <= 0) { transitionToCooldown(player, stack, 20); return; }
        if (broken) HIDTargeting.fireBroken(player.level(), player);
        else HIDTargeting.firePrimary(player.level(), player);
        if (player.tickCount % 4 == 0) HIDData.setCharge(stack, charge - 1); // 5%/s
    }

    private static void handleChargeWindup(Player player, ItemStack stack, boolean rightDown, int timer, int charge) {
        if (!rightDown || charge <= 0) { transitionToCooldown(player, stack, 140); return; }
        if (timer > 0) HIDData.setTimer(stack, timer - 1);
        if (timer % 13 == 0) HIDData.setCharge(stack, charge - 1); // 1.5%/s
        if (timer <= 0) {
            HIDData.setState(stack, HIDState.CHARGE_HOLDING);
            HIDData.setHoldTimer(stack, 0);
            player.displayClientMessage(Component.literal("[MicroHID] 充能完成！按左键发射！").withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static void handleChargeHolding(Player player, ItemStack stack, boolean leftDown, boolean rightDown, int holdTimer, int charge) {
        if (!rightDown || charge <= 0) { transitionToCooldown(player, stack, 140); return; }
        if (leftDown && charge >= 10) {
            HIDAudioManager.onStateChange(player.level(), player, HIDState.CHARGE_FIRING);
            HIDData.setState(stack, HIDState.CHARGE_FIRING);
            HIDData.setCharge(stack, charge - 10);
            return;
        }
        HIDData.setHoldTimer(stack, holdTimer + 1);
        if (holdTimer > 0 && holdTimer % 40 == 0) HIDData.setCharge(stack, charge - 1); // 0.5%/s
        if (holdTimer > 160) {
            HIDAudioManager.onStateChange(player.level(), player, HIDState.OVERCHARGE_EXPLODING);
            HIDData.setState(stack, HIDState.OVERCHARGE_EXPLODING);
        }
    }

    private static void handleChargeFiring(Player player, ItemStack stack, boolean rightDown, int charge) {
        if (!rightDown || charge <= 0) { transitionToCooldown(player, stack, 140); return; }
        HIDTargeting.fireHeavy(player.level(), player);
        if (player.tickCount % 2 == 0) HIDData.setCharge(stack, charge - 1); // 10%/s
    }

    private static void handleOvercharge(Player player, ItemStack stack) {
        player.hurt(player.damageSources().generic(), 125.0F);
        HIDData.setBroken(stack, true);
        HIDData.setCharge(stack, HIDData.getCharge(stack) - 25);
        transitionToCooldown(player, stack, 140);
    }

    private static void transitionToCooldown(Player player, ItemStack stack, int duration) {
        HIDState previousState = HIDData.getState(stack);
        if (previousState == HIDState.PRIMARY_FIRING) {
            if (HIDData.isBroken(stack)) HIDAudioManager.play(player.level(), player, ModSounds.STOP_FIRING_BROKEN);
            else HIDAudioManager.play(player.level(), player, ModSounds.STOP_FIRING_LIGHT);
        } else if (previousState == HIDState.CHARGE_FIRING) {
            HIDAudioManager.play(player.level(), player, ModSounds.STOP_FIRING_HEAVY);
        }
        HIDAudioManager.onStateChange(player.level(), player, HIDState.COOLDOWN);
        HIDData.setState(stack, HIDState.COOLDOWN);
        HIDData.setTimer(stack, duration);
    }

    private static void handleCooldown(Player player, ItemStack stack, int timer) {
        if (timer > 0) HIDData.setTimer(stack, timer - 1);
        if (timer <= 0) {
            HIDData.setState(stack, HIDState.IDLE);
            player.displayClientMessage(Component.literal("[MicroHID] 准备就绪").withStyle(ChatFormatting.GREEN), true);
        }
    }

    private static void applySlowdown(Player player, boolean active) {
        var attr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            if (active) {
                if (attr.getModifier(SLOW_UUID) == null) {
                    attr.addTransientModifier(new AttributeModifier(SLOW_UUID, "HID slowdown", -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else {
                attr.removeModifier(SLOW_UUID);
            }
        }
    }
}
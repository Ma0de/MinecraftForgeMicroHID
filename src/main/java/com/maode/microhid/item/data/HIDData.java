package com.maode.microhid.item.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class HIDData {
    private static final String KEY_STATE = "State";
    private static final String KEY_TIMER = "Timer";
    private static final String KEY_HOLD_TIMER = "HoldTimer";
    private static final String KEY_CHARGE = "Charge";
    private static final String KEY_BROKEN = "Broken";

    public static HIDState getState(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.contains(KEY_STATE) ? HIDState.valueOf(tag.getString(KEY_STATE)) : HIDState.IDLE;
    }

    public static void setState(ItemStack stack, HIDState state) {
        stack.getOrCreateTag().putString(KEY_STATE, state.name());
    }

    public static int getTimer(ItemStack stack) { return stack.getOrCreateTag().getInt(KEY_TIMER); }
    public static void setTimer(ItemStack stack, int timer) { stack.getOrCreateTag().putInt(KEY_TIMER, timer); }

    public static int getHoldTimer(ItemStack stack) { return stack.getOrCreateTag().getInt(KEY_HOLD_TIMER); }
    public static void setHoldTimer(ItemStack stack, int timer) { stack.getOrCreateTag().putInt(KEY_HOLD_TIMER, timer); }

    public static int getCharge(ItemStack stack) { return stack.getOrCreateTag().getInt(KEY_CHARGE); }
    public static void setCharge(ItemStack stack, int charge) {
        stack.getOrCreateTag().putInt(KEY_CHARGE, Math.max(0, Math.min(100, charge)));
    }

    public static boolean isBroken(ItemStack stack) { return stack.getOrCreateTag().getBoolean(KEY_BROKEN); }
    public static void setBroken(ItemStack stack, boolean broken) { stack.getOrCreateTag().putBoolean(KEY_BROKEN, broken); }
}
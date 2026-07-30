package com.maode.microhid.config;

import com.maode.microhid.MicroHIDMod;
import net.minecraftforge.common.ForgeConfigSpec;

public class MicroHIDConfig {
    public static final String MOD_ID = MicroHIDMod.MOD_ID; // ← 加这行，否则 ClientEventHandler 注册失败
    
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("general");
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
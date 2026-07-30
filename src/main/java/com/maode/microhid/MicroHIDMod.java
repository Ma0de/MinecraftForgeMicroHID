package com.maode.microhid;

import com.maode.microhid.config.MicroHIDConfig;
import com.maode.microhid.init.ModItems;
import com.maode.microhid.init.ModSounds;
import com.maode.microhid.network.NetworkHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(MicroHIDMod.MOD_ID)
public class MicroHIDMod {
    public static final String MOD_ID = "micro_hid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MicroHIDMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // 注册注册器
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        // 注册网络
        NetworkHandler.register();

        // 注册配置
        context.registerConfig(ModConfig.Type.COMMON, MicroHIDConfig.SPEC);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
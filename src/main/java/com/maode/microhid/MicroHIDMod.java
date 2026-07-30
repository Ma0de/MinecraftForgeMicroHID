package com.maode.microhid;

import com.maode.microhid.item.Moditems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(MicroHIDMod.MOD_ID)
public class MicroHIDMod {
    public static final String MOD_ID = "micro_hid";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    // 注册新的音效
    public static final RegistryObject<SoundEvent> RAILGUN_DRAW = registerSoundEvent("railgun_draw");
    public static final RegistryObject<SoundEvent> RAILGUN_WIND_UP_HEAVY = registerSoundEvent("railgun_wind_up_heavy");
    public static final RegistryObject<SoundEvent> RAILGUN_WIND_UP_LIGHT = registerSoundEvent("railgun_wind_up_light");
    public static final RegistryObject<SoundEvent> RAILGUN_WIND_UP_BROKEN = registerSoundEvent("railgun_wind_up_broken");
    public static final RegistryObject<SoundEvent> RAILGUN_FIRE_HEAVY = registerSoundEvent("railgun_fire_heavy");
    public static final RegistryObject<SoundEvent> RAILGUN_FIRE_LIGHT = registerSoundEvent("railgun_fire_light");
    public static final RegistryObject<SoundEvent> RAILGUN_INSPECT = registerSoundEvent("railgun_inspect");
    public static final RegistryObject<SoundEvent> RAILGUN_OVERCHARGE = registerSoundEvent("railgun_overcharge");

    public MicroHIDMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        Moditems.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, name)));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(Moditems.MICRO_HID);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
package com.maode.microhid;

import com.maode.microhid.item.Moditems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.CreativeModeTabRegistry;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MicroHIDMod.MOD_ID)
public class MicroHIDMod
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "micro_hid";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();


    public MicroHIDMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        Moditems.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        SOUND_EVENTS.register(modEventBus);
        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);


    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_1 = registerSoundEvent("railgun_high_discharge_1");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_2 = registerSoundEvent("railgun_high_discharge_2");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_3 = registerSoundEvent("railgun_high_discharge_3");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_4 = registerSoundEvent("railgun_high_discharge_4");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_5 = registerSoundEvent("railgun_high_discharge_5");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_6 = registerSoundEvent("railgun_high_discharge_6");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_7 = registerSoundEvent("railgun_high_discharge_7");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_8 = registerSoundEvent("railgun_high_discharge_8");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_9 = registerSoundEvent("railgun_high_discharge_9");
    // 充能分段音效注册
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE_1 = registerSoundEvent("railgun_high_charge_1");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE_2 = registerSoundEvent("railgun_high_charge_2");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE_3 = registerSoundEvent("railgun_high_charge_3");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE_4 = registerSoundEvent("railgun_high_charge_4");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE_5 = registerSoundEvent("railgun_high_charge_5");
    public static final RegistryObject<SoundEvent> RAILGUN_DRAW = registerSoundEvent("railgun_draw");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE = registerSoundEvent("railgun_high_charge");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_CHARGE_CONTINUOUS = registerSoundEvent("railgun_high_charge_continuous");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE = registerSoundEvent("railgun_high_discharge");
    public static final RegistryObject<SoundEvent> RAILGUN_HIGH_DISCHARGE_COMPLETE = registerSoundEvent("railgun_high_discharge_complete");
    public static final RegistryObject<SoundEvent> RAILGUN_OVERCHARGE_DAMAGE = registerSoundEvent("railgun_overcharge_damage");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(MOD_ID, name)));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(Moditems.MICRO_HID);
        }

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}

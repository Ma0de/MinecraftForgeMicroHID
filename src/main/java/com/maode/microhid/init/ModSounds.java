package com.maode.microhid.init;

import com.maode.microhid.config.MicroHIDConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MicroHIDConfig.MOD_ID);

    public static final RegistryObject<SoundEvent> DRAW = register("railgun_draw");
    public static final RegistryObject<SoundEvent> WIND_UP_HEAVY = register("railgun_wind_up_heavy");
    public static final RegistryObject<SoundEvent> WIND_UP_LIGHT = register("railgun_wind_up_light");
    public static final RegistryObject<SoundEvent> WIND_UP_BROKEN = register("railgun_wind_up_broken");
    public static final RegistryObject<SoundEvent> FIRE_HEAVY = register("railgun_fire_heavy");
    public static final RegistryObject<SoundEvent> FIRE_LIGHT = register("railgun_fire_light");
    public static final RegistryObject<SoundEvent> STOP_FIRING_HEAVY = register("railgun_stop_firing_heavy");
    public static final RegistryObject<SoundEvent> STOP_FIRING_LIGHT = register("railgun_stop_firing_light");
    public static final RegistryObject<SoundEvent> STOP_FIRING_BROKEN = register("railgun_stop_firing_broken");
    public static final RegistryObject<SoundEvent> INSPECT = register("railgun_inspect");
    public static final RegistryObject<SoundEvent> OVERCHARGE = register("railgun_overcharge");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MicroHIDConfig.MOD_ID, name)));
    }
}
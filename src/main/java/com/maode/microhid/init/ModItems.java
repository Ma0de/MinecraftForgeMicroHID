package com.maode.microhid.init;

import com.maode.microhid.config.MicroHIDConfig;
import com.maode.microhid.item.MicroHIDItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MicroHIDConfig.MOD_ID);

    public static final RegistryObject<Item> MICRO_HID = ITEMS.register("micro_hid", 
            () -> new MicroHIDItem(new Item.Properties().stacksTo(1)));
}
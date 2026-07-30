package com.maode.microhid.network;

import com.maode.microhid.config.MicroHIDConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(MicroHIDConfig.MOD_ID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );
        CHANNEL.registerMessage(0, HIDInputPacket.class, 
                HIDInputPacket::encode, 
                HIDInputPacket::decode, 
                HIDInputPacket::handle);
    }
}
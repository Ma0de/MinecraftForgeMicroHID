package com.maode.microhid.client;

import com.maode.microhid.config.MicroHIDConfig;
import com.maode.microhid.init.ModItems;
import com.maode.microhid.network.HIDInputPacket;
import com.maode.microhid.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MicroHIDConfig.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static boolean lastLeft = false;
    private static boolean lastRight = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        boolean left = mc.options.keyAttack.isDown();
        boolean right = mc.options.keyUse.isDown();

        if (left != lastLeft || right != lastRight) {
            lastLeft = left;
            lastRight = right;
            if (player.getMainHandItem().is(ModItems.MICRO_HID.get())) {
                NetworkHandler.CHANNEL.sendToServer(new HIDInputPacket(left, right));
            }
        }
    }
}
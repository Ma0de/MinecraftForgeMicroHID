package com.maode.microhid.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HIDInputPacket {
    private final boolean leftDown;
    private final boolean rightDown;

    public HIDInputPacket(boolean leftDown, boolean rightDown) {
        this.leftDown = leftDown;
        this.rightDown = rightDown;
    }

    public static void encode(HIDInputPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.leftDown);
        buf.writeBoolean(msg.rightDown);
    }

    public static HIDInputPacket decode(FriendlyByteBuf buf) {
        return new HIDInputPacket(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(HIDInputPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                HIDInputCache.updateInput(player.getUUID(), msg.leftDown, msg.rightDown);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
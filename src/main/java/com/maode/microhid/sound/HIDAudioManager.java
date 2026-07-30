package com.maode.microhid.sound;

import com.maode.microhid.MicroHIDMod;
import com.maode.microhid.init.ModSounds;
import com.maode.microhid.item.data.HIDState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class HIDAudioManager {

    public static void play(Level level, Player player, RegistryObject<net.minecraft.sounds.SoundEvent> sound) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (sound.get() == null) {
            MicroHIDMod.LOGGER.error("[HID Audio] SoundEvent is null!");
            return;
        }

        MicroHIDMod.LOGGER.info("[HID Audio] Server playing: {}", sound.getId());
        serverLevel.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            sound.get(),
            SoundSource.PLAYERS,
            1.0F, 1.0F
        );
    }

    public static void stop(Level level, RegistryObject<net.minecraft.sounds.SoundEvent> sound) {
        if (level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (sound.get() == null) return;

        var packet = new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
            sound.getId(), SoundSource.PLAYERS
        );
        serverLevel.getServer().getPlayerList().broadcastAll(packet);
    }

    public static void onStateChange(Level level, Player player, HIDState newState) {
        stop(level, ModSounds.WIND_UP_LIGHT);
        stop(level, ModSounds.FIRE_LIGHT);
        stop(level, ModSounds.WIND_UP_HEAVY);
        stop(level, ModSounds.FIRE_HEAVY);
        stop(level, ModSounds.WIND_UP_BROKEN);

        switch (newState) {
            case PRIMARY_WINDUP   -> play(level, player, ModSounds.WIND_UP_LIGHT);
            case PRIMARY_FIRING   -> play(level, player, ModSounds.FIRE_LIGHT);
            case CHARGE_WINDUP    -> play(level, player, ModSounds.WIND_UP_HEAVY);
            case CHARGE_FIRING    -> play(level, player, ModSounds.FIRE_HEAVY);
            case OVERCHARGE_EXPLODING -> play(level, player, ModSounds.OVERCHARGE);
            default -> {}
        }
    }
}
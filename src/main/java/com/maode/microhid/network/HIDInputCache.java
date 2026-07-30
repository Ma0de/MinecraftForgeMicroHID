package com.maode.microhid.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HIDInputCache {
    private static final Map<UUID, boolean[]> INPUTS = new ConcurrentHashMap<>();

    public static void updateInput(UUID id, boolean left, boolean right) {
        INPUTS.put(id, new boolean[]{left, right});
    }

    public static boolean isLeftDown(UUID id) {
        return INPUTS.containsKey(id) && INPUTS.get(id)[0];
    }

    public static boolean isRightDown(UUID id) {
        return INPUTS.containsKey(id) && INPUTS.get(id)[1];
    }

    public static void removePlayer(UUID id) {
        INPUTS.remove(id);
    }
}
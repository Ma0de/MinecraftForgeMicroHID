package com.maode.microhid.item.target;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HIDTargeting {
    public static void firePrimary(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        double range = 6.0D;

        AABB area = new AABB(eyePos, eyePos.add(lookVec.scale(range))).inflate(1.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);

        LivingEntity nearest = null;
        double nearestDist = range * range;

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(eyePos);
            if (toEntity.dot(lookVec) > 0 && toEntity.lengthSqr() < nearestDist) {
                nearestDist = toEntity.lengthSqr();
                nearest = entity;
            }
        }
        if (nearest != null) nearest.hurt(player.damageSources().playerAttack(player), 20.0F); // 400 DPS
    }

    public static void fireHeavy(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        double range = 10.0D;

        AABB area = new AABB(eyePos, eyePos.add(lookVec.scale(range))).inflate(2.0D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(eyePos);
            if (toEntity.dot(lookVec) > 0.5) {
                entity.hurt(player.damageSources().playerAttack(player), 50.0F); // 1000 DPS
            }
        }
    }

    public static void fireBroken(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        double range = 4.0D;

        AABB area = new AABB(eyePos, eyePos.add(lookVec.scale(range))).inflate(1.5D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player);

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(eyePos);
            if (toEntity.dot(lookVec) > 0.3) {
                entity.hurt(player.damageSources().playerAttack(player), 20.0F); // 400 DPS
            }
        }
    }
}
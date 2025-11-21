package com.maode.microhid.item;

import com.maode.microhid.MicroHIDMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// 新增导入
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import java.util.List;

public class Moditems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MicroHIDMod.MOD_ID);

    public static final RegistryObject<Item> MICRO_HID =
            ITEMS.register("micro_hid", () -> new RailgunItem(new Item.Properties().stacksTo(1)) {
                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("item.micro_hid.micro_hid.desc"));
                    super.appendHoverText(stack, level, tooltip, flag);
                }

                @Override
                public boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity) {
                    // 只允许在主手装备，禁止副手
                    return slot == EquipmentSlot.MAINHAND;
                }

                @Override
                public EquipmentSlot getEquipmentSlot(ItemStack stack) {
                    // 明确指定装备槽位为主手
                    return EquipmentSlot.MAINHAND;
                }
            });

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
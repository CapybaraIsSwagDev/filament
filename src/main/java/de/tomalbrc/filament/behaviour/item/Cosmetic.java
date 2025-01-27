package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/**
 * Cosmetics; either head or chestplate slot, can be Blockbenchmodel for chestplate slot or simple item model for either
 */
public class Cosmetic implements ItemBehaviour<Cosmetic.CosmeticConfig> {
    private final CosmeticConfig config;

    public Cosmetic(CosmeticConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public CosmeticConfig getConfig() {
        return this.config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player player, InteractionHand interactionHand) {
        if (item instanceof Equipable equipable) {
            return equipable.swapWithEquipmentSlot(item, level, player, interactionHand);
        }

        return ItemBehaviour.super.use(item, level, player, interactionHand);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return this.config.slot;
    }

    public static class CosmeticConfig {
        /**
         * The equipment slot for the cosmetic (head, chest).
         */
        public EquipmentSlot slot;

        /**
         * The resource location of the animated model for the cosmetic.
         */
        public ResourceLocation model;

        /**
         * The name of the animation to autoplay. The animation should be loopable
         */
        public String autoplay;

        /**
         * Scale of the chest cosmetic
         */
        public Vector3f scale = new Vector3f(1);

        /**
         * Translation of the chest cosmetic
         */
        public Vector3f translation = new Vector3f();
    }
}

package de.tomalbrc.filament.behaviour.item;

import de.tomalbrc.filament.api.behaviour.ItemBehaviour;
import de.tomalbrc.filament.item.BaseProjectileEntity;
import de.tomalbrc.filament.registry.EntityRegistry;
import de.tomalbrc.filament.util.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

/**
 * Item behaviour for projectile shooting
 */
public class Shoot implements ItemBehaviour<Shoot.ShootConfig> {
    private final ShootConfig config;

    public Shoot(ShootConfig config) {
        this.config = config;
    }

    @Override
    @NotNull
    public ShootConfig getConfig() {
        return this.config;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Item item, Level level, Player user, InteractionHand hand) {
        user.getCooldowns().addCooldown(item, 8);
        ItemStack itemStack = user.getItemInHand(hand);

        if (!level.isClientSide) {
            BaseProjectileEntity projectile = EntityRegistry.BASE_PROJECTILE.create(level);
            if (projectile != null) {
                projectile.setPos(user.position().add(0, user.getEyeHeight(), 0));
                Util.damageAndBreak(1, itemStack, user, Player.getSlotForHand(hand));

                float pitch = user.getXRot();
                float yaw = user.getYRot();
                double speed = this.config.speed; // Adjust the speed as needed

                Vector3f deltaMovement = new Vector3f(
                        -(float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch)),
                        -(float) Math.sin(Math.toRadians(pitch)),
                        (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch))
                ).mul((float) speed);

                projectile.setYRot(user.getYRot());
                projectile.setXRot(user.getXRot());
                projectile.setDeltaMovement(deltaMovement.x, deltaMovement.y, deltaMovement.z);

                var projItem = this.config.projectile != null ? BuiltInRegistries.ITEM.get(this.config.projectile).getDefaultInstance() : itemStack.copyWithCount(1);
                projectile.setProjectileStack(projItem);
                projectile.setPickupStack(projItem);
                projectile.setOwner(user);
                projectile.setBaseDamage(this.config.baseDamage);

                if (user.isCreative()) {
                    projectile.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            level.addFreshEntity(projectile);
            level.playSound(null, projectile, this.config.sound != null ? BuiltInRegistries.SOUND_EVENT.get(this.config.sound) : SoundEvents.TRIDENT_THROW.value(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            if (!user.isCreative() && this.config.consumes) {
                itemStack.shrink(1);
            }
        }

        user.awardStat(Stats.ITEM_USED.get(item));

        return InteractionResultHolder.consume(itemStack);
    }

    public static class ShootConfig {
        /**
         * Indicates whether the shooting action consumes the item
         */
        public boolean consumes;

        /**
         * The base damage of the projectile.
         */
        public double baseDamage = 2.0;

        /**
         * The speed at which the projectile is fired.
         */
        public double speed = 1.0;

        /**
         * The identifier for the projectile item
         */
        public ResourceLocation projectile;

        /**
         * Sound effect to play when shooting
         */
        public ResourceLocation sound;
    }
}

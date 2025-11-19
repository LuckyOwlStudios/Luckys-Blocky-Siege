package net.luckystudios.blocks.custom.shooting.cannon;

import net.luckystudios.BlockySiegeConfig;
import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.util.enums.FiringState;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.gui.cannons.CannonBlockMenu;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModParticleTypes;
import net.luckystudios.init.ModSoundEvents;
import net.luckystudios.init.ModTags;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CannonBlockEntity extends AbstractShootingAimableBlockEntity {

    public int firePower;
    public int cooldown, maxCooldown;

    public CannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), pos, blockState);
        this.maxCooldown = 60;
        this.animationLength = 0.25F;
        this.firePower = 4; // Default firepower
        this.cooldown = -1; // Default cooldown
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockEntity cannonBlockEntity) {
        // Needed to handle rotates of the gun
        extraTick(level, pos, state, cannonBlockEntity);

        if (cannonBlockEntity.cooldown > 0) {
            cannonBlockEntity.cooldown--;
            cannonBlockEntity.setChanged();
        }
        if (cannonBlockEntity.animationTime > 0) {
            cannonBlockEntity.animationTime--;
        }

        // Fire cannon when cooldown finishes
        if (cannonBlockEntity.cooldown == 0) {
            if (cannonBlockEntity.canShoot()) {
                fireCannon(level, pos, cannonBlockEntity);
                cannonBlockEntity.setChanged();
                cannonBlockEntity.cooldown = -40; // Set to -5 to begin flash
            } else {
                cannonBlockEntity.cooldown = -1; // Set to -1 to prevent firing again
            }
            return; // Exit after firing
        }

        if (cannonBlockEntity.cooldown < -1) {
            cannonBlockEntity.cooldown++;
            if (cannonBlockEntity.cooldown > -55 && cannonBlockEntity.getBlockState().getValue(CannonBlock.FIRING_STATE) == FiringState.FIRED) {
                level.setBlock(pos, cannonBlockEntity.getBlockState()
                        .setValue(CannonBlock.FIRING_STATE, FiringState.OFF)
                        .setValue(CannonBlock.LIT, false), 3);
            }
            // Only render smoke particles on client side
            if (level.isClientSide) {
                Vec3 direction = getAimVector(cannonBlockEntity);
                Vec3 particlePos = Vec3.atCenterOf(pos).add(direction.scale(1.25F));
                level.addParticle(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
            }
        }

        if (cannonBlockEntity.cooldown <= -1) return; // Return if already fired

        ItemStack cannonBall = cannonBlockEntity.getProjectile();

        // Visual effects: flame & smoke while cooling down - CLIENT SIDE ONLY
        if (level.isClientSide && !isControllingThisBlock(cannonBlockEntity)) {
            Vec3 particlePos = getRelativeLocationWithOffset(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.75f, 0.3f, 0.0f);

            level.addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
        }

        // Bottle pop effect when cooldown hits 20 - SERVER SIDE for sound and custom particles
        if (cannonBlockEntity.cooldown == 20 && cannonBall.is(ModTags.BOTTLED_AMMO)) {
            if (level instanceof ServerLevel serverLevel) {
                Vec3 direction = getAimVector(cannonBlockEntity);
                Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(1.25));

                serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                        ModSoundEvents.BOTTLE_POP_SMALL.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

                serverLevel.sendParticles(
                        ModParticleTypes.BOTTLE_CAP.get(),
                        spawnPos.x, spawnPos.y, spawnPos.z,
                        0, direction.x, direction.y, direction.z, 0.25
                );
            }
        }
    }

    public static void fireCannon(Level level, BlockPos pos, CannonBlockEntity cannonBlockEntity) {
        cannonBlockEntity.animationTime = cannonBlockEntity.animationLength;

        // Normalize direction
        Vec3 direction = getAimVector(cannonBlockEntity);

        // Offset to spawn in front of the cannon
        double offsetDistance = 1.25;
        Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(offsetDistance));

        SoundEvent cannonSound = getCannonSoundVariant(level, spawnPos);
        if (level.isClientSide()) {
            float soundVolume = 2f + cannonBlockEntity.firePower * 0.6f;
            level.playLocalSound(pos, ModSoundEvents.CANNON_FIRE.get(), SoundSource.BLOCKS, soundVolume, 1, false);
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.GUST,
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    1,  // count
                    0.0, 0.0, 0.0,  // xOffset/yOffset/zOffset for random spread
                    0.0  // speed
            );
        }

        float slowdownFactor = 0.05f; // Adjust this value to control the slowdown effect
        for (int i = 0; i < 12; i++) {
            Random random = new Random();
            double xOffset = (random.nextDouble() - 0.5) * 0.5; // Range: -0.25 to +0.25
            double yOffset = (random.nextDouble() - 0.5) * 0.5;
            double zOffset = (random.nextDouble() - 0.5) * 0.5;
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
                        spawnPos.x + xOffset, spawnPos.y + yOffset, spawnPos.z + zOffset,
                        0,  // count
                        direction.x, direction.y, direction.z,  // xOffset/yOffset/zOffset for random spread
                        (0.03125 * cannonBlockEntity.firePower) - slowdownFactor  // The speed is determined by the fire power and gradually reduced with each particle
                );
            }
            slowdownFactor -= 0.05F; // Gradually reduce the speed for each particle
        }

        // Inventory slot 1 must contain gunpowder
        int requestedPower = cannonBlockEntity.getFirePower(); // desired firepower (1–4)
        int availableGunpowder = cannonBlockEntity.getFuse().getCount();

        // If there's no gunpowder, do nothing
        if (availableGunpowder <= 0) return;

        // Clamp actual power to available gunpowder
        int actualPower = Math.min(requestedPower, availableGunpowder);

        // Create and launch the projectile
        Projectile cannonBall = getCannonBall(level, spawnPos, cannonBlockEntity, direction);
        if (cannonBall == null) return;
        cannonBall.shoot(direction.x, direction.y, direction.z, actualPower, 0); // power * speed factor
        cannonBlockEntity.removeItem(0, actualPower);
        cannonBlockEntity.removeItem(1, 1);
        level.addFreshEntity(cannonBall);
        Random random = new Random();

        // We try to damage the cannon based on how powerful the shot was!
        // Fully powered shot is a 6% chance to damage, half of how an anvil works
        if (random.nextFloat() < actualPower * 0.015) {
            if (cannonBlockEntity.getBlockState().getBlock() instanceof DamageableBlock damageableBlock) {
                damageableBlock.damageBlock(null, level, pos, cannonBlockEntity.getBlockState());
            }
        } else {
            level.setBlock(pos, cannonBlockEntity.getBlockState().setValue(CannonBlock.FIRING_STATE, FiringState.FIRED), 3);
        }
    }

    public int getFirePower() {
        return this.firePower;
    }

    public ItemStack getFuse() {
        return this.getItem(0);
    }

    public ItemStack getProjectile() {
        return this.getItem(1);
    }

    public float getFiringProgress() {
        if (this.cooldown <= 0) {
            return 0.0f; // Not firing or ready to fire
        }

        // Calculate progress from 0.0 (just started) to 1.0 (ready to fire)
        return 1.0f - ((float) this.cooldown / (float) this.maxCooldown);
    }

    @Override
    public boolean canShoot() {
        ItemStack itemStack = this.getFuse();
        boolean hasFuse = itemStack.is(Tags.Items.GUNPOWDERS);
        ItemStack itemStack2 = this.getProjectile();
        boolean hasAmmo = itemStack2.is(ModTags.CANNON_AMMO);
        return hasFuse && hasAmmo && this.cooldown <= 0;
    }

    private static Projectile getCannonBall(Level level, Vec3 spawnPos, CannonBlockEntity cannonBlockEntity, Vec3 direction) {
        ItemStack projectileStack = cannonBlockEntity.getProjectile();

        // Check if the item is a BlockItem and if its block implements ProjectileItem
        // Needed due to Cannonballs being Blocks and not Items
        if (projectileStack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof ProjectileItem projectileItem) {
                return projectileItem.asProjectile(level, spawnPos, projectileStack, Direction.getNearest(direction));
            }
        }

        // Fallback: check if the item itself is a ProjectileItem
        if (projectileStack.getItem() instanceof ProjectileItem projectileItem) {
            return projectileItem.asProjectile(level, spawnPos, projectileStack, Direction.getNearest(direction));
        }

        return null;
    }

    private static SoundEvent getCannonSoundVariant(Level level, Vec3 position) {
        Player nearestPlayer = level.getNearestPlayer(position.x, position.y, position.z, 12, false);
        if (nearestPlayer != null) {
            double distSq = nearestPlayer.distanceToSqr(position);
            if (distSq >= 256.0) {
                return SoundEvents.GENERIC_EXPLODE.value(); // <- you'll need to register this
            }
        }
        return ModSoundEvents.CANNON_FIRE.get();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("firePower", this.firePower);
        tag.putInt("cooldown", this.cooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.firePower = tag.getInt("firePower");
        this.cooldown = tag.getInt("cooldown");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new CannonBlockMenu(containerId, inventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
        return this.saveWithFullMetadata(lookupProvider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
    }

    @Override
    public float maxPitch() {
        return 45;
    }

    @Override
    public float minPitch() {
        return -45;
    }

    @Override
    public float pitchOffset() {
        return 4.0F;
    }

    @Override
    public float barrelOffsetDistance() {
        return 1.25F;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.blockysiege.cannon");
    }
}

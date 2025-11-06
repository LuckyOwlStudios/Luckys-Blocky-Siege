package net.luckystudios.blocks.custom.cannon.types.generic;

import net.luckystudios.blocks.custom.cannon.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.cannon_ammo.CannonBallProjectileBlock;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.entity.custom.cannon_ball.AbstractCannonBall;
import net.luckystudios.init.ModParticleTypes;
import net.luckystudios.init.ModSoundEvents;
import net.luckystudios.init.ModTags;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class CannonBlockEntity extends AbstractShootingAimableBlockEntity {

    public CannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), pos, blockState);
        this.maxCooldown = 60;
        this.animationLength = 0.25F;
    }

    @Override
    public int inventorySize() {
        return 2;
    }

    public int getFirePower() {
        return this.firePower;
    }

    @Override
    public boolean hasFuse(ItemStack fuseStack) {
        return fuseStack.is(Tags.Items.GUNPOWDERS);
    }

    @Override
    public boolean hasAmmo(ItemStack ammoStack) {
        return ammoStack.is(ModTags.CANNON_AMMO);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CannonBlockEntity cannonBlockEntity) {
        extraTick(level, pos, state, cannonBlockEntity);

        if (cannonBlockEntity.cooldown <= 0) return;

        ItemStack cannonBall = cannonBlockEntity.inventory.getStackInSlot(1);

        // Visual effects: flame & smoke while cooling down
        if (level instanceof ServerLevel serverLevel) {
            Vec3 particlePos = getRelativeLocationWithOffset(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.75f, 0.3f, 0.0f);

            serverLevel.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 1, 0.0, 0.0, 0.0, 0.0);

            // Bottle pop effect when cooldown hits 20
            if (cannonBlockEntity.cooldown == 20 && cannonBall.is(ModTags.BOTTLED_AMMO)) {
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

        // Fire cannon when cooldown finishes
        if (cannonBlockEntity.cooldown == 1 && cannonBlockEntity.canShoot(cannonBlockEntity, false)) {
            fireCannon(level, pos, cannonBlockEntity);
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
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                cannonSound, SoundSource.BLOCKS, 1.0f, 1.0f);

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
        int availableGunpowder = cannonBlockEntity.inventory.getStackInSlot(1).getCount();

        // If there's no gunpowder, do nothing
        if (availableGunpowder <= 0) return;

        // Clamp actual power to available gunpowder
        int actualPower = Math.min(requestedPower, availableGunpowder);

        // Create and launch the projectile
        AbstractCannonBall cannonBall = getCannonBall(level, spawnPos, cannonBlockEntity);
        if (cannonBall == null) return;
        cannonBall.shoot(direction.x, direction.y, direction.z, actualPower, 0); // power * speed factor
        cannonBlockEntity.inventory.extractItem(0, actualPower, false);
        cannonBlockEntity.inventory.extractItem(1, 1, false);
        level.addFreshEntity(cannonBall);
    }

    private static AbstractCannonBall getCannonBall(Level level, Vec3 spawnPos, CannonBlockEntity cannonBlockEntity) {
        ItemStack cannonBallStack = cannonBlockEntity.inventory.getStackInSlot(1);
        Item cannonBallItem = cannonBallStack.getItem();
        if (!(cannonBallItem instanceof BlockItem blockItem)) return null;
        if (blockItem.getBlock() instanceof CannonBallProjectileBlock ballProjectileBlock) {
            return ballProjectileBlock.asProjectile(level, spawnPos.x, spawnPos.y, spawnPos.z);
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
    public Component getDisplayName() {
        return Component.translatable("container.blockysiege.cannon");
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
}

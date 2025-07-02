package net.luckystudios.blocks.custom.cannon.types.multi;

import net.luckystudios.blocks.custom.cannon.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.luckystudios.entity.custom.bullet.Bullet;
import net.luckystudios.sounds.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiCannonBlockEntity extends AbstractShootingAimableBlockEntity {

    public int barrelIndex;

    public MultiCannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), pos, blockState);
        this.maxCooldown = 60;
        this.animationLength = 0.75F;
        this.barrelIndex = 0; // Default to the first barrel
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultiCannonBlockEntity multiCannonBlockEntity) {
        extraTick(level, pos, state, multiCannonBlockEntity);
        Vec3 particlePos = getParticleLocation(multiCannonBlockEntity, new Vec3(0, 0.0625, 0), 0.5f, 0.3f);
        if (multiCannonBlockEntity.cooldown > 0) {
            if (multiCannonBlockEntity.cooldown > multiCannonBlockEntity.maxCooldown - 15) {
                if (multiCannonBlockEntity.cooldown % 3 == 0) {
                    fireCannon(level, pos, multiCannonBlockEntity);
                }
            } else {
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            particlePos.x, particlePos.y, particlePos.z,
                            0,  // count
                            0.0, 0.0, 0.0,  // xOffset/yOffset/zOffset for random spread
                            0.0  // speed
                    );
                }
            }
            if (multiCannonBlockEntity.cooldown == 1) {
                Vec3 direction = getAimVector(multiCannonBlockEntity);
                Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(1.25));
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }

    public static void fireCannon(Level level, BlockPos pos, MultiCannonBlockEntity multiCannonBlockEntity) {
        if (level.isClientSide) return;
        level.playSound(null, pos, ModSoundEvents.SHOOT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        // Normalize direction
        Vec3 direction = getAimVector(multiCannonBlockEntity);

        // Offset to spawn in front of the cannon
        double offsetDistance = 1.25;
//        Vec3 spawnPos = Vec3.atCenterOf(pos).add(getAimVector(cannonBlockEntity).scale(offsetDistance));
        Vec3 spawnPos = getBarrelPositions(multiCannonBlockEntity).get(multiCannonBlockEntity.barrelIndex);

        multiCannonBlockEntity.inventory.extractItem(0, 1, false);

        // Create and launch the projectile
        Bullet bullet = new Bullet(level, spawnPos.x, spawnPos.y, spawnPos.z);
        bullet.shoot(direction.x, direction.y, direction.z, multiCannonBlockEntity.getFirePower(), 0); // power * speed factor

        level.addFreshEntity(bullet);
        multiCannonBlockEntity.barrelIndex += 1;
        if (multiCannonBlockEntity.barrelIndex > 3) multiCannonBlockEntity.barrelIndex = 0; // Reset if out of bounds
    }

    private static List<Vec3> getBarrelPositions(MultiCannonBlockEntity cannonBlockEntity) {
        Vec3 topLeftBarrel = getRelativeLocation(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.125F, -1.0F, 0.1875F);
        Vec3 topRightBarrel = getRelativeLocation(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.125F, -1.0F, -0.1875F);
        Vec3 bottomLeftBarrel = getRelativeLocation(cannonBlockEntity, new Vec3(0, 0.0625, 0), -0.125F, -1.0F, 0.1875F);
        Vec3 bottomRightBarrel = getRelativeLocation(cannonBlockEntity, new Vec3(0, 0.0625, 0), -0.125F, -1.0F, -0.1875F);
        return List.of(topLeftBarrel, topRightBarrel, bottomLeftBarrel, bottomRightBarrel);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.blockysiege.multi_cannon");
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
}

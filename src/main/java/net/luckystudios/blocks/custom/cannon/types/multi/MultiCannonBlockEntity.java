package net.luckystudios.blocks.custom.cannon.types.multi;

import net.luckystudios.blocks.custom.cannon.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.custom.cannon.types.generic.CannonBlock;
import net.luckystudios.blocks.util.ModBlockEntityTypes;
import net.luckystudios.datagen.items.ModItemTags;
import net.luckystudios.entity.custom.bullet.Bullet;
import net.luckystudios.sounds.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class MultiCannonBlockEntity extends AbstractShootingAimableBlockEntity {

    public static int maxCooldown = 60; // Cooldown for firing the cannon

    public MultiCannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultiCannonBlockEntity cannonBlockEntity) {
        ServerLevel serverLevel = (ServerLevel) level;
        Vec3 particlePos = getParticleLocation(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.5f, 0.3f);
        if (cannonBlockEntity.cooldown > 0) {
            if (cannonBlockEntity.cooldown > maxCooldown - 12) {
                if (cannonBlockEntity.cooldown % 3 == 0) {
                    fireCannon(level, pos, cannonBlockEntity);
                }
            } else {
                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        particlePos.x, particlePos.y, particlePos.z,
                        0,  // count
                        0.0, 0.0, 0.0,  // xOffset/yOffset/zOffset for random spread
                        0.0  // speed
                );
            }
            cannonBlockEntity.cooldown--;
            if (cannonBlockEntity.cooldown == 1) {
                Vec3 direction = getAimVector(cannonBlockEntity);
                Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(1.25));
                serverLevel.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public static void fireCannon(Level level, BlockPos pos, MultiCannonBlockEntity cannonBlockEntity) {
        if (level.isClientSide) return;

        level.playSound(null, pos, ModSoundEvents.SHOOT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        // Normalize direction
        Vec3 direction = getAimVector(cannonBlockEntity);

        // Offset to spawn in front of the cannon
        double offsetDistance = 1.25;
        Vec3 spawnPos = Vec3.atCenterOf(pos).add(getAimVector(cannonBlockEntity).scale(offsetDistance));

        cannonBlockEntity.inventory.extractItem(0, 1, false);

        // Create and launch the projectile
        Bullet bullet = new Bullet(level, spawnPos.x, spawnPos.y, spawnPos.z);
        bullet.shoot(direction.x, direction.y, direction.z, cannonBlockEntity.getFirePower(), 0); // power * speed factor

        level.addFreshEntity(bullet);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.blockysiege.multi_cannon");
    }
}

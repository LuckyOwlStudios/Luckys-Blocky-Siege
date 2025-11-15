package net.luckystudios.blocks.custom.shooting.multi_cannon;

import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.gui.multi_cannon.MultiCannonBlockMenu;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.entity.custom.bullet.FireworkStarProjectile;
import net.luckystudios.init.ModSoundEvents;
import net.luckystudios.init.ModTags;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.FireworkStarItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MultiCannonBlockEntity extends AbstractShootingAimableBlockEntity {

    public int barrelIndex; // Used to get which barrel we are currently at in the firing process
    private int shotsRemaining; // How many shots are remaining
    public int bullet_count;
    public int cooldown, maxCooldown;

    public MultiCannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.MULTI_CANNON_BLOCK_ENTITY.get(), pos, blockState);
        this.maxCooldown = 40;
        this.animationLength = 0.75F;
        this.barrelIndex = 0; // Default to the first barrel
        this.shotsRemaining = 0;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new MultiCannonBlockMenu(containerId, inventory, this);
    }

    public ItemStack getFuse() {
        return this.getItem(0);
    }

    public ItemStack getProjectile() {
        return this.getItem(1);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MultiCannonBlockEntity multiCannonBlockEntity) {
        // Needed to handle rotates of the gun
        extraTick(level, pos, state, multiCannonBlockEntity);

        if (multiCannonBlockEntity.cooldown == multiCannonBlockEntity.maxCooldown) {
            multiCannonBlockEntity.shotsRemaining = 4; // Set number of shots
        }

        if (multiCannonBlockEntity.cooldown > 0) {
            multiCannonBlockEntity.cooldown--;
            multiCannonBlockEntity.setChanged();
        }
        if (multiCannonBlockEntity.animationTime > 0) {
            multiCannonBlockEntity.animationTime--;
        }

        Vec3 particlePos = getRelativeLocationWithOffset(multiCannonBlockEntity, new Vec3(0, 0.0625, 0), 0.5f, 0.3f, 0.0F);
        if (multiCannonBlockEntity.cooldown > 0) {
            if (multiCannonBlockEntity.cooldown > multiCannonBlockEntity.maxCooldown - 15) {
                if (multiCannonBlockEntity.cooldown % 3 == 0 && multiCannonBlockEntity.canFire() && multiCannonBlockEntity.shotsRemaining > 0) {
                    fireRound(level, pos, multiCannonBlockEntity);
                    multiCannonBlockEntity.shotsRemaining--;
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

    // Used to see if the gun can start to shoot
    @Override
    public boolean canShoot() {
        return canFire() && this.cooldown == 0;
    }

    // Used to see if the gun can continue to shoot its other rounds in the burst
    private boolean canFire() {
        ItemStack fuseStack = this.getFuse();
        boolean hasFuse = fuseStack.is(Tags.Items.GUNPOWDERS);
        ItemStack ammoStack = this.getProjectile();
        boolean hasAmmo = ammoStack.is(ModTags.MULTI_CANNON_SHOOTABLE);
        return hasAmmo && hasFuse;
    }

    @Override
    public float barrelOffsetDistance() {
        return 1.25F;
    }

    public static void fireRound(Level level, BlockPos pos, MultiCannonBlockEntity multiCannonBlockEntity) {
        if (level.isClientSide) return;
        level.playSound(null, pos, ModSoundEvents.SHOOT.get(), SoundSource.BLOCKS, 1.0f, 1.0f);

        // Normalize direction
        Vec3 direction = getAimVector(multiCannonBlockEntity);

        // Offset to spawn in front of the cannon
        Vec3 spawnPos = getBarrelPositions(multiCannonBlockEntity).get(multiCannonBlockEntity.barrelIndex);

        // Create and launch the projectile
        ItemStack itemStack = multiCannonBlockEntity.getProjectile();
        if (itemStack.getItem() instanceof ProjectileItem projectileItem) {
            Projectile projectile = projectileItem.asProjectile(level, spawnPos, itemStack, Direction.getNearest(direction));
            projectile.shoot(direction.x, direction.y, direction.z, 2.0F, 1.0F);
            level.addFreshEntity(projectile);
        } else if (itemStack.getItem() instanceof FireworkStarItem) {
            FireworkStarProjectile fireworkStarProjectile = new FireworkStarProjectile(level, spawnPos.x, spawnPos.y, spawnPos.z, itemStack);
            fireworkStarProjectile.shoot(direction.x, direction.y, direction.z, 4.0F, 1.0F);
            level.addFreshEntity(fireworkStarProjectile);
        }
        // Consume
        multiCannonBlockEntity.removeItem(0, 1);
        multiCannonBlockEntity.removeItem(1, 1);
        multiCannonBlockEntity.barrelIndex += 1;
        if (multiCannonBlockEntity.barrelIndex > 3) multiCannonBlockEntity.barrelIndex = 0; // Reset if out of bounds

        Random random = new Random();
        if (random.nextFloat() >= 0.015) return;
        if (multiCannonBlockEntity.getBlockState().getBlock() instanceof DamageableBlock damageableBlock) {
            damageableBlock.damageBlock(null, level, pos, multiCannonBlockEntity.getBlockState());
        }
    }

    // We use this to get the multiple barrel positions on the multi-cannon
    private static List<Vec3> getBarrelPositions(MultiCannonBlockEntity cannonBlockEntity) {
        Vec3 topLeftBarrel = getRelativeLocationWithOffset(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.125F, -1.0F, 0.1875F);
        Vec3 topRightBarrel = getRelativeLocationWithOffset(cannonBlockEntity, new Vec3(0, 0.0625, 0), 0.125F, -1.0F, -0.1875F);
        Vec3 bottomLeftBarrel = getRelativeLocationWithOffset(cannonBlockEntity, new Vec3(0, 0.0625, 0), -0.125F, -1.0F, 0.1875F);
        Vec3 bottomRightBarrel = getRelativeLocationWithOffset(cannonBlockEntity, new Vec3(0, 0.0625, 0), -0.125F, -1.0F, -0.1875F);
        return List.of(topLeftBarrel, topRightBarrel, bottomLeftBarrel, bottomRightBarrel);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cooldown", this.cooldown);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.cooldown = tag.getInt("cooldown");
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
        return 2.5F;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.blockysiege.multi_cannon");
    }

    @Override
    public int getContainerSize() {
        return 2;
    }
}

package net.luckystudios.blocks.custom.shooting.cannon;

import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.blocks.util.enums.FiringState;
import net.luckystudios.blocks.util.interfaces.DamageableBlock;
import net.luckystudios.entity.ai.crew.SiegeCrewAI;
import net.luckystudios.entity.custom.block_projectile.BlockProjectile;
import net.luckystudios.entity.custom.cannonball.AbstractCannonBall;
import net.minecraft.world.entity.Mob;
import net.luckystudios.gui.cannons.CannonBlockMenu;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModParticleTypes;
import net.luckystudios.init.ModSoundEvents;
import net.luckystudios.init.ModTags;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
                resetFiringState(level, pos, cannonBlockEntity); // Nothing fired, so don't stay lit
            }
            return; // Exit after firing
        }

        if (cannonBlockEntity.cooldown < -1) {
            cannonBlockEntity.cooldown++;
            // Any non-idle state (FIRED, or CHARGING when the shot fizzled or damaged the cannon) ends here
            resetFiringState(level, pos, cannonBlockEntity);
            // Only render smoke particles on client side
            if (level.isClientSide) {
                Vec3 direction = getAimVector(cannonBlockEntity);
                Vec3 particlePos = Vec3.atCenterOf(pos).add(direction.scale(1.25F));
                level.addParticle(ParticleTypes.LARGE_SMOKE, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
            }
        }

        if (cannonBlockEntity.cooldown <= -1) {
            // Idle (cooldown isn't saved, so a cannon reloaded mid-fuse would otherwise stay lit forever)
            if (cannonBlockEntity.cooldown == -1) resetFiringState(level, pos, cannonBlockEntity);
            return; // Return if already fired
        }

        ItemStack cannonBall = cannonBlockEntity.getShotProjectile();

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

    private static void resetFiringState(Level level, BlockPos pos, CannonBlockEntity cannon) {
        if (level.isClientSide) return;
        BlockState state = cannon.getBlockState();
        if (state.getValue(CannonBlock.FIRING_STATE) == FiringState.OFF) return;
        level.setBlock(pos, state.setValue(CannonBlock.FIRING_STATE, FiringState.OFF).setValue(CannonBlock.LIT, false), 3);
    }

    public static void fireCannon(Level level, BlockPos pos, CannonBlockEntity cannonBlockEntity) {
        cannonBlockEntity.animationTime = cannonBlockEntity.animationLength;

        // Normalize direction
        Vec3 direction = getAimVector(cannonBlockEntity);

        // Offset to spawn in front of the cannon
        double offsetDistance = 1.25;
        Vec3 spawnPos = Vec3.atCenterOf(pos).add(direction.scale(offsetDistance));

        if (level.isClientSide()) {
            float soundVolume = 2f + cannonBlockEntity.firePower * 0.6f;
            // Close boom: normal range, so it fades out with distance.
            level.playLocalSound(spawnPos.x, spawnPos.y, spawnPos.z, ModSoundEvents.CANNON_FIRE.get(), SoundSource.BLOCKS,
                    soundVolume, 0.75f + level.random.nextFloat() * 0.2f, false);
            // Distant rumble: huge volume so the sound range reaches far past the close boom's.
            level.playLocalSound(spawnPos.x, spawnPos.y, spawnPos.z, ModSoundEvents.CANNON_FIRE_DISTANT.get(), SoundSource.BLOCKS,
                    10000f, 0.5f + level.random.nextFloat() * 0.2f, false);
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
        int availableGunpowder = cannonBlockEntity.getShotFuse().getCount();

        // If there's no gunpowder, do nothing
        if (availableGunpowder <= 0) return;

        // Clamp actual power to available gunpowder
        int actualPower = Math.min(requestedPower, availableGunpowder);

        // Create and launch the projectile
        Projectile cannonBall = getCannonBall(level, spawnPos, cannonBlockEntity, direction);
        if (cannonBall == null) return;
        // Shots from a mob crew belong to the gunner, and are tamer than a player's (see AbstractCannonBall#canBreak)
        Mob gunner = SiegeCrewAI.crewGunner(level, pos);
        if (gunner != null) {
            cannonBall.setOwner(gunner);
            if (cannonBall instanceof AbstractCannonBall crewBall) crewBall.setCrewFired(true);
        }
        cannonBall.shoot(direction.x, direction.y, direction.z, actualPower, 0); // power * speed factor
        // Spend loaded ammo first; crew ammo only fills what the slots lack
        if (cannonBlockEntity.getFuse().is(ModTags.GUNPOWDERS)) cannonBlockEntity.removeItem(0, actualPower);
        else cannonBlockEntity.crewPowder = 0;
        if (isValidAmmo(cannonBlockEntity.getProjectile())) cannonBlockEntity.removeItem(1, 1);
        else cannonBlockEntity.crewProjectile = ItemStack.EMPTY;
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

    // Ammo a mob crew brings. It is fired like loaded ammo but never stored in the container, so it can't be taken
    // out, and it isn't saved: it only exists while a crew works the gun.
    private ItemStack crewProjectile = ItemStack.EMPTY;
    private int crewPowder = 0;

    /** Gives the next shot crew ammo for whatever the slots are missing. */
    public void setCrewAmmo(ItemStack projectile, int powder) {
        this.crewProjectile = projectile.copyWithCount(1);
        this.crewPowder = Math.max(0, Math.min(4, powder));
    }

    @Override
    public boolean hasCrewAmmo() {
        return !this.crewProjectile.isEmpty() || this.crewPowder > 0;
    }

    @Override
    public void clearCrewAmmo() {
        this.crewProjectile = ItemStack.EMPTY;
        this.crewPowder = 0;
    }

    /** The gunpowder the next shot will use: loaded powder, else the crew's. */
    public ItemStack getShotFuse() {
        ItemStack fuse = this.getFuse();
        if (fuse.is(ModTags.GUNPOWDERS) || this.crewPowder <= 0) return fuse;
        return new ItemStack(Items.GUNPOWDER, this.crewPowder);
    }

    /** The ammo the next shot will fire: loaded ammo, else the crew's. */
    public ItemStack getShotProjectile() {
        ItemStack projectile = this.getProjectile();
        return isValidAmmo(projectile) ? projectile : this.crewProjectile;
    }

    public float getFiringProgress() {
        if (this.cooldown <= 0) {
            return 0.0f; // Not firing or ready to fire
        }

        // Calculate progress from 1.0 (just started) to 0.0 (ready to fire)
        return (float) this.cooldown / (float) this.maxCooldown;
    }

    @Override
    public boolean canShoot() {
        ItemStack itemStack = this.getShotFuse();
        boolean hasFuse = itemStack.is(ModTags.GUNPOWDERS);
        ItemStack itemStack2 = this.getShotProjectile();
        boolean hasAmmo = isValidAmmo(itemStack2);
        return hasFuse && hasAmmo && this.cooldown <= 0;
    }

    /** The cannon fires its dedicated ammo, and any other block as a placeable block projectile. */
    public static boolean isValidAmmo(ItemStack stack) {
        return stack.is(ModTags.CANNON_AMMO) || stack.getItem() instanceof BlockItem;
    }

    private static Projectile getCannonBall(Level level, Vec3 spawnPos, CannonBlockEntity cannonBlockEntity, Vec3 direction) {
        ItemStack projectileStack = cannonBlockEntity.getShotProjectile();

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

        // Any other block is fired as a block projectile that tries to place itself on impact
        if (projectileStack.getItem() instanceof BlockItem) {
            return new BlockProjectile(level, spawnPos.x, spawnPos.y, spawnPos.z, projectileStack);
        }

        return null;
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

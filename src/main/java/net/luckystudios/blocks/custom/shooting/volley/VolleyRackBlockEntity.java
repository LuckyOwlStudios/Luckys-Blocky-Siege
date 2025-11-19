package net.luckystudios.blocks.custom.shooting.volley;

import net.luckystudios.blocks.custom.shooting.AbstractShootingAimableBlockEntity;
import net.luckystudios.entity.custom.FallingFireworkRocketEntity;
import net.luckystudios.gui.volley.VolleyRackBlockMenu;
import net.luckystudios.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class VolleyRackBlockEntity extends AbstractShootingAimableBlockEntity {

    private int cooldown = 0;
    public int firePower;
    public int barrelIndex; // Used to get which barrel we are currently at in the firing process

    public VolleyRackBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.VOLLEY_BLOCK_ENTITY.get(), pos, blockState);
        this.cooldown = 0;
        this.firePower = 4;
        this.barrelIndex = 0; // Default to the first barrel
//        this.pitch = 30;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VolleyRackBlockEntity volleyBlockEntity) {
        extraTick(level, pos, state, volleyBlockEntity);

        // Decrement cooldown
        if (volleyBlockEntity.cooldown > 0) {
            volleyBlockEntity.cooldown--;
        }

        boolean has_redstone = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());

        // Only fire if redstone is on AND cooldown is 0
        if (has_redstone && volleyBlockEntity.cooldown <= 0) {

            int slots = volleyBlockEntity.items.size();
            Random random = new Random();
            int slotIndex = random.nextInt(slots);

            volleyBlockEntity.fireVolley(level, volleyBlockEntity, slotIndex);
            volleyBlockEntity.cooldown = 3; // Change this to change the rate of fire!
        }
    }

    public void fireVolley(Level level, VolleyRackBlockEntity volleyBlockEntity, int slotIndex) {
        if (!this.canShoot()) return;
        volleyBlockEntity.animationTime = volleyBlockEntity.animationLength;
        // Normalize direction
        Vec3 direction = getAimVector(volleyBlockEntity);

        // Offset to spawn in front of the cannon
        Vec3 spawnPos = getLaunchPositions(volleyBlockEntity).get(volleyBlockEntity.barrelIndex);
        Vec3 fusePos = getFusePositions(volleyBlockEntity).get(volleyBlockEntity.barrelIndex);

        if (level.isClientSide()) return;

        ItemStack itemStackAtIndex = volleyBlockEntity.getItem(slotIndex);

        Projectile projectile;
        if (itemStackAtIndex.getItem() instanceof FireworkRocketItem) {
            projectile = new FallingFireworkRocketEntity(level, itemStackAtIndex, spawnPos.x, spawnPos.y, spawnPos.z, true);
        } else if (itemStackAtIndex.getItem() instanceof ProjectileItem projectileItem) {
            projectile = projectileItem.asProjectile(level, spawnPos, itemStackAtIndex, Direction.getNearest(direction));
        } else {
            return;
        }


        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    spawnPos.x(), spawnPos.y(), spawnPos.z(),
                    0,  // count
                    direction.x, direction.y, direction.z,  // xOffset/yOffset/zOffset for random spread
                    0  // The speed is determined by the fire power and gradually reduced with each particle
            );
            serverLevel.sendParticles(
                    ParticleTypes.SMALL_FLAME,
                    fusePos.x(), fusePos.y(), fusePos.z(),
                    0,  // count
                    direction.x, direction.y, direction.z,  // xOffset/yOffset/zOffset for random spread
                    0  // The speed is determined by the fire power and gradually reduced with each particle
            );
        }
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z,
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 0.25f, 0.75f);

        // Create and launch the projectile
        projectile.shoot(direction.x, direction.y, direction.z, volleyBlockEntity.firePower * 0.5F, 12.0F); // power * speed factor
        level.addFreshEntity(projectile);
        volleyBlockEntity.removeItem(0, 1);
        volleyBlockEntity.removeItem(slotIndex, 1);
        volleyBlockEntity.barrelIndex += 1;
        if (volleyBlockEntity.barrelIndex > getLaunchPositions(volleyBlockEntity).size() - 1) {
            volleyBlockEntity.barrelIndex = 0; // Reset if out of bounds
        }
    }

    // We use this to get the multiple launch positions on the volley rack
    private static List<Vec3> getLaunchPositions(VolleyRackBlockEntity volleyRackBlockEntity) {
        Vec3 first = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, -volleyRackBlockEntity.barrelOffsetDistance(), 0.375F);
        Vec3 second = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, -volleyRackBlockEntity.barrelOffsetDistance(), 0.125F);
        Vec3 third = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, -volleyRackBlockEntity.barrelOffsetDistance(), -0.125F);
        Vec3 fourth = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, -volleyRackBlockEntity.barrelOffsetDistance(), -0.375F);
        Vec3 fifth = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, -volleyRackBlockEntity.barrelOffsetDistance(), 0.375F);
        Vec3 six = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, -volleyRackBlockEntity.barrelOffsetDistance(), 0.125F);
        Vec3 seven = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, -volleyRackBlockEntity.barrelOffsetDistance(), -0.125F);
        Vec3 eight = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, -volleyRackBlockEntity.barrelOffsetDistance(), -0.375F);
        return List.of(first, second, third, fourth, fifth, six, seven, eight);
    }

    private static List<Vec3> getFusePositions(VolleyRackBlockEntity volleyRackBlockEntity) {
        Vec3 first = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, 0.6F, 0.375F);
        Vec3 second = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, 0.6F, 0.125F);
        Vec3 third = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, 0.6F, -0.125F);
        Vec3 fourth = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), 0.15F, 0.6F, -0.375F);
        Vec3 fifth = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, 0.6F, 0.375F);
        Vec3 six = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, 0.6F, 0.125F);
        Vec3 seven = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, 0.6F, -0.125F);
        Vec3 eight = getRelativeLocationWithOffset(volleyRackBlockEntity, new Vec3(0, 0.0625, 0), -0.1F, 0.6F, -0.375F);
        return List.of(first, second, third, fourth, fifth, six, seven, eight);
    }

    public ItemStack getFuse() {
        return this.getItem(0);
    }

    @Override
    public float maxPitch() {
        return 30;
    }

    @Override
    public float minPitch() {
        return 30;
    }

    @Override
    public float pitchOffset() {
        return 0;
    }

    @Override
    public boolean canShoot() {
        return this.cooldown <= 0 && !this.getItem(0).isEmpty();
    }

    @Override
    public float barrelOffsetDistance() {
        return 0.75F;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.blockysiege.volley_rack");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new VolleyRackBlockMenu(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("cooldown", this.cooldown);
        tag.putInt("barrelIndex", this.barrelIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.cooldown = tag.getInt("cooldown");
        this.barrelIndex = tag.getInt("barrelIndex");
    }
}

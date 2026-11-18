package net.luckystudios.entity.custom.turrets.ballista;

import net.luckystudios.util.MenuHelper;
import net.luckystudios.blocks.custom.turret.ballista.BallistaBlockEntity;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.entity.custom.turrets.AbstractTurret;
import net.luckystudios.init.ModSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BallistaEntity extends AbstractTurret implements CrossbowAttackMob {

    // Synced to clients so the model can hide the strings without a crossbow and pull them back while charging.
    private static final EntityDataAccessor<Boolean> HAS_CROSSBOW = SynchedEntityData.defineId(BallistaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> CHARGE_PROGRESS = SynchedEntityData.defineId(BallistaEntity.class, EntityDataSerializers.FLOAT);

    public BallistaEntity(EntityType<BallistaEntity> entityType, Level level) {
        super(entityType, level);
        // The crossbow it holds is the one stored in the block, which drops it; never drop a copy.
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public BallistaEntity(Level level, BlockPos pos) {
        super(ModEntityTypes.BALLISTA.get(), level, pos);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_CROSSBOW, false);
        builder.define(CHARGE_PROGRESS, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0F)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, ATTACK_RANGE)
                .add(Attributes.ARMOR, 4.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new StationaryCrossbowAttackGoal<>(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, (float) ATTACK_RANGE, 1.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, (float) ATTACK_RANGE));

        // FIXED: Remove randomInterval for immediate targeting, set mustSee to false for longer range
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 1, false, false, null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 1, false, false, (entity) -> entity instanceof Enemy));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        ItemStack crossbow = ItemStack.EMPTY;
        Optional<BallistaBlockEntity> blockEntity = getBallistaBlockEntity();
        if (blockEntity.isPresent()) {
            ItemStack stored = blockEntity.get().inventory.getItem(BallistaBlockEntity.CROSSBOW_SLOT);
            if (stored.isEmpty() && stored != ItemStack.EMPTY) {
                // The crossbow broke while firing.
                blockEntity.get().inventory.setItem(BallistaBlockEntity.CROSSBOW_SLOT, ItemStack.EMPTY);
            } else if (stored.getItem() instanceof CrossbowItem) {
                crossbow = stored;
            }
        }
        // Hold the very same stack that lives in the block's inventory, so charging, damage,
        // enchantments and the loaded projectiles all stay on the real item.
        if (this.getMainHandItem() != crossbow) {
            this.setItemInHand(InteractionHand.MAIN_HAND, crossbow);
        }

        boolean hasCrossbow = !crossbow.isEmpty();
        this.getEntityData().set(HAS_CROSSBOW, hasCrossbow);
        float progress = 0.0F;
        if (hasCrossbow) {
            if (CrossbowItem.isCharged(crossbow)) {
                progress = 1.0F;
            } else if (this.isUsingItem()) {
                progress = Mth.clamp(this.getTicksUsingItem() / (float) Math.max(1, CrossbowItem.getChargeDuration(crossbow, this)), 0.0F, 1.0F);
            }
        }
        this.getEntityData().set(CHARGE_PROGRESS, progress);
    }

    public Optional<BallistaBlockEntity> getBallistaBlockEntity() {
        Level level = this.level();
        return level.getBlockEntity(this.blockPosition().below(), ModBlockEntityTypes.BALLISTA_BLOCK_ENTITY.get());
    }

    @Override
    public int getMaxHeadXRot() {
        return 60;
    }

    @Override
    public int getMaxHeadYRot() {
        return 180;
    }

    @Override
    public BlockState attachedBlockState() {
        return ModBlocks.BALLISTA_BLOCK.get().defaultBlockState();
    }

    @Override
    public ParticleOptions breakParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, attachedBlockState());
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource damageSource) {
        return ModSoundEvents.METAL_HIT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR;
    }

    /** Whether a crossbow is installed, as synced to the client (used by the model). */
    public boolean isCrossbowInstalled() {
        return this.getEntityData().get(HAS_CROSSBOW);
    }

    /** 0 when relaxed, 1 when fully drawn or loaded. */
    public float getChargeProgress() {
        return this.getEntityData().get(CHARGE_PROGRESS);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        BlockPos currentPos = this.blockPosition();
        BlockPos blockPosBelow = currentPos.below();
        Level level = this.level();
        if (level.getBlockEntity(blockPosBelow) instanceof BallistaBlockEntity ballistaBlockEntity) {
            MenuHelper.openMenu(player, new SimpleMenuProvider(ballistaBlockEntity, ballistaBlockEntity.getDisplayName()), blockPosBelow);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void die(DamageSource damageSource) {
        BlockPos currentPos = this.blockPosition();
        BlockPos blockPosBelow = currentPos.below();
        Level level = this.level();
        if (level.getBlockEntity(blockPosBelow) instanceof BallistaBlockEntity) {
            level.destroyBlock(blockPosBelow, false);
        }
        super.die(damageSource);
        this.discard();
    }

    public boolean hasCrossbow() {
        return getBallistaBlockEntity()
                .map(be -> be.inventory.getItem(BallistaBlockEntity.CROSSBOW_SLOT).getItem() instanceof CrossbowItem)
                .orElse(false);
    }

    /** Ammo slots that hold something the given weapon can fire. */
    private List<ItemStack> findProjectiles(ItemStack weapon) {
        List<ItemStack> candidates = new ArrayList<>();
        if (!(weapon.getItem() instanceof ProjectileWeaponItem weaponItem)) {
            return candidates;
        }
        Predicate<ItemStack> supported = weaponItem.getAllSupportedProjectiles();
        getBallistaBlockEntity().ifPresent(be -> {
            for (int i = 0; i < BallistaBlockEntity.CROSSBOW_SLOT; i++) {
                ItemStack stack = be.inventory.getItem(i);
                if (!stack.isEmpty() && supported.test(stack)) {
                    candidates.add(stack);
                }
            }
        });
        return candidates;
    }

    public boolean hasProjectile(ItemStack weapon) {
        return !findProjectiles(weapon).isEmpty();
    }

    /**
     * The crossbow draws its ammunition through this method when it loads, so picking a random
     * stack here makes the ballista fire a random projectile from its inventory.
     */
    @Override
    public ItemStack getProjectile(ItemStack weapon) {
        List<ItemStack> candidates = findProjectiles(weapon);
        if (candidates.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return candidates.get(this.getRandom().nextInt(candidates.size()));
    }

    @Override
    public void setChargingCrossbow(boolean charging) {
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    public static final double ATTACK_RANGE = 32.0;
    private static final float ARROW_POWER = 3.15F;
    private static final float ROCKET_POWER = 1.6F;
    private static final float INACCURACY = 0.25F;

    private static boolean firesRocket(ItemStack weapon) {
        ChargedProjectiles loaded = weapon.get(DataComponents.CHARGED_PROJECTILES);
        return loaded != null && loaded.contains(Items.FIREWORK_ROCKET);
    }

    /** Aim solution for the loaded (or, while charging, the expected) projectile, leading the target. */
    public BallistaAiming.Solution solveAim(LivingEntity target) {
        if (firesRocket(this.getMainHandItem())) {
            return BallistaAiming.solve(this, target, ROCKET_POWER, BallistaAiming.ROCKET_GRAVITY, BallistaAiming.ROCKET_DRAG);
        }
        return BallistaAiming.solve(this, target, ARROW_POWER, BallistaAiming.ARROW_GRAVITY, BallistaAiming.ARROW_DRAG);
    }

    /**
     * Fires the loaded crossbow through its own item logic, so multishot, piercing, durability and
     * any modded crossbow or enchantment behaviour all apply. The crossbow shoots along the
     * ballista's view, which is pointed at the solved (leading, arcing) aim first.
     */
    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        ItemStack weapon = this.getMainHandItem();
        if (!(weapon.getItem() instanceof CrossbowItem crossbow) || !CrossbowItem.isCharged(weapon)) {
            return;
        }
        BallistaAiming.Solution aim = solveAim(target);
        this.setYHeadRot(aim.yRot());
        this.yHeadRotO = aim.yRot();
        this.setXRot(aim.xRot());
        this.xRotO = aim.xRot();
        float power = firesRocket(weapon) ? ROCKET_POWER : ARROW_POWER;
        crossbow.performShooting(this.level(), this, InteractionHand.MAIN_HAND, weapon, power, INACCURACY, null);
        getBallistaBlockEntity().ifPresent(be -> be.inventory.setChanged());
        this.onCrossbowAttackPerformed();
    }
}

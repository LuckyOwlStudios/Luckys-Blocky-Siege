package net.luckystudios.entity.custom.turrets.ballista;

import net.luckystudios.blocks.custom.turret.ballista.BallistaBlockEntity;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.init.ModBlocks;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.entity.custom.turrets.AbstractTurret;
import net.luckystudios.init.ModSoundEvents;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BallistaEntity extends AbstractTurret implements CrossbowAttackMob {

    private static final EntityDataAccessor<Integer> COOLDOWN = SynchedEntityData.defineId(BallistaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CROSSBOW_STATE = SynchedEntityData.defineId(BallistaEntity.class, EntityDataSerializers.INT);

    public BallistaEntity(EntityType<BallistaEntity> entityType, Level level) {
        super(entityType, level);
    }

    public BallistaEntity(Level level, BlockPos pos) {
        super(ModEntityTypes.BALLISTA.get(), level, pos);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COOLDOWN, 0);
        builder.define(CROSSBOW_STATE, CrossbowState.UNCHARGED.ordinal());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0F)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ARMOR, 4.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new StationaryCrossbowAttackGoal<>(this));
        // Increased look range to match targeting range
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 64.0F, 1.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Mob.class, 64.0F));

        // FIXED: Remove randomInterval for immediate targeting, set mustSee to false for longer range
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 1, false, false, null));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 1, false, false, (entity) -> entity instanceof Enemy));
    }

    @Override
    public void tick() {
        super.tick();
        Level level = this.level();
        if (level.getBlockEntity(this.blockPosition().below()) instanceof BallistaBlockEntity ballistaBlockEntity) {
            ItemStack itemStack = ballistaBlockEntity.inventory.getStackInSlot(9);
            if (itemStack.getItem() instanceof CrossbowItem crossbowItem) {
                setItemInHand(InteractionHand.MAIN_HAND, itemStack);
            }
        }
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

    public int getChargeTime() {
        return this.getEntityData().get(COOLDOWN);
    }

    public int getCrossbowState() {
        return this.getEntityData().get(CROSSBOW_STATE);
    }

    public void setCrossbowState(int crossbowState) {
        this.getEntityData().set(CROSSBOW_STATE, crossbowState);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        BlockPos currentPos = this.blockPosition();
        BlockPos blockPosBelow = currentPos.below();
        Level level = this.level();
        if (level.getBlockEntity(blockPosBelow) instanceof BallistaBlockEntity ballistaBlockEntity) {
            player.openMenu(new SimpleMenuProvider(ballistaBlockEntity, ballistaBlockEntity.getDisplayName()), blockPosBelow);
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
        if (!(level().getBlockEntity(blockPosition().below()) instanceof BallistaBlockEntity ballistaBlockEntity)) {
            return false;
        }
        return ballistaBlockEntity.inventory.getStackInSlot(9).getItem() instanceof CrossbowItem;
    }

    @Override
    public void setChargingCrossbow(boolean b) {

    }

    @Override
    public void onCrossbowAttackPerformed() {

    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float v) {

    }

    enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}

package net.luckystudios.entity.custom.potion_blob;

import com.mojang.logging.LogUtils;
import net.luckystudios.entity.ImprovedProjectile;
import net.luckystudios.init.ModEntityTypes;
import net.luckystudios.init.ModParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PotionBlob extends ImprovedProjectile {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = (p_350140_) -> p_350140_.isSensitiveToWater() || p_350140_.isOnFire();
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PotionBlob.class, EntityDataSerializers.INT);
    public PotionContents potionContents = PotionContents.EMPTY;

    public PotionBlob(EntityType<? extends ImprovedProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public PotionBlob(Level level, double x, double y, double z, PotionContents potionContents) {
        super(ModEntityTypes.POTION_BLOB.get(), x, y, z, level);
        this.potionContents = potionContents;
        this.updateColor(); // Initialize color based on potion
    }

    public PotionBlob(LivingEntity shooter, Level level, PotionContents potionContents) {
        super(ModEntityTypes.POTION_BLOB.get(), shooter, level);
        this.potionContents = potionContents;
        this.updateColor(); // Initialize color based on potion
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, -1);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        // Copied directly from AreaEffectCloud.class
        RegistryOps<Tag> registryops = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (compound.contains("potion_contents")) {
            PotionContents.CODEC
                    .parse(registryops, compound.get("potion_contents"))
                    .resultOrPartial(p_340707_ -> LOGGER.warn("Failed to parse area effect cloud potions: '{}'", p_340707_))
                    .ifPresent(this::setPotionContents);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        // Copied directly from AreaEffectCloud.class
        RegistryOps<Tag> registryops = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (!this.potionContents.equals(PotionContents.EMPTY)) {
            Tag tag = PotionContents.CODEC.encodeStart(registryops, this.potionContents).getOrThrow();
            compound.put("potion_contents", tag);
        }
    }

    public void setPotionContents(PotionContents potionContents) {
        this.potionContents = potionContents;
        this.updateColor();
    }

    private void updateColor() {
        int i = this.potionContents.equals(PotionContents.EMPTY) ? -1 : this.potionContents.getColor(); // Use -1 for empty
        this.entityData.set(COLOR, i);
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    @Override
    public void tick() {
        super.tick();

        // Despawn if we fall into water
        if (isInWater()) {
            this.discard();
        }

        if (isInLava() || isOnFire()) {
            playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.0F);
            if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        this.getX(), this.getY() + 1, this.getZ(),
                        10,  // particle count
                        0.25, 0.25, 0.25,  // spread (x, y, z)
                        0.05  // speed
                );
            }
            this.discard();
        }

        // Emit colored particles based on potion contents
        if (level().isClientSide) {
            // Spawn a few particles around the blob
            for (int i = 0; i < 2; i++) {
                double offsetX = (random.nextDouble() - 0.5) * 0.3;
                double offsetY = (random.nextDouble() - 0.5) * 0.3;
                double offsetZ = (random.nextDouble() - 0.5) * 0.3;

                level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, getColor()),
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0);
                level().addParticle(ColorParticleOption.create(ModParticleTypes.WATER_TRAIL.get(), getColor()),
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        0, 0, 0);
            }
        }
    }

    @Override
    protected List<ParticleOptions> getTrailParticles() {
        return List.of();
    }

    // Copied from ThrownPotion
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            PotionContents potioncontents = this.potionContents;
            if (potioncontents.is(Potions.WATER)) {
                this.applyWater();
            } else if (potioncontents.hasEffects()) {
                this.applySplash(potioncontents.getAllEffects(), result.getType() == HitResult.Type.ENTITY ? ((EntityHitResult)result).getEntity() : null);
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                ParticleOptions splashParticle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, potioncontents.getColor());
                serverLevel.sendParticles(splashParticle,
                        this.getX(), this.getY(), this.getZ(),
                        30,  // particle count
                        0.5, 0.5, 0.5,  // spread (x, y, z) - much larger spread
                        0.1  // speed - higher speed for faster scattering
                );
            }

            this.discard();
        }
    }

    // Copied from ThrownPotion
    private void applyWater() {
        AABB aabb = this.getBoundingBox().inflate(4.0F, 2.0F, 4.0F);

        for(LivingEntity livingentity : this.level().getEntitiesOfClass(LivingEntity.class, aabb, WATER_SENSITIVE_OR_ON_FIRE)) {
            double d0 = this.distanceToSqr(livingentity);
            if (d0 < (double)16.0F) {
                if (livingentity.isSensitiveToWater()) {
                    livingentity.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 1.0F);
                }

                if (livingentity.isOnFire() && livingentity.isAlive()) {
                    livingentity.extinguishFire();
                }
            }
        }

        for(Axolotl axolotl : this.level().getEntitiesOfClass(Axolotl.class, aabb)) {
            axolotl.rehydrate();
        }
    }

    // Copied from ThrownPotion
    private void applySplash(Iterable<MobEffectInstance> effects, @Nullable Entity p_entity) {
        AABB aabb = this.getBoundingBox().inflate(4.0F, 2.0F, 4.0F);
        List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
        if (!list.isEmpty()) {
            Entity entity = this.getEffectSource();

            for(LivingEntity livingentity : list) {
                if (livingentity.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr(livingentity);
                    if (d0 < (double)16.0F) {
                        double d1;
                        if (livingentity == p_entity) {
                            d1 = (double)1.0F;
                        } else {
                            d1 = (double)1.0F - Math.sqrt(d0) / (double)4.0F;
                        }

                        for(MobEffectInstance mobeffectinstance : effects) {
                            Holder<MobEffect> holder = mobeffectinstance.getEffect();
                            if (holder.value().isInstantenous()) {
                                holder.value().applyInstantenousEffect(this, this.getOwner(), livingentity, mobeffectinstance.getAmplifier(), d1);
                            } else {
                                int i = mobeffectinstance.mapDuration((p_267930_) -> (int)(d1 * (double)p_267930_ + (double)0.5F));
                                MobEffectInstance mobeffectinstance1 = new MobEffectInstance(holder, i, mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible());
                                if (!mobeffectinstance1.endsWithin(20)) {
                                    livingentity.addEffect(mobeffectinstance1, entity);
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

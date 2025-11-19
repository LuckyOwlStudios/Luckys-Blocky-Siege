package net.luckystudios.init;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.FallingFireworkRocketEntity;
import net.luckystudios.entity.custom.ember.Ember;
import net.luckystudios.entity.custom.liquid_projectile.SnowProjectile;
import net.luckystudios.entity.custom.new_boats.SloopEntity;
import net.luckystudios.entity.custom.potion_blob.PotionBlob;
import net.luckystudios.entity.custom.turrets.ballista.BallistaEntity;
import net.luckystudios.entity.custom.bullet.FireworkStarProjectile;
import net.luckystudios.entity.custom.cannonball.types.explosive_barrel.ExplosiveKeg;
import net.luckystudios.entity.custom.cannonball.types.spreading.fire_bomb.FireBomb;
import net.luckystudios.entity.custom.cannonball.types.spreading.frost_bomb.FrostBomb;
import net.luckystudios.entity.custom.cannonball.types.normal.CannonBall;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrel;
import net.luckystudios.entity.custom.seat.Seat;
import net.luckystudios.entity.custom.water_drop.WaterBlob;
import net.luckystudios.entity.custom.wooden_shrapnel.WoodenShrapnel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, BlockySiege.MOD_ID);

    public static final Supplier<EntityType<Seat>> SEAT =
            ENTITY_TYPES.register("seat", () -> EntityType.Builder.<Seat>of(Seat::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(4)
                    .build("seat"));

    public static final Supplier<EntityType<CannonBall>> CANNONBALL =
            ENTITY_TYPES.register("cannonball", () -> EntityType.Builder.<CannonBall>of(CannonBall::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .build("cannonball"));

    public static final Supplier<EntityType<ExplosiveKeg>> EXPLOSIVE_KEG =
            ENTITY_TYPES.register("explosive_keg", () -> EntityType.Builder.<ExplosiveKeg>of(ExplosiveKeg::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .build("explosive_keg"));

    public static final Supplier<EntityType<FireBomb>> FIRE_BOMB =
            ENTITY_TYPES.register("fire_bomb", () -> EntityType.Builder.<FireBomb>of(FireBomb::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .build("fire_bomb"));

    public static final Supplier<EntityType<FrostBomb>> FROST_BOMB =
            ENTITY_TYPES.register("frost_bomb", () -> EntityType.Builder.<FrostBomb>of(FrostBomb::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .build("frost_bomb"));

    public static final Supplier<EntityType<CannonBall>> WIND_BOMB =
            ENTITY_TYPES.register("wind_bomb", () -> EntityType.Builder.<CannonBall>of(CannonBall::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .build("wind_bomb"));


    public static final Supplier<EntityType<PrimedExplosiveBarrel>> PRIMED_EXPLOSIVE_BARREL =
            ENTITY_TYPES.register("primed_explosive_barrel", () -> EntityType.Builder.<PrimedExplosiveBarrel>of(PrimedExplosiveBarrel::new, MobCategory.MISC)
                    .fireImmune()
                    .sized(0.98F, 0.98F)
                    .eyeHeight(0.15F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("primed_explosive_barrel"));

    public static final Supplier<EntityType<WoodenShrapnel>> WOODEN_SHRAPNEL =
            ENTITY_TYPES.register("wooden_shrapnel", () -> EntityType.Builder.<WoodenShrapnel>of(WoodenShrapnel::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .build("wooden_shrapnel"));

    public static final Supplier<EntityType<Ember>> EMBER =
            ENTITY_TYPES.register("ember", () -> EntityType.Builder.<Ember>of(Ember::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("ember"));

    public static final Supplier<EntityType<WaterBlob>> WATER_BLOB =
            ENTITY_TYPES.register("water_blob", () -> EntityType.Builder.<WaterBlob>of(WaterBlob::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("water_blob"));

    public static final Supplier<EntityType<PotionBlob>> POTION_BLOB =
            ENTITY_TYPES.register("potion_blob", () -> EntityType.Builder.<PotionBlob>of(PotionBlob::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("potion_blob"));

    public static final Supplier<EntityType<SnowProjectile>> ICE_SHARD =
            ENTITY_TYPES.register("ice_shard", () -> EntityType.Builder.<SnowProjectile>of(SnowProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .build("ice_shard"));

    public static final Supplier<EntityType<FireworkStarProjectile>> FIREWORK_STAR =
            ENTITY_TYPES.register("firework_star", () -> EntityType.Builder.<FireworkStarProjectile>of(FireworkStarProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .eyeHeight(0.125F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .setUpdateInterval(1)
                    .build("firework_star"));

    public static final Supplier<EntityType<FallingFireworkRocketEntity>> FALLING_FIREWORK =
            ENTITY_TYPES.register("falling_firework", () -> EntityType.Builder.<FallingFireworkRocketEntity>of(FallingFireworkRocketEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("falling_firework"));

    public static final Supplier<EntityType<BallistaEntity>> BALLISTA =
            ENTITY_TYPES.register("ballista", () -> EntityType.Builder.<BallistaEntity>of(BallistaEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .eyeHeight(0.5F).clientTrackingRange(10)
                    .build("ballista"));

    public static final Supplier<EntityType<SloopEntity>> SLOOP =
            ENTITY_TYPES.register("sloop", () -> EntityType.Builder.<SloopEntity>of(SloopEntity::new, MobCategory.MISC)
                    .sized(3.0F, 1.0F)
                    .eyeHeight(0.5F).clientTrackingRange(10)
                    .build("sloop"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

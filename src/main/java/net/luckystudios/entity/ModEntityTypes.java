package net.luckystudios.entity;

import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.ballista.Ballista;
import net.luckystudios.entity.custom.cannon_ball.explosive_barrel.ExplosiveKeg;
import net.luckystudios.entity.custom.cannon_ball.fire_bomb.FireBomb;
import net.luckystudios.entity.custom.cannon_ball.frost_bomb.FrostBomb;
import net.luckystudios.entity.custom.cannon_ball.normal.CannonBall;
import net.luckystudios.entity.custom.explosive_barrel.PrimedExplosiveBarrel;
import net.luckystudios.entity.custom.seat.Seat;
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

    // We need to add <CannonBall> when we are making an abstract class that extends from something!
    // We found this out when we looked at ARROW in EntityType.java
    public static final Supplier<EntityType<CannonBall>> CANNON_BALL =
            ENTITY_TYPES.register("cannon_ball", () -> EntityType.Builder.<CannonBall>of(CannonBall::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.25F)
                    .clientTrackingRange(6)
                    .updateInterval(1)
                    .build("cannon_ball"));

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

    public static final Supplier<EntityType<Ballista>> BALLISTA =
            ENTITY_TYPES.register("ballista", () -> EntityType.Builder.of(Ballista::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .noSummon()
                    .build("ballista"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}

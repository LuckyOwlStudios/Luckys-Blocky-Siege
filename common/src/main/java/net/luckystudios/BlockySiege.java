package net.luckystudios;

import com.mojang.logging.LogUtils;
import net.luckystudios.entity.custom.turrets.ballista.BallistaEntity;
import net.luckystudios.entity.custom.block_projectile.behavior.BlockProjectileBehavior;
import net.luckystudios.init.*;
import net.luckystudios.professions.ModVillagerProfessions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.slf4j.Logger;

import java.util.function.BiConsumer;

/**
 * Loader-independent entry point. NeoForge and Fabric each call {@link #init()} from their own entry point.
 */
public class BlockySiege {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "blockysiege";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // Loading these classes registers their contents. The order matters on Fabric, which registers immediately.
        BlockProjectileBehavior.bootstrap();
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTabs.init();
        ModBlockEntityTypes.init();
        ModMenuTypes.init();
        ModEntityTypes.init();
        ModSoundEvents.init();
        ModParticleTypes.init();
        ModFluids.init();
        ModVillagerProfessions.init();
        ModGameRules.init();
    }

    public static void registerEntityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> consumer) {
        consumer.accept(ModEntityTypes.BALLISTA.get(), BallistaEntity.createAttributes().build());
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}

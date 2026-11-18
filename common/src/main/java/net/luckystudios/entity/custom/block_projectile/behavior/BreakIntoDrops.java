package net.luckystudios.entity.custom.block_projectile.behavior;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.luckystudios.BlockySiege;
import net.luckystudios.entity.custom.block_projectile.BlockProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/** The block shatters on impact and drops the listed items instead of being placed. */
public record BreakIntoDrops(List<Drop> drops) implements BlockProjectileBehavior {

    public static final MapCodec<BreakIntoDrops> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Drop.CODEC.listOf().fieldOf("drops").forGetter(BreakIntoDrops::drops)
    ).apply(instance, BreakIntoDrops::new));

    /** The item is held by id so drops can name items from mods that may not be installed. */
    public record Drop(ResourceLocation item, int min, int max) {
        public static final com.mojang.serialization.Codec<Drop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Drop::item),
                com.mojang.serialization.Codec.INT.fieldOf("min").forGetter(Drop::min),
                com.mojang.serialization.Codec.INT.fieldOf("max").forGetter(Drop::max)
        ).apply(instance, Drop::new));
    }

    @Override
    public ResourceLocation typeId() {
        return BlockySiege.id("break_into_drops");
    }

    @Override
    public void onImpact(ServerLevel level, Vec3 pos, BlockProjectile projectile) {
        // Block break particles and sound
        level.levelEvent(2001, BlockPos.containing(pos), Block.getId(projectile.getBlockState()));
        for (Drop drop : drops) {
            int count = drop.min() >= drop.max() ? drop.min() : drop.min() + level.random.nextInt(drop.max() - drop.min() + 1);
            Optional<Item> item = BuiltInRegistries.ITEM.getOptional(drop.item());
            if (count <= 0 || item.isEmpty()) continue;
            ItemEntity itemEntity = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(item.get(), count));
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }
}

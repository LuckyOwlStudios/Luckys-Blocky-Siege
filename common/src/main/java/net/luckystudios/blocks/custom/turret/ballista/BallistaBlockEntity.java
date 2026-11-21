package net.luckystudios.blocks.custom.turret.ballista;

import net.luckystudios.gui.ballista.BallistaMenu;
import net.luckystudios.init.ModBlockEntityTypes;
import net.luckystudios.entity.custom.turrets.ballista.BallistaEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BallistaBlockEntity extends BlockEntity implements MenuProvider {
    public static final int INVENTORY_SIZE = 10;
    public static final int CROSSBOW_SLOT = 9;

    // Slots 0-8 hold ammo, slot 9 holds the crossbow.
    public final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            BallistaBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    public BallistaBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.BALLISTA_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.blockysiege.ballista");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BallistaMenu(i, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Same layout NeoForge's ItemStackHandler used, so existing worlds keep their items.
        CompoundTag inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, inventory.getItems(), registries);
        inventoryTag.putInt("Size", INVENTORY_SIZE);
        tag.put("inventory", inventoryTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag.getCompound("inventory"), items, registries);
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.getItems().set(i, items.get(i));
        }
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

    /** The ballista turret entity standing on this block, if any. */
    public Optional<BallistaEntity> findBallista() {
        if (this.level == null) {
            return Optional.empty();
        }
        List<BallistaEntity> ballistas = this.level.getEntitiesOfClass(BallistaEntity.class, new AABB(this.getBlockPos().above()).inflate(0.1));
        return ballistas.stream().filter(BallistaEntity::isAlive).findFirst();
    }
}

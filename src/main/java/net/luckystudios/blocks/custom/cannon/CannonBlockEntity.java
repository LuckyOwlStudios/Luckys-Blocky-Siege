package net.luckystudios.blocks.custom.cannon;

import net.luckystudios.blocks.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CannonBlockEntity extends BlockEntity {

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return super.getStackLimit(slot, stack);
        }

        // This updates all neighbors and clients when an item is added or removed. SYNCING
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public CannonBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntityTypes.CANNON_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void addCannonBall() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }
}

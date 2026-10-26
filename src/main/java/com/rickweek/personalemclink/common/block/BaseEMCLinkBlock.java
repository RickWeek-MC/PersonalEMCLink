package com.rickweek.personalemclink.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Shared logic of the three links (it used to be copy-pasted three times). */
public abstract class BaseEMCLinkBlock extends BaseEntityBlock {

    protected BaseEMCLinkBlock(Properties properties) {
        super(properties);
    }

    protected abstract BlockEntityType<? extends BaseEMCLinkBlockEntity> getBlockEntityType();

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != (Object) getBlockEntityType()) return null;
        return (lvl, pos, st, be) -> ((BaseEMCLinkBlockEntity) be).serverTick(lvl, pos, st);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof BaseEMCLinkBlockEntity link) {
            link.setOwnerUUID(player.getUUID());
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof BaseEMCLinkBlockEntity link) {
            if (link.getOwnerUUID() == null) {
                link.setOwnerUUID(player.getUUID());
            }
            if (!link.canUse(player)) {
                player.displayClientMessage(Component.translatableWithFallback(
                        "message.personalemclink.not_owner", "This EMC Link belongs to another player"), true);
                return InteractionResult.CONSUME;
            }
            player.openMenu(link, pos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BaseEMCLinkBlockEntity link) {
            link.dropInputContents(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}

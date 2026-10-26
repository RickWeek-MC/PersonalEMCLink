package com.rickweek.personalemclink.common.block;

import com.mojang.serialization.MapCodec;
import com.rickweek.personalemclink.common.ModRegistries;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RefinedEMCLinkBlock extends BaseEMCLinkBlock {
    public static final MapCodec<RefinedEMCLinkBlock> CODEC = simpleCodec(RefinedEMCLinkBlock::new);

    public RefinedEMCLinkBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends BaseEMCLinkBlockEntity> getBlockEntityType() {
        return ModRegistries.REFINED_EMC_LINK_BE.get();
    }
}

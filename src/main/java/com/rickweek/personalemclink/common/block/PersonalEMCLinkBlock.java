package com.rickweek.personalemclink.common.block;

import com.mojang.serialization.MapCodec;
import com.rickweek.personalemclink.common.ModRegistries;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PersonalEMCLinkBlock extends BaseEMCLinkBlock {
    public static final MapCodec<PersonalEMCLinkBlock> CODEC = simpleCodec(PersonalEMCLinkBlock::new);

    public PersonalEMCLinkBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends BaseEMCLinkBlockEntity> getBlockEntityType() {
        return ModRegistries.PERSONAL_EMC_LINK_BE.get();
    }
}

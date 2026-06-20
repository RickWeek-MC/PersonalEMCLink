package com.rickweek.personalemclink.common;

import com.rickweek.personalemclink.PersonalEMCLink;
import com.rickweek.personalemclink.common.block.*;
import com.rickweek.personalemclink.common.menu.BaseEMCLinkMenu;
import com.rickweek.personalemclink.common.menu.CompressedRefinedEMCLinkMenu;
import com.rickweek.personalemclink.common.menu.PersonalEMCLinkMenu;
import com.rickweek.personalemclink.common.menu.RefinedEMCLinkMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegistries {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PersonalEMCLink.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PersonalEMCLink.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PersonalEMCLink.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, PersonalEMCLink.MODID);

    // Blocks
    public static final DeferredBlock<Block> PERSONAL_EMC_LINK_BLOCK = BLOCKS.register("personal_emc_link",
            () -> new PersonalEMCLinkBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> REFINED_EMC_LINK_BLOCK = BLOCKS.register("refined_emc_link",
            () -> new RefinedEMCLinkBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> COMPRESSED_REFINED_EMC_LINK_BLOCK = BLOCKS.register("compressed_refined_emc_link",
            () -> new CompressedRefinedEMCLinkBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f)
                    .requiresCorrectToolForDrops()));

    // BlockItems
    public static final DeferredItem<BlockItem> PERSONAL_EMC_LINK_ITEM = ITEMS.register("personal_emc_link",
            () -> new BlockItem(PERSONAL_EMC_LINK_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> REFINED_EMC_LINK_ITEM = ITEMS.register("refined_emc_link",
            () -> new BlockItem(REFINED_EMC_LINK_BLOCK.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> COMPRESSED_REFINED_EMC_LINK_ITEM = ITEMS.register("compressed_refined_emc_link",
            () -> new BlockItem(COMPRESSED_REFINED_EMC_LINK_BLOCK.get(), new Item.Properties()));


    // BlockEntities
    public static final Supplier<BlockEntityType<PersonalEMCLinkBlockEntity>> PERSONAL_EMC_LINK_BE = BLOCK_ENTITIES.register("personal_emc_link",
            () -> BlockEntityType.Builder.of(PersonalEMCLinkBlockEntity::new, PERSONAL_EMC_LINK_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<RefinedEMCLinkBlockEntity>> REFINED_EMC_LINK_BE = BLOCK_ENTITIES.register("refined_emc_link",
            () -> BlockEntityType.Builder.of(RefinedEMCLinkBlockEntity::new, REFINED_EMC_LINK_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<CompressedRefinedEMCLinkBlockEntity>> COMPRESSED_REFINED_EMC_LINK_BE = BLOCK_ENTITIES.register("compressed_refined_emc_link",
            () -> BlockEntityType.Builder.of(CompressedRefinedEMCLinkBlockEntity::new, COMPRESSED_REFINED_EMC_LINK_BLOCK.get()).build(null));

    // Menus
    public static final Supplier<MenuType<PersonalEMCLinkMenu>> PERSONAL_EMC_LINK_MENU =
            MENUS.register("personal_emc_link", () ->
                    IMenuTypeExtension.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        return new PersonalEMCLinkMenu(windowId, inv, inv.player.level().getBlockEntity(pos));
                    })
            );
    public static final Supplier<MenuType<RefinedEMCLinkMenu>> REFINED_EMC_LINK_MENU =
            MENUS.register("refined_emc_link", () ->
                    IMenuTypeExtension.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        return new RefinedEMCLinkMenu(windowId, inv, inv.player.level().getBlockEntity(pos));
                    })
            );
    public static final Supplier<MenuType<CompressedRefinedEMCLinkMenu>> COMPRESSED_REFINED_EMC_LINK_MENU =
            MENUS.register("compressed_refined_emc_link", () ->
                    IMenuTypeExtension.create((windowId, inv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        return new CompressedRefinedEMCLinkMenu(windowId, inv, inv.player.level().getBlockEntity(pos));
                    })
            );

    // CreativeTab
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "personalemclink");
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EMC_TAB =
            CREATIVE_MODE_TABS.register("emc_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.personalemclink.tab"))
                    .icon(() -> PERSONAL_EMC_LINK_BLOCK.get().asItem().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(PERSONAL_EMC_LINK_BLOCK.get());
                        output.accept(REFINED_EMC_LINK_BLOCK.get());
                        output.accept(COMPRESSED_REFINED_EMC_LINK_BLOCK.get());
                    })
                    .build());

    // register
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        MENUS.register(eventBus);
    }
}

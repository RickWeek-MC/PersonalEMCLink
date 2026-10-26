package com.rickweek.personalemclink.common;

import com.rickweek.personalemclink.PersonalEMCLink;
import com.rickweek.personalemclink.common.block.CompressedRefinedEMCLinkBlock;
import com.rickweek.personalemclink.common.block.CompressedRefinedEMCLinkBlockEntity;
import com.rickweek.personalemclink.common.block.PersonalEMCLinkBlock;
import com.rickweek.personalemclink.common.block.PersonalEMCLinkBlockEntity;
import com.rickweek.personalemclink.common.block.RefinedEMCLinkBlock;
import com.rickweek.personalemclink.common.block.RefinedEMCLinkBlockEntity;
import com.rickweek.personalemclink.common.menu.CompressedRefinedEMCLinkMenu;
import com.rickweek.personalemclink.common.menu.PersonalEMCLinkMenu;
import com.rickweek.personalemclink.common.menu.RefinedEMCLinkMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRegistries {
    private ModRegistries() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PersonalEMCLink.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PersonalEMCLink.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PersonalEMCLink.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, PersonalEMCLink.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PersonalEMCLink.MODID);

    // ---------------------------------------------------------------- blocks
    // strength(hardness, resistance): same values as before (strength(3.0f) was overwritten by destroyTime/explosionResistance)

    public static final DeferredBlock<PersonalEMCLinkBlock> PERSONAL_EMC_LINK_BLOCK = BLOCKS.register("personal_emc_link",
            () -> new PersonalEMCLinkBlock(BlockBehaviour.Properties.of()
                    .strength(3.5F, 6.0F)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<RefinedEMCLinkBlock> REFINED_EMC_LINK_BLOCK = BLOCKS.register("refined_emc_link",
            () -> new RefinedEMCLinkBlock(BlockBehaviour.Properties.of()
                    .strength(4.0F, 8.0F)
                    .requiresCorrectToolForDrops()));
    public static final DeferredBlock<CompressedRefinedEMCLinkBlock> COMPRESSED_REFINED_EMC_LINK_BLOCK = BLOCKS.register("compressed_refined_emc_link",
            () -> new CompressedRefinedEMCLinkBlock(BlockBehaviour.Properties.of()
                    .strength(4.5F, 10.0F)
                    .requiresCorrectToolForDrops()));

    // ---------------------------------------------------------------- items

    public static final DeferredItem<BlockItem> PERSONAL_EMC_LINK_ITEM = ITEMS.registerSimpleBlockItem(PERSONAL_EMC_LINK_BLOCK);
    public static final DeferredItem<BlockItem> REFINED_EMC_LINK_ITEM = ITEMS.registerSimpleBlockItem(REFINED_EMC_LINK_BLOCK);
    public static final DeferredItem<BlockItem> COMPRESSED_REFINED_EMC_LINK_ITEM = ITEMS.registerSimpleBlockItem(COMPRESSED_REFINED_EMC_LINK_BLOCK);

    // ---------------------------------------------------------------- block entities

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PersonalEMCLinkBlockEntity>> PERSONAL_EMC_LINK_BE =
            BLOCK_ENTITIES.register("personal_emc_link",
                    () -> BlockEntityType.Builder.of(PersonalEMCLinkBlockEntity::new, PERSONAL_EMC_LINK_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RefinedEMCLinkBlockEntity>> REFINED_EMC_LINK_BE =
            BLOCK_ENTITIES.register("refined_emc_link",
                    () -> BlockEntityType.Builder.of(RefinedEMCLinkBlockEntity::new, REFINED_EMC_LINK_BLOCK.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompressedRefinedEMCLinkBlockEntity>> COMPRESSED_REFINED_EMC_LINK_BE =
            BLOCK_ENTITIES.register("compressed_refined_emc_link",
                    () -> BlockEntityType.Builder.of(CompressedRefinedEMCLinkBlockEntity::new, COMPRESSED_REFINED_EMC_LINK_BLOCK.get()).build(null));

    // ---------------------------------------------------------------- menus

    public static final DeferredHolder<MenuType<?>, MenuType<PersonalEMCLinkMenu>> PERSONAL_EMC_LINK_MENU =
            MENUS.register("personal_emc_link", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new PersonalEMCLinkMenu(windowId, inv, inv.player.level().getBlockEntity(data.readBlockPos()))));
    public static final DeferredHolder<MenuType<?>, MenuType<RefinedEMCLinkMenu>> REFINED_EMC_LINK_MENU =
            MENUS.register("refined_emc_link", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new RefinedEMCLinkMenu(windowId, inv, inv.player.level().getBlockEntity(data.readBlockPos()))));
    public static final DeferredHolder<MenuType<?>, MenuType<CompressedRefinedEMCLinkMenu>> COMPRESSED_REFINED_EMC_LINK_MENU =
            MENUS.register("compressed_refined_emc_link", () -> IMenuTypeExtension.create(
                    (windowId, inv, data) -> new CompressedRefinedEMCLinkMenu(windowId, inv, inv.player.level().getBlockEntity(data.readBlockPos()))));

    // ---------------------------------------------------------------- creative tab

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EMC_TAB =
            CREATIVE_MODE_TABS.register("emc_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.personalemclink.tab"))
                    .icon(() -> PERSONAL_EMC_LINK_ITEM.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(PERSONAL_EMC_LINK_ITEM.get());
                        output.accept(REFINED_EMC_LINK_ITEM.get());
                        output.accept(COMPRESSED_REFINED_EMC_LINK_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}

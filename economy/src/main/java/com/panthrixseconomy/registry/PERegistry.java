package com.panthrixseconomy.registry;

import com.panthrixseconomy.PanthrixsEconomy;
import com.panthrixseconomy.block.AtmBlock;
import com.panthrixseconomy.block.BankBlock;
import com.panthrixseconomy.item.BankCardItem;
import com.panthrixseconomy.block.BankBlockEntity;
import com.panthrixseconomy.block.TownCoreBlock;
import com.panthrixseconomy.block.TownCoreBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class PERegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PanthrixsEconomy.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PanthrixsEconomy.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, PanthrixsEconomy.MODID);

    /** Muy resistentes: solo un administrador puede romperlos (lo comprueba PEEvents). */
    private static BlockBehaviour.Properties official() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5.0F, 3600000.0F)
                .sound(SoundType.STONE).pushReaction(PushReaction.BLOCK);
    }

    public static final RegistryObject<Block> TOWN_CORE = BLOCKS.register("town_core",
            () -> new TownCoreBlock(official().lightLevel(s -> 6)));
    public static final RegistryObject<Block> BANK = BLOCKS.register("bank_counter",
            () -> new BankBlock(official()));

    /** Cajero automático: oficial (solo lo colocan administradores), se usa con la tarjeta bancaria. */
    public static final RegistryObject<Block> ATM = BLOCKS.register("atm",
            () -> new AtmBlock(official().mapColor(MapColor.METAL).sound(SoundType.METAL).lightLevel(s -> 4)));

    public static final RegistryObject<Item> TOWN_CORE_ITEM = ITEMS.register("town_core",
            () -> new BlockItem(TOWN_CORE.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BANK_ITEM = ITEMS.register("bank_counter",
            () -> new BlockItem(BANK.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ATM_ITEM = ITEMS.register("atm",
            () -> new BlockItem(ATM.get(), new Item.Properties().rarity(Rarity.RARE)));
    /** Tarjeta bancaria personal: se pide en un mostrador de banco y abre los cajeros. */
    public static final RegistryObject<Item> BANK_CARD = ITEMS.register("bank_card",
            () -> new BankCardItem(new Item.Properties().stacksTo(1)));
    /** Moneda física (₱): se deposita y se retira en los bancos. */
    public static final RegistryObject<Item> COIN = ITEMS.register("panthrix_coin",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<BlockEntityType<TownCoreBlockEntity>> TOWN_CORE_BE = BLOCK_ENTITIES.register("town_core",
            () -> BlockEntityType.Builder.of(TownCoreBlockEntity::new, TOWN_CORE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BankBlockEntity>> BANK_BE = BLOCK_ENTITIES.register("bank_counter",
            () -> BlockEntityType.Builder.of(BankBlockEntity::new, BANK.get(), ATM.get()).build(null));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(PERegistry::onCreativeTabs);
    }

    /** Bloques oficiales: solo en la pestaña de "Utilidades de operador" (no salen en la de los jugadores). Sin receta. */
    private static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(TOWN_CORE_ITEM);
            event.accept(BANK_ITEM);
            event.accept(ATM_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(COIN);
        }
    }

    private PERegistry() {
    }
}

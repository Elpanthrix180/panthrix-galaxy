package com.panthrixsgalaxy.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Libro guía de Panthrixs Galaxy.
 *
 * Es un libro escrito normal de Minecraft (no un objeto nuevo), así que se abre y se lee
 * como cualquier libro. Cada página es un texto traducible: el jugador lo ve en su idioma
 * (claves "book.panthrixsgalaxy.guide.pageN" en es_es.json y en_us.json).
 */
public final class PGGuideBook {

    /** Número de páginas: si añades una, añade también su texto en los dos idiomas. */
    public static final int PAGES = 16;

    public static ItemStack create() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "Panthrixs Galaxy");
        tag.putString("author", "Panthrix");
        tag.putBoolean("resolved", true);

        ListTag pages = new ListTag();
        for (int i = 1; i <= PAGES; i++) {
            Component page = Component.translatable("book.panthrixsgalaxy.guide.page" + i);
            pages.add(StringTag.valueOf(Component.Serializer.toJson(page)));
        }
        tag.put("pages", pages);
        return book;
    }

    private PGGuideBook() {
    }
}

package com.panthrixsgalaxy.init;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Tags propios del mod. Un tag es una "lista con nombre" que se rellena en un archivo JSON.
 * Los archivos están en data/panthrixsgalaxy/tags/blocks/.
 */
public final class ModTags {

    public static final class Blocks {
        /** Bloques que necesitan como mínimo un pico de lunarita. */
        public static final TagKey<Block> NEEDS_LUNARITE_TOOL = create("needs_lunarite_tool");
        /** Bloques que necesitan como mínimo un pico de marteíta. */
        public static final TagKey<Block> NEEDS_MARTIANITE_TOOL = create("needs_martianite_tool");
        /** Bloques que necesitan como mínimo un pico de osmio. */
        public static final TagKey<Block> NEEDS_OSMIUM_TOOL = create("needs_osmium_tool");
        /** Bloques que necesitan como mínimo un pico de xenita. */
        public static final TagKey<Block> NEEDS_XENITE_TOOL = create("needs_xenite_tool");
        /** Bloques que no dejan pasar el aire aunque no sean bloques enteros (Fase 19): paneles de cristal... */
        public static final TagKey<Block> AIRTIGHT = create("airtight");

        private static TagKey<Block> create(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(PanthrixsGalaxy.MOD_ID, name));
        }

        private Blocks() {
        }
    }

    private ModTags() {
    }
}

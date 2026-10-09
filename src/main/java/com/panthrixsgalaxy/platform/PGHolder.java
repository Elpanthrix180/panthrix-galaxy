package com.panthrixsgalaxy.platform;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Algo registrado por el mod (un bloque, un objeto, una entidad...).
 * Se pide con get() cuando ya está registrado.
 *
 * Es igual en Forge y en Fabric: así el código común no depende de ninguno de los dos.
 */
public interface PGHolder<T> extends Supplier<T> {

    @Override
    T get();

    /** Nombre con el que se registró, p. ej. panthrixsgalaxy:pg_lunarite_ingot. */
    ResourceLocation getId();

    ResourceKey<T> getKey();
}

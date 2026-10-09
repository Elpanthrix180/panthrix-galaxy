package com.panthrixsgalaxy.platform;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Lista de cosas que el mod registra en un registro de Minecraft (bloques, objetos...).
 *
 * VERSIÓN FABRIC: se apuntan al crearse y se registran de verdad con registerAll(),
 * en el orden que marca PanthrixsGalaxyFabric (primero bloques, luego objetos...).
 * La versión Forge (src/main/java/...) tiene los mismos métodos públicos.
 */
public final class PGRegistry<T> {

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final List<Entry<? extends T>> entries = new ArrayList<>();

    private PGRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public static <T> PGRegistry<T> create(ResourceKey<? extends Registry<T>> registryKey) {
        return new PGRegistry<>(registryKey);
    }

    public <I extends T> PGHolder<I> register(String name, Supplier<? extends I> supplier) {
        Entry<I> entry = new Entry<>(new ResourceLocation(PanthrixsGalaxy.MOD_ID, name), registryKey, supplier);
        entries.add(entry);
        return entry;
    }

    /** Todo lo registrado, en el orden en que se añadió. */
    public Collection<PGHolder<? extends T>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Solo Fabric: crea y registra todo lo apuntado. */
    @SuppressWarnings("unchecked")
    public void registerAll() {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (registry == null) {
            throw new IllegalStateException("No existe el registro " + registryKey.location());
        }
        for (Entry<? extends T> entry : entries) {
            entry.registerIn(registry);
        }
    }

    private static final class Entry<I> implements PGHolder<I> {

        private final ResourceLocation id;
        private final ResourceKey<I> key;
        private final Supplier<? extends I> supplier;
        private I value;

        @SuppressWarnings("unchecked")
        Entry(ResourceLocation id, ResourceKey<? extends Registry<?>> registryKey, Supplier<? extends I> supplier) {
            this.id = id;
            this.key = ResourceKey.create((ResourceKey<? extends Registry<I>>) (ResourceKey<?>) registryKey, id);
            this.supplier = supplier;
        }

        @SuppressWarnings("unchecked")
        <T> void registerIn(Registry<T> registry) {
            T created = (T) supplier.get();
            value = (I) Registry.register(registry, id, created);
        }

        @Override
        public I get() {
            if (value == null) {
                throw new IllegalStateException("Todavía no está registrado: " + id);
            }
            return value;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public ResourceKey<I> getKey() {
            return key;
        }
    }
}

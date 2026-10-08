package com.panthrixsgalaxy.platform;

import com.panthrixsgalaxy.PanthrixsGalaxy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Lista de cosas que el mod registra en un registro de Minecraft (bloques, objetos...).
 *
 * VERSIÓN FORGE: por dentro usa el DeferredRegister de Forge.
 * La versión Fabric (fabric/src/...) tiene exactamente los mismos métodos públicos.
 */
public final class PGRegistry<T> {

    private final DeferredRegister<T> register;
    private final List<PGHolder<? extends T>> entries = new ArrayList<>();

    private PGRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        this.register = DeferredRegister.create(registryKey, PanthrixsGalaxy.MOD_ID);
    }

    public static <T> PGRegistry<T> create(ResourceKey<? extends Registry<T>> registryKey) {
        return new PGRegistry<>(registryKey);
    }

    public <I extends T> PGHolder<I> register(String name, Supplier<? extends I> supplier) {
        PGHolder<I> holder = new ForgeHolder<>(register.register(name, supplier));
        entries.add(holder);
        return holder;
    }

    /** Todo lo registrado, en el orden en que se añadió. */
    public Collection<PGHolder<? extends T>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /** Solo Forge: conecta la lista con el bus de eventos del mod. */
    public void register(IEventBus modEventBus) {
        register.register(modEventBus);
    }

    @SuppressWarnings("unchecked")
    private record ForgeHolder<I>(RegistryObject<I> object) implements PGHolder<I> {

        @Override
        public I get() {
            return object.get();
        }

        @Override
        public ResourceLocation getId() {
            return object.getId();
        }

        @Override
        public ResourceKey<I> getKey() {
            return (ResourceKey<I>) object.getKey();
        }
    }
}

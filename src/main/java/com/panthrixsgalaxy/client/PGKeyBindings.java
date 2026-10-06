package com.panthrixsgalaxy.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Teclas del mod. Se pueden cambiar en Opciones > Controles > Panthrixs Galaxy.
 * (Se registran en PGClientModEvents.)
 */
public final class PGKeyBindings {

    public static final String CATEGORY = "key.categories.panthrixsgalaxy";

    /** Tecla B: abrir la mochila equipada. Con Mayús: quitarla. */
    public static final KeyMapping BACKPACK =
            new KeyMapping("key.panthrixsgalaxy.backpack", GLFW.GLFW_KEY_B, CATEGORY);

    private PGKeyBindings() {
    }
}

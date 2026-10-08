package com.panthrixsgalaxy.fabric.config;

import com.panthrixsgalaxy.PanthrixsGalaxy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Solo Fabric: configuración con los MISMOS métodos que ForgeConfigSpec de Forge.
 *
 * Así el archivo config/PGConfig.java de Forge sirve tal cual: al compilar la versión Fabric,
 * Gradle lo copia cambiando "ForgeConfigSpec" por "PGConfigSpec" (mira fabric/build.gradle).
 * Cada opción nueva se escribe una sola vez.
 *
 * El archivo (config/panthrixsgalaxy-common.toml) tiene el mismo aspecto que en Forge:
 *   [oxygen]
 *   # Oxígeno que gasta el traje cada segundo
 *   oxygenPerSecond = 1
 */
public final class PGConfigSpec {

    private final List<Value<?>> values;

    private PGConfigSpec(List<Value<?>> values) {
        this.values = values;
    }

    /** Lee el archivo (si existe) y lo vuelve a escribir con todas las opciones y sus explicaciones. */
    public void load(Path file) {
        Map<String, String> read = new HashMap<>();
        if (Files.exists(file)) {
            try {
                String section = "";
                for (String rawLine : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String line = rawLine.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("[") && line.endsWith("]")) {
                        section = line.substring(1, line.length() - 1).trim();
                        continue;
                    }
                    int equals = line.indexOf('=');
                    if (equals > 0) {
                        String key = line.substring(0, equals).trim();
                        read.put(section.isEmpty() ? key : section + "." + key, line.substring(equals + 1).trim());
                    }
                }
            } catch (IOException e) {
                PanthrixsGalaxy.LOGGER.warn("[Panthrixs Galaxy] No se pudo leer {}: {}", file, e.getMessage());
            }
        }
        for (Value<?> value : values) {
            String text = read.get(value.path);
            if (text != null) {
                value.parse(text);
            }
        }
        save(file);
    }

    private void save(Path file) {
        StringBuilder out = new StringBuilder();
        String section = null;
        for (Value<?> value : values) {
            if (!value.section.equals(section)) {
                section = value.section;
                if (!out.isEmpty()) {
                    out.append('\n');
                }
                if (!value.sectionComment.isEmpty()) {
                    out.append("# ").append(value.sectionComment).append('\n');
                }
                out.append('[').append(section).append("]\n");
            }
            if (!value.comment.isEmpty()) {
                out.append("\t# ").append(value.comment).append('\n');
            }
            if (!value.range.isEmpty()) {
                out.append("\t# ").append(value.range).append('\n');
            }
            out.append('\t').append(value.name).append(" = ").append(value.get()).append('\n');
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            PanthrixsGalaxy.LOGGER.warn("[Panthrixs Galaxy] No se pudo guardar {}: {}", file, e.getMessage());
        }
    }

    // ===== Valores (mismos nombres que en Forge) =====

    public abstract static class ConfigValue<T> {
        protected T value;

        ConfigValue(T defaultValue) {
            this.value = defaultValue;
        }

        public T get() {
            return value;
        }

        public void set(T value) {
            this.value = value;
        }
    }

    public static final class BooleanValue extends Value<Boolean> {
        BooleanValue(String section, String name, boolean defaultValue) {
            super(section, name, defaultValue);
        }

        @Override
        void parse(String text) {
            value = Boolean.parseBoolean(text);
        }
    }

    public static final class IntValue extends Value<Integer> {
        private final int min;
        private final int max;

        IntValue(String section, String name, int defaultValue, int min, int max) {
            super(section, name, defaultValue);
            this.min = min;
            this.max = max;
            this.range = "Rango: " + min + " ~ " + max;
        }

        @Override
        void parse(String text) {
            try {
                value = Math.max(min, Math.min(max, Integer.parseInt(text)));
            } catch (NumberFormatException ignored) {
                // se queda el valor por defecto
            }
        }
    }

    public static final class DoubleValue extends Value<Double> {
        private final double min;
        private final double max;

        DoubleValue(String section, String name, double defaultValue, double min, double max) {
            super(section, name, defaultValue);
            this.min = min;
            this.max = max;
            this.range = "Rango: " + min + " ~ " + max;
        }

        @Override
        void parse(String text) {
            try {
                value = Math.max(min, Math.min(max, Double.parseDouble(text)));
            } catch (NumberFormatException ignored) {
                // se queda el valor por defecto
            }
        }
    }

    private abstract static class Value<T> extends ConfigValue<T> {
        final String section;
        final String name;
        final String path;
        String comment = "";
        String sectionComment = "";
        String range = "";

        Value(String section, String name, T defaultValue) {
            super(defaultValue);
            this.section = section;
            this.name = name;
            this.path = section.isEmpty() ? name : section + "." + name;
        }

        abstract void parse(String text);
    }

    // ===== Constructor de la configuración (como ForgeConfigSpec.Builder) =====

    public static final class Builder {
        private final List<Value<?>> values = new ArrayList<>();
        private final List<String> sections = new ArrayList<>();
        private String pendingComment = "";
        private String sectionComment = "";

        public Builder comment(String comment) {
            pendingComment = comment;
            return this;
        }

        public Builder push(String section) {
            sections.add(section);
            sectionComment = pendingComment;
            pendingComment = "";
            return this;
        }

        public Builder pop() {
            sections.remove(sections.size() - 1);
            sectionComment = "";
            return this;
        }

        private String section() {
            return String.join(".", sections);
        }

        private <V extends Value<?>> V add(V value) {
            value.comment = pendingComment;
            value.sectionComment = sectionComment;
            pendingComment = "";
            values.add(value);
            return value;
        }

        public BooleanValue define(String name, boolean defaultValue) {
            return add(new BooleanValue(section(), name, defaultValue));
        }

        public IntValue defineInRange(String name, int defaultValue, int min, int max) {
            return add(new IntValue(section(), name, defaultValue, min, max));
        }

        public DoubleValue defineInRange(String name, double defaultValue, double min, double max) {
            return add(new DoubleValue(section(), name, defaultValue, min, max));
        }

        public PGConfigSpec build() {
            return new PGConfigSpec(List.copyOf(values));
        }
    }
}

package com.panthrixseconomy.data;

import java.util.List;
import java.util.Locale;

/**
 * Genera el nombre visible de un banco a partir del nombre oficial del país.
 *   Panthria          -> Banco Nacional de Panthria
 *   Reino de Aurelia  -> Banco Nacional del Reino de Aurelia
 *   República de Sol  -> Banco Nacional de la República de Sol
 * Sucursales 2, 3...  -> "... — Sucursal 2"
 */
public final class BankNames {

    public static final String INDEPENDENT = "Banco Independiente";
    public static final List<String> ARTICLES = List.of("de", "del", "de la", "de los", "de las");

    private static final List<String> MASCULINE = List.of("reino", "imperio", "principado", "ducado", "gran ducado",
            "sultanato", "estado", "condado", "califato", "emirato", "protectorado", "territorio", "archipiélago",
            "dominio", "señorío", "marquesado", "virreinato", "khanato", "janato", "país");
    private static final List<String> FEMININE = List.of("república", "federación", "confederación", "unión",
            "monarquía", "mancomunidad", "liga", "alianza", "comunidad", "provincia", "isla", "tierra", "orden",
            "corona", "nación", "región", "colonia", "marca");
    private static final List<String> MASCULINE_PLURAL = List.of("estados", "reinos", "territorios", "países");
    private static final List<String> FEMININE_PLURAL = List.of("islas", "provincias", "tierras", "repúblicas");

    /** "de", "del", "de la"... para el nombre del país. */
    public static String connector(Country country) {
        if (country.article != null && ARTICLES.contains(country.article)) {
            return country.article;
        }
        String lower = country.name.trim().toLowerCase(Locale.ROOT);
        if (lower.startsWith("el ") || lower.startsWith("la ") || lower.startsWith("los ") || lower.startsWith("las ")) {
            return "de";  // "de El Salvador" -> se resuelve en baseName
        }
        String first = lower.split("\\s+")[0];
        if (lower.startsWith("gran ducado")) {
            return "del";
        }
        if (MASCULINE.contains(first)) {
            return "del";
        }
        if (FEMININE.contains(first)) {
            return "de la";
        }
        if (MASCULINE_PLURAL.contains(first)) {
            return "de los";
        }
        if (FEMININE_PLURAL.contains(first)) {
            return "de las";
        }
        return "de";
    }

    public static String baseName(Country country) {
        if (country == null) {
            return INDEPENDENT;
        }
        String name = country.name.trim();
        String lower = name.toLowerCase(Locale.ROOT);
        if (country.article == null && lower.startsWith("el ")) {
            return "Banco Nacional del " + name.substring(3);
        }
        return "Banco Nacional " + connector(country) + " " + name;
    }

    public static String displayName(Country country, int branch) {
        String base = baseName(country);
        return branch <= 1 ? base : base + " — Sucursal " + branch;
    }

    private BankNames() {
    }
}

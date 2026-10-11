package com.panthrixseconomy.data;

public enum SettlementType {
    CITY("Ciudad"),
    TOWN("Pueblo");

    public final String label;

    SettlementType(String label) {
        this.label = label;
    }
}

package com.muguett.moonforge.item;

public enum FireMode {
    AUTO("AUTO"),
    SEMI("SEMI");

    private final String label;

    FireMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

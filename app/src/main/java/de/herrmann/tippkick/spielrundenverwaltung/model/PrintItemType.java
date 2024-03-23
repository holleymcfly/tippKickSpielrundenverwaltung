package de.herrmann.tippkick.spielrundenverwaltung.model;

import androidx.annotation.NonNull;

public enum PrintItemType {

    COMPETITION ("Wettbewerb");

    private final String name;

    PrintItemType(String s) {
        name = s;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public static PrintItemType getEnum(String value) {
        for(PrintItemType v : values()) {
            if (v.name.equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}

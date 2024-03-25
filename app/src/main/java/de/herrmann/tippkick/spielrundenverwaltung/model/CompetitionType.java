package de.herrmann.tippkick.spielrundenverwaltung.model;

import androidx.annotation.NonNull;

public enum CompetitionType {

    DFB_POKAL ("DFB-Pokal"),
    GROUP_STAGE ("Spiel mit Gruppenphase");

    private final String name;

    CompetitionType(String s) {
        name = s;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public static CompetitionType getEnum(String value) {
        for(CompetitionType v : values()) {
            if (v.name.equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}

package de.herrmann.tippkick.spielrundenverwaltung.model;

import androidx.annotation.NonNull;

public enum CompetitionType {

    DFB_POKAL ("DFB-Pokal");

    private final String name;

    CompetitionType(String s) {
        name = s;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    public boolean isValidNumberOfTeams(int numberOfTeams) {
        return numberOfTeams == 2 || numberOfTeams == 4 || numberOfTeams == 8 || numberOfTeams == 16
                || numberOfTeams == 32 || numberOfTeams == 64;
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

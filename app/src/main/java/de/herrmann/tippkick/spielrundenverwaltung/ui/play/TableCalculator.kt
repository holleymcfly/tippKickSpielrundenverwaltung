package de.herrmann.tippkick.spielrundenverwaltung.ui.play

import android.content.Context
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO

class TableCalculator(private var context: Context, private var allPairings: List<PairingDAO>,
                      private var group: Int) {

    private var pairings = mutableListOf<PairingDAO>()
    fun calculate() : List<TableEntry> {

        // Only regard pairings for the requested group.
        allPairings.forEach { pairing ->
            if (pairing.group == group) {
                pairings.add(pairing)
            }
        }

        val tableEntries: List<TableEntry> = getTableEntryForEachTeam()

        pairings.forEach { pairing ->

            if (pairing.goalsHome != -1 && pairing.goalsAway == -1) {

                val teamHome: TableEntry = getTableEntryForId(tableEntries, pairing.teamIdHome)
                val teamAway: TableEntry = getTableEntryForId(tableEntries, pairing.teamIdAway)

                teamHome.addMatchCount()
                teamAway.addMatchCount()

                if (pairing.goalsHome > pairing.goalsAway) {
                    teamHome.addWin()
                    teamAway.addLost()
                }
                else if (pairing.goalsHome < pairing.goalsAway) {
                    teamHome.addLost()
                    teamAway.addWin()
                }
                else {
                    teamHome.addDraw()
                    teamAway.addDraw()
                }

                teamHome.addGoalsShot(pairing.goalsHome)
                teamAway.addGoalsShot(pairing.goalsAway)

                teamHome.addGoalsConceded(pairing.goalsAway)
                teamAway.addGoalsConceded(pairing.goalsHome)
            }
        }

        return tableEntries
    }

    private fun getTableEntryForEachTeam(): List<TableEntry> {

        val teamIds = getTeamIds()
        val tableEntries = mutableListOf<TableEntry>()
        teamIds.forEach { teamId ->
            if (!hasTeam(tableEntries, teamId)) {
                tableEntries.add(TableEntry(context, teamId))
            }
        }

        return tableEntries
    }

    private fun hasTeam(tableEntries: List<TableEntry>, id: Int) : Boolean {

        for (tableEntry in tableEntries) {
            if (tableEntry.getTeamId() == id) {
                return true;
            }
        }

        return false;
    }

    private fun getTableEntryForId(tableEntries: List<TableEntry>, id: Int) : TableEntry {

        for (tableEntry in tableEntries) {
            if (tableEntry.getTeamId() == id) {
                return tableEntry
            }
        }

        throw RuntimeException("Failed to get table entry with id " + id)
    }

    private fun getTeamIds() : List<Int> {

        val teamIds = mutableListOf<Int>()
        pairings.forEach { pairing ->
            if (!teamIds.contains(pairing.teamIdHome)) {
                teamIds.add(pairing.teamIdHome)
            }
            if (!teamIds.contains(pairing.teamIdAway)) {
                teamIds.add(pairing.teamIdAway)
            }
        }

        return teamIds
    }
}
package de.herrmann.tippkick.spielrundenverwaltung.logic

import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO

class DrawHelper(private var teamIds: MutableList<Int>) {

    private val pairings: MutableList<PairingDAO> = mutableListOf()

    fun draw(): List<PairingDAO> {

        while (teamIds.isNotEmpty()) {
            teamIds = drawNextPairing()
        }

        return this.pairings
    }

    private fun drawNextPairing(): MutableList<Int> {

        val teamIdsCopy: MutableList<Int> = mutableListOf()
        teamIdsCopy.addAll(teamIds)

        val indexTeam1: Int = (0..<teamIdsCopy.size).random()
        val team1Id: Int = teamIdsCopy[indexTeam1]
        teamIdsCopy.removeAt(indexTeam1)

        val indexTeam2: Int = (0..<teamIdsCopy.size).random()
        val teamId2: Int = teamIdsCopy[indexTeam2]
        teamIdsCopy.removeAt(indexTeam2)

        val newPairing = PairingDAO(team1Id, teamId2)
        this.pairings.add(newPairing)
        return teamIdsCopy
    }
}
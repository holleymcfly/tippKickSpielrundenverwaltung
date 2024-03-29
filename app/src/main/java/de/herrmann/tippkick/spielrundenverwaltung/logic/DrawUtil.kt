package de.herrmann.tippkick.spielrundenverwaltung.logic

import android.content.Context
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionType
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.util.Util

class DrawUtil {
    companion object {

        fun drawCompetitionsFirstRound(competition: CompetitionDAO, context: Context,
                                      finishText: String) {

            if (CompetitionType.DFB_POKAL == competition.competitionType) {
                drawCompetitionsFirstRoundDFB(competition, context, finishText)
            }
            else if (CompetitionType.GROUP_STAGE == competition.competitionType) {
                drawCompetitionFirstRoundGroup(competition, context, finishText)
            }
        }

        private fun drawCompetitionFirstRoundGroup(competition: CompetitionDAO, context: Context,
                                           finishText: String) {

            val teamIds: MutableList<Int> = getAllTeamIds(competition)

            // Split up the teams in groups.
            val teamIdsGroup1 = fillGroupList(competition, teamIds)
            val teamIdsGroup2 = fillGroupList(competition, teamIds)
            val teamIdsGroup3 = fillGroupList(competition, teamIds)
            val teamIdsGroup4 = mutableListOf<Int>()
            val teamIdsGroup5 = mutableListOf<Int>()
            val teamIdsGroup6 = mutableListOf<Int>()
            if (competition.numberOfGroups > 3) {
                teamIdsGroup4.addAll(fillGroupList(competition, teamIds))
            }
            if (competition.numberOfGroups > 4) {
                teamIdsGroup5.addAll(fillGroupList(competition, teamIds))
            }
            if (competition.numberOfGroups > 5) {
                teamIdsGroup6.addAll(fillGroupList(competition, teamIds))
            }

            // In each group every team has to play against every other team.
            val allPairings = mutableListOf<PairingDAO>()
            allPairings.addAll(createPairingsInGroup(teamIdsGroup1, 1, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup2, 2, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup3, 3, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup4, 4, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup5, 5, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup6, 6, competition.id))

            // Insert pairings in database.
            val pairingDBAccess = PairingDBAccess()
            allPairings.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }

            // Update competition with the "is started" info.
            competition.isStarted = true
            val competitionDBAccess = CompetitionsDBAccess()
            competitionDBAccess.updateCompetition(context, competition)

            Util.showOkButtonMessage(context, finishText)
        }

        /**
         * Creates all pairings in one group (the team ids that have been passed in the list).
         * Returns a list of pairings, where each teams plays against each other team.
         */
        private fun createPairingsInGroup(teamIdsInGroup: List<Int>, group: Int, competitionId: Int):
                MutableList<PairingDAO> {

            val pairings = mutableListOf<PairingDAO>()
            for (i in 0..teamIdsInGroup.size) {

                for (j in i+1..<teamIdsInGroup.size) {

                    val pairing = PairingDAO(teamIdsInGroup[i], teamIdsInGroup[j])
                    pairing.round = 1
                    pairing.group = group
                    pairing.competitionId = competitionId

                    pairings.add(pairing)
                }
            }

            return pairings
        }

        /**
         * Returns a list of team ids. The list is as big as the required number of teams that
         * has been set in the competition definition.
         * The used team ids are remove from the given list of all team ids.
         */
        private fun fillGroupList(competition: CompetitionDAO, teamIdsCopy: MutableList<Int>):
                MutableList<Int> {

            val groupList = mutableListOf<Int>()
            for (count in 1..competition.numberOfTeamsPerGroup) {
                val indexTeam: Int = (0..<teamIdsCopy.size).random()
                groupList.add(teamIdsCopy[indexTeam])
                teamIdsCopy.removeAt(indexTeam)
            }
            return groupList
        }

        private fun drawCompetitionsFirstRoundDFB(competition: CompetitionDAO, context: Context,
                                          finishText: String) {

            val teamIds: MutableList<Int> = getAllTeamIds(competition)

            val drawHelper = DrawHelper(teamIds)
            val pairings: List<PairingDAO> = drawHelper.draw()
            pairings.forEach { pairing ->
                pairing.competitionId = competition.id
                pairing.round = 1
            }

            val pairingDBAccess = PairingDBAccess()
            pairings.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }

            competition.isStarted = true
            val competitionDBAccess = CompetitionsDBAccess()
            competitionDBAccess.updateCompetition(context, competition)

            Util.showOkButtonMessage(context, finishText)
        }

        private fun getAllTeamIds(competition: CompetitionDAO): MutableList<Int> {
            val teamIds: MutableList<Int> = mutableListOf()
            competition.teamRelations.forEach { relation -> teamIds.add(relation.teamId) }
            return teamIds
        }

        fun drawNextRoundDfb(competitionId: Int, pairings: MutableList<PairingDAO>, context: Context,
                             newRound: Int, finishText: String) {

            val teamIds: MutableList<Int> = getWinners(pairings)

            val drawHelper = DrawHelper(teamIds)
            val newPairings: List<PairingDAO> = drawHelper.draw()
            newPairings.forEach { pairing ->
                pairing.competitionId = competitionId
                pairing.round = newRound
            }

            val pairingDBAccess = PairingDBAccess()
            newPairings.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }

            Util.showOkButtonMessage(context, finishText)
        }

        private fun getWinners(pairings: MutableList<PairingDAO>): MutableList<Int> {

            val teamIds = mutableListOf<Int>()
            pairings.forEach { pairing ->
                if (pairing.goalsHome > pairing.goalsAway) {
                    teamIds.add(pairing.teamIdHome)
                }
                else {
                    teamIds.add(pairing.teamIdAway)
                }
            }
            return teamIds
        }
    }
}
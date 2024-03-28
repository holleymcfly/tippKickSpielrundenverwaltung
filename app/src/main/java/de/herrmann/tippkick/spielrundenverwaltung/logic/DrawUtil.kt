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

            if (CompetitionType.DFB_POKAL.equals(competition.competitionType)) {
                drawCompetitionsFirstRoundDFB(competition, context, finishText);
            }
            else if (CompetitionType.GROUP_STAGE.equals(competition.competitionType)) {
                drawCompetitionFirstRoundGroup(competition, context, finishText);
            }
        }

        private fun drawCompetitionFirstRoundGroup(competition: CompetitionDAO, context: Context,
                                           finishText: String) {

            val teamIds: MutableList<Int> = getAllTeamIds(competition)

            val drawHelper = DrawHelper(teamIds)
            val pairingsFirstLeg: List<PairingDAO> = drawHelper.draw()

            var currentGroup = 1
            var pairingInGroup = 1

            for (pairing in pairingsFirstLeg) {

                pairing.competitionId = competition.id
                pairing.round = 1

                pairing.group = currentGroup

                if (pairingInGroup == competition.numberOfTeamsPerGroup / 2) {
                    pairingInGroup = 1
                    currentGroup++
                }
                else {
                    pairingInGroup++
                }
            }

            val pairingsSecondLeg: List<PairingDAO> = getPairingsSecondLeg(pairingsFirstLeg)

            val pairingDBAccess = PairingDBAccess()
            pairingsFirstLeg.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }
            pairingsSecondLeg.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }

            competition.isStarted = true
            val competitionDBAccess = CompetitionsDBAccess()
            competitionDBAccess.updateCompetition(context, competition)

            Util.showOkButtonMessage(context, finishText)
        }

        fun getPairingsSecondLeg(pairings: List<PairingDAO>) : MutableList<PairingDAO> {

            val secondLegPairings = mutableListOf<PairingDAO>()

            for (pairing in pairings) {
                secondLegPairings.add(pairing.reversePairing())
            }

            return secondLegPairings
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

        fun drawNextRound(competitionId: Int, pairings: MutableList<PairingDAO>, context: Context,
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
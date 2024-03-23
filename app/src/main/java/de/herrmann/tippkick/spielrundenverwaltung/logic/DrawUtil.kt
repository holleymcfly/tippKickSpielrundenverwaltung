package de.herrmann.tippkick.spielrundenverwaltung.logic

import android.content.Context
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.util.Util

class DrawUtil {
    companion object {
        fun drawCompetitionsFirstRound(competition: CompetitionDAO, context: Context,
                                       finishText: String) {

            val teamIds: MutableList<Int> = mutableListOf();
            competition.teamRelations.forEach { relation -> teamIds.add(relation.teamId) }

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

        fun drawNextRound(competitionId: Int, pairings: MutableList<PairingDAO>, context: Context,
                          newRound: Int, finishText: String) {

            val teamIds: MutableList<Int> = getWinners(pairings);

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
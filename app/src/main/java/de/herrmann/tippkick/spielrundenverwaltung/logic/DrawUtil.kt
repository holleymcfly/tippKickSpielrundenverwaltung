package de.herrmann.tippkick.spielrundenverwaltung.logic

import android.content.Context
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionType
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.ui.play.TableCalculator
import de.herrmann.tippkick.spielrundenverwaltung.ui.play.TableEntry
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
            val teamIdsGroup7 = mutableListOf<Int>()
            val teamIdsGroup8 = mutableListOf<Int>()

            if (competition.numberOfGroups > 3) {
                teamIdsGroup4.addAll(fillGroupList(competition, teamIds))
            }
            if (competition.numberOfGroups > 4) {
                teamIdsGroup5.addAll(fillGroupList(competition, teamIds))
            }
            if (competition.numberOfGroups > 5) {
                teamIdsGroup6.addAll(fillGroupList(competition, teamIds))
            }
            if (competition.numberOfGroups > 6) {
                teamIdsGroup7.addAll(fillGroupList(competition, teamIds))
            }
            if (competition.numberOfGroups > 7) {
                teamIdsGroup8.addAll(fillGroupList(competition, teamIds))
            }

            // In each group every team has to play against every other team.
            val allPairings = mutableListOf<PairingDAO>()
            allPairings.addAll(createPairingsInGroup(teamIdsGroup1, 1, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup2, 2, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup3, 3, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup4, 4, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup5, 5, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup6, 6, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup7, 7, competition.id))
            allPairings.addAll(createPairingsInGroup(teamIdsGroup8, 8, competition.id))

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

        /**
         * The number of teams for the second round differs by the number of groups and the number
         * of teams per group.
         * Depending on that values, the following number of teams will come into the next round:
         *
         * Number Of Groups     Number of Teams Per Group           Number of Teams in Next Round
         *          3                       4                                   8
         *          3                       6                                   8
         *          4                       4                                   8
         *          4                       6                                   16
         *          5                       4                                   16
         *          5                       6                                   16
         *          6                       4                                   16
         *          6                       6                                   16
         *          8                       4                                   16
         *          8                       6                                   32
         */
        fun drawNextRoundGroupCompetition(competition: CompetitionDAO, pairings: MutableList<PairingDAO>,
                                          context: Context, newRound: Int, finishText: String) {

            if (newRound == 2) {
                drawSecondRoundGroupCompetition(
                    competition,
                    pairings,
                    context,
                    newRound,
                    finishText
                )
            }
            else {
                drawKnockoutRoundOfGroupCompetition(competition, pairings, context, newRound,
                    finishText)
            }
        }

        private fun drawKnockoutRoundOfGroupCompetition(competition: CompetitionDAO,
                                                        pairings: MutableList<PairingDAO>,
                                                        context: Context,
                                                        newRound: Int,
                                                        finishText: String) {

            val winners = getKnockoutWinners(pairings, context)

            val drawHelper = DrawHelper(winners)
            val newPairingsFirstLeg: List<PairingDAO> = drawHelper.draw()
            newPairingsFirstLeg.forEach { pairing ->
                pairing.competitionId = competition.id
                pairing.round = newRound
            }

            val allPairings = mutableListOf<PairingDAO>()
            allPairings.addAll(newPairingsFirstLeg)

            if (allPairings.size > 1) {
                // the final doesn't have a second leg
                newPairingsFirstLeg.forEach { pairing ->
                    allPairings.add(pairing.reversePairing())
                }
            }

            val pairingDBAccess = PairingDBAccess()
            allPairings.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }

            Util.showOkButtonMessage(context, finishText)
        }

        private fun getKnockoutWinners(pairings: MutableList<PairingDAO>, context: Context):
                MutableList<Int> {

            val winners = mutableListOf<Int>()

            pairings.forEach { pairing ->
                val secondLeg = getSecondLeg(pairing, pairings)

                val twoTeams = mutableListOf<PairingDAO>()
                twoTeams.add(pairing)
                twoTeams.add(secondLeg)

                TableEntry(context, pairing.teamIdHome)
                TableEntry(context, pairing.teamIdAway)

                val tableCalculator = TableCalculator(context, twoTeams, -1)
                val table = tableCalculator.calculate()

                if (!winners.contains(table[0].getTeamId())) {
                    winners.add(table[0].getTeamId())
                }
            }

            return winners
        }

        private fun getSecondLeg(pairing: PairingDAO, pairings: MutableList<PairingDAO>) : PairingDAO {

            pairings.forEach { p ->
                if (p.teamIdAway == pairing.teamIdHome && p.teamIdHome == pairing.teamIdAway) {
                    return p
                }
            }

            throw RuntimeException("Could not get second leg for pairing.")
        }

        private fun drawSecondRoundGroupCompetition(competition: CompetitionDAO,
                                                    pairings: MutableList<PairingDAO>,
                                                    context: Context, newRound: Int,
                                                    finishText: String) {

            val survivors = getSurvivors(pairings, context, competition)
            val teamIds = mutableListOf<Int>()
            survivors.forEach { survivor ->
                teamIds.add(survivor.getTeamId())
            }

            val drawHelper = DrawHelper(teamIds)
            val newPairingsFirstLeg: List<PairingDAO> = drawHelper.draw()
            newPairingsFirstLeg.forEach { pairing ->
                pairing.competitionId = competition.id
                pairing.round = newRound
            }

            val allPairings = mutableListOf<PairingDAO>()
            allPairings.addAll(newPairingsFirstLeg)

            newPairingsFirstLeg.forEach { pairing ->
                allPairings.add(pairing.reversePairing())
            }

            val pairingDBAccess = PairingDBAccess()
            allPairings.forEach { pairing ->
                pairingDBAccess.insertPairing(context, pairing)
            }

            Util.showOkButtonMessage(context, finishText)
        }

        private fun getSurvivors(pairings: MutableList<PairingDAO>, context: Context,
                                 competition: CompetitionDAO): List<TableEntry> {

            val pairingsGroup1 = Util.getPairingsForGroup(pairings, 1)
            val pairingsGroup2 = Util.getPairingsForGroup(pairings, 2)
            val pairingsGroup3 = Util.getPairingsForGroup(pairings, 3)
            val pairingsGroup4 = Util.getPairingsForGroup(pairings, 4)
            val pairingsGroup5 = Util.getPairingsForGroup(pairings, 5)
            val pairingsGroup6 = Util.getPairingsForGroup(pairings, 6)
            val pairingsGroup7 = Util.getPairingsForGroup(pairings, 7)
            val pairingsGroup8 = Util.getPairingsForGroup(pairings, 8)

            val tableCalculator1 = TableCalculator(context, pairingsGroup1, 1)
            val tableEntriesGroup1: List<TableEntry> = tableCalculator1.calculate()
            val tableCalculator2 = TableCalculator(context, pairingsGroup2, 2)
            val tableEntriesGroup2: List<TableEntry> = tableCalculator2.calculate()
            val tableCalculator3 = TableCalculator(context, pairingsGroup3, 3)
            val tableEntriesGroup3: List<TableEntry> = tableCalculator3.calculate()
            val tableCalculator4 = TableCalculator(context, pairingsGroup4, 4)
            val tableEntriesGroup4: List<TableEntry> = tableCalculator4.calculate()
            val tableCalculator5 = TableCalculator(context, pairingsGroup5, 5)
            val tableEntriesGroup5: List<TableEntry> = tableCalculator5.calculate()
            val tableCalculator6 = TableCalculator(context, pairingsGroup6, 6)
            val tableEntriesGroup6: List<TableEntry> = tableCalculator6.calculate()
            val tableCalculator7 = TableCalculator(context, pairingsGroup7, 7)
            val tableEntriesGroup7 : List<TableEntry> = tableCalculator7.calculate()
            val tableCalculator8 = TableCalculator(context, pairingsGroup8, 8)
            val tableEntriesGroup8 : List<TableEntry> = tableCalculator8.calculate()

            val survivors = mutableListOf<TableEntry>()
            if (competition.numberOfGroups == 3) {
                if (competition.numberOfTeamsPerGroup == 4 ||
                    competition.numberOfTeamsPerGroup == 6) {

                    // In both cases with three groups, the best 8 teams survive.
                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])

                    val thirdsOfGroup = mutableListOf<TableEntry>()
                    thirdsOfGroup.add(tableEntriesGroup1[2])
                    thirdsOfGroup.add(tableEntriesGroup2[2])
                    thirdsOfGroup.add(tableEntriesGroup3[2])
                    thirdsOfGroup.sorted()

                    survivors.add(thirdsOfGroup[0])
                    survivors.add(thirdsOfGroup[1])
                }
            }
            else if (competition.numberOfGroups == 4) {
                if (competition.numberOfTeamsPerGroup == 4) {

                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])
                    survivors.add(tableEntriesGroup4[0])
                    survivors.add(tableEntriesGroup4[1])
                }
                else if (competition.numberOfTeamsPerGroup == 6) {

                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup1[2])
                    survivors.add(tableEntriesGroup1[3])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup2[2])
                    survivors.add(tableEntriesGroup2[3])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])
                    survivors.add(tableEntriesGroup3[2])
                    survivors.add(tableEntriesGroup3[3])
                    survivors.add(tableEntriesGroup4[0])
                    survivors.add(tableEntriesGroup4[1])
                    survivors.add(tableEntriesGroup4[2])
                    survivors.add(tableEntriesGroup4[3])
                }
            }
            else if (competition.numberOfGroups == 5) {
                if (competition.numberOfTeamsPerGroup == 4 ||
                    competition.numberOfTeamsPerGroup == 6) {

                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup1[2])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup2[2])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])
                    survivors.add(tableEntriesGroup3[2])
                    survivors.add(tableEntriesGroup4[0])
                    survivors.add(tableEntriesGroup4[1])
                    survivors.add(tableEntriesGroup4[2])
                    survivors.add(tableEntriesGroup5[0])
                    survivors.add(tableEntriesGroup5[1])
                    survivors.add(tableEntriesGroup5[2])

                    val forthsOfGroup = mutableListOf<TableEntry>()
                    forthsOfGroup.add(tableEntriesGroup1[3])
                    forthsOfGroup.add(tableEntriesGroup2[3])
                    forthsOfGroup.add(tableEntriesGroup3[3])
                    forthsOfGroup.add(tableEntriesGroup4[3])
                    forthsOfGroup.add(tableEntriesGroup5[3])

                    forthsOfGroup.sorted()
                    survivors.add(forthsOfGroup[0])
                }
            }
            else if (competition.numberOfGroups == 6) {
                if (competition.numberOfTeamsPerGroup == 4 ||
                    competition.numberOfTeamsPerGroup == 6) {

                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup1[2])
                    survivors.add(tableEntriesGroup1[3])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup2[2])
                    survivors.add(tableEntriesGroup2[3])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])
                    survivors.add(tableEntriesGroup3[2])
                    survivors.add(tableEntriesGroup3[3])
                    survivors.add(tableEntriesGroup4[0])
                    survivors.add(tableEntriesGroup4[1])
                    survivors.add(tableEntriesGroup4[2])
                    survivors.add(tableEntriesGroup4[3])
                    survivors.add(tableEntriesGroup5[0])
                    survivors.add(tableEntriesGroup5[1])
                    survivors.add(tableEntriesGroup5[2])
                    survivors.add(tableEntriesGroup5[3])
                    survivors.add(tableEntriesGroup6[0])
                    survivors.add(tableEntriesGroup6[1])
                    survivors.add(tableEntriesGroup6[2])
                    survivors.add(tableEntriesGroup6[3])
                }
            }
            else if (competition.numberOfGroups == 8) {
                if (competition.numberOfTeamsPerGroup == 4) {
                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])
                    survivors.add(tableEntriesGroup4[0])
                    survivors.add(tableEntriesGroup4[1])
                    survivors.add(tableEntriesGroup5[0])
                    survivors.add(tableEntriesGroup5[1])
                    survivors.add(tableEntriesGroup6[0])
                    survivors.add(tableEntriesGroup6[1])
                    survivors.add(tableEntriesGroup7[0])
                    survivors.add(tableEntriesGroup7[1])
                    survivors.add(tableEntriesGroup8[0])
                    survivors.add(tableEntriesGroup8[1])
                }
                else if (competition.numberOfTeamsPerGroup == 6) {
                    survivors.add(tableEntriesGroup1[0])
                    survivors.add(tableEntriesGroup1[1])
                    survivors.add(tableEntriesGroup1[2])
                    survivors.add(tableEntriesGroup1[3])
                    survivors.add(tableEntriesGroup2[0])
                    survivors.add(tableEntriesGroup2[1])
                    survivors.add(tableEntriesGroup2[2])
                    survivors.add(tableEntriesGroup2[3])
                    survivors.add(tableEntriesGroup3[0])
                    survivors.add(tableEntriesGroup3[1])
                    survivors.add(tableEntriesGroup3[2])
                    survivors.add(tableEntriesGroup3[3])
                    survivors.add(tableEntriesGroup4[0])
                    survivors.add(tableEntriesGroup4[1])
                    survivors.add(tableEntriesGroup4[2])
                    survivors.add(tableEntriesGroup4[3])
                    survivors.add(tableEntriesGroup5[0])
                    survivors.add(tableEntriesGroup5[1])
                    survivors.add(tableEntriesGroup5[2])
                    survivors.add(tableEntriesGroup5[3])
                    survivors.add(tableEntriesGroup6[0])
                    survivors.add(tableEntriesGroup6[1])
                    survivors.add(tableEntriesGroup6[2])
                    survivors.add(tableEntriesGroup6[3])
                    survivors.add(tableEntriesGroup7[0])
                    survivors.add(tableEntriesGroup7[1])
                    survivors.add(tableEntriesGroup7[2])
                    survivors.add(tableEntriesGroup7[3])
                    survivors.add(tableEntriesGroup8[0])
                    survivors.add(tableEntriesGroup8[1])
                    survivors.add(tableEntriesGroup8[2])
                    survivors.add(tableEntriesGroup8[3])
                }
            }

            return survivors
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
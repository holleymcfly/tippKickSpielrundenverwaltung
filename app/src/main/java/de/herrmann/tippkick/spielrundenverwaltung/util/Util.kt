package de.herrmann.tippkick.spielrundenverwaltung.util

import android.app.AlertDialog
import android.content.Context
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess

class Util {

    companion object {
        fun showOkButtonMessage(context: Context, message: String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.hint)
            builder.setMessage(message)
            builder.setPositiveButton(R.string.ok) { _, _ -> }
            builder.show()
        }

        fun existsNextRound(competition: CompetitionDAO, currentRound: Int, context: Context) : Boolean {

            val pairingDBAccess = PairingDBAccess()
            val pairingsNextRound = pairingDBAccess.getPairingsForCompetition(context,
                competition.id, currentRound+1)
            return pairingsNextRound.size > 0
        }

        fun isCompetitionFinished(competitionId: Int, context: Context): Boolean {

            val pairingDBAccess = PairingDBAccess()
            val numberOfFinishedPairings = pairingDBAccess.getNumberOfFinishedPairingsForCompetition(
                context, competitionId)

            val competitionsDBAccess = CompetitionsDBAccess()
            val competition = competitionsDBAccess.getCompetitionById(context, competitionId)

            // numberOfTeams - 1 is the number of matches that are played in one complete competition
            val numberOfRequiredPairings = competition.numberOfTeams - 1

            return numberOfFinishedPairings == numberOfRequiredPairings
        }

        fun getAllTeamsList() : MutableList<String> {

            val allTeams = mutableListOf<String>()

            // 1st league
            allTeams.add("Bayer 04 Leverkusen")
            allTeams.add("Bayern München")
            allTeams.add("VfB Stuttgart")
            allTeams.add("Borussia Dortmund")
            allTeams.add("RB Leipzig")
            allTeams.add("Eintracht Frankfurt")
            allTeams.add("TSG Hoffenheim")
            allTeams.add("SC Freiburg")
            allTeams.add("FC Augsburg")
            allTeams.add("Werder Bremen")
            allTeams.add("1. FC Heidenheim")
            allTeams.add("Borussia Mönchengladbach")
            allTeams.add("VfL Wolfsburg")
            allTeams.add("1. FC Union Berlin")
            allTeams.add("VfL Bochum")
            allTeams.add("1. FC Köln")
            allTeams.add("1. FSV Mainz 05")
            allTeams.add("SV Darmstadt 98")

            // 2nd league
            allTeams.add("FC St. Pauli")
            allTeams.add("Holstein Kiel")
            allTeams.add("Hamburger SV")
            allTeams.add("Fortuna Düsseldorf")
            allTeams.add("Hannover 96")
            allTeams.add("SC Paderborn 07")
            allTeams.add("SpVgg Greuther Fürth")
            allTeams.add("1. FC Nürnberg")
            allTeams.add("Karlsruher SC")
            allTeams.add("SV Elversberg")
            allTeams.add("Hertha BSC")
            allTeams.add("1. FC Magdeburg")
            allTeams.add("SV Wehen Wiesbaden")
            allTeams.add("FC Schalke 04")
            allTeams.add("1. FC Kaiserslautern")
            allTeams.add("Hansa Rostock")
            allTeams.add("Eintracht Braunschweig")
            allTeams.add("VfL Osnabrück")

            // 3rd league
            allTeams.add("SSV Ulm 1846 Fußball")
            allTeams.add("Jahn Regensburg")
            allTeams.add("Dynamo Dresden")
            allTeams.add("Preußen Münster")
            allTeams.add("SpVgg Unterhaching")
            allTeams.add("Borussia Dortmund II")
            allTeams.add("SV Sandhausen")
            allTeams.add("Rot-Weiss Essen")
            allTeams.add("1. FC Saarbrücken")
            allTeams.add("Erzgebirge Aue")
            allTeams.add("FC Ingolstadt 04")
            allTeams.add("FC Viktoria Köln")
            allTeams.add("TSV 1860 München")
            allTeams.add("SC Verl")
            allTeams.add("Arminia Bielefeld")
            allTeams.add("Hallescher FC")
            allTeams.add("SV Waldhof Mannheim")
            allTeams.add("MSV Duisburg")
            allTeams.add("VfB Lübeck")
            allTeams.add("SC Freiburg II")

            return allTeams
        }
    }
}
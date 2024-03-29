package de.herrmann.tippkick.spielrundenverwaltung.ui.play

import android.content.Context
import de.herrmann.tippkick.spielrundenverwaltung.persistence.TeamsDBAccess

class TableEntry(context: Context, private var teamId: Int) {

    private var dbAccess = TeamsDBAccess()
    private var teamName: String = dbAccess.getTeamById(context, teamId).name
    private var numberOfMatches: Int = 0
    private var numberOfWins: Int = 0
    private var numberOfDraws: Int = 0
    private var numberOfLost: Int = 0
    private var goalsShot: Int = 0
    private var goalsConceded: Int = 0
    private var points: Int = 0

    fun getTeamId(): Int {
        return this.teamId
    }

    fun getTeamName(): String {
        return this.teamName
    }

    fun addMatchCount() {
        this.numberOfMatches++
    }

    fun getMatchCount(): Int {
        return this.numberOfMatches
    }

    fun addWin() {
        this.numberOfWins++
        this.points += 3
    }

    fun getWins(): Int {
        return this.numberOfWins
    }

    fun addDraw() {
        this.numberOfDraws++
        this.points++
    }

    fun getDraws(): Int {
        return this.numberOfDraws
    }

    fun addLost() {
        this.numberOfLost++
    }

    fun getLosts(): Int {
        return this.numberOfLost
    }

    fun addGoalsShot(count: Int) {
        this.goalsShot += count
    }

    fun getGoalsShot(): Int {
        return this.goalsShot
    }

    fun addGoalsConceded(count: Int) {
        this.goalsConceded += count
    }

    fun getGoalsConceded(): Int {
        return this.goalsConceded
    }

    fun getPoints(): Int {
        return this.points
    }
}
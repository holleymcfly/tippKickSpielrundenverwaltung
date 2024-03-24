package de.herrmann.tippkick.spielrundenverwaltung.model;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Date;

import de.herrmann.tippkick.spielrundenverwaltung.R;
import de.herrmann.tippkick.spielrundenverwaltung.persistence.TeamsDBAccess;

public class PairingDAO {
    private Integer id;
    private final Integer teamIdHome;
    private final Integer teamIdAway;
    private Integer competitionId;
    private Integer round;
    private Integer goalsHome = -1;
    private Integer goalsAway = -1;

    private Boolean isExtraTime = false;

    private Boolean isPenalty = false;

    private Date playDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public PairingDAO(Integer teamIdHome, Integer teamIdAway) {
        this.teamIdHome = teamIdHome;
        this.teamIdAway = teamIdAway;
    }

    public PairingDAO(Integer id, Integer teamIdHome, Integer teamIdAway, Integer competitionId,
                      Integer round, Integer goalsHome, Integer goalsAway, Integer extraTime,
                      Integer penalty, Date playDate) {
        this.id = id;
        this.teamIdHome = teamIdHome;
        this.teamIdAway = teamIdAway;
        this.competitionId = competitionId;
        this.round = round;
        this.goalsHome = goalsHome;
        this.goalsAway = goalsAway;
        this.isExtraTime = extraTime == 1;
        this.isPenalty = penalty == 1;
        this.playDate = playDate;
    }

    public Integer getTeamIdHome() {
        return teamIdHome;
    }

    public Integer getTeamIdAway() {
        return teamIdAway;
    }

    public Integer getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Integer competitionId) {
        this.competitionId = competitionId;
    }

    public Integer getRound() {
        return round;
    }

    public void setRound(Integer round) {
        this.round = round;
    }

    public Integer getGoalsHome() {
        return goalsHome;
    }

    public void setGoalsHome(Integer goalsHome) {
        this.goalsHome = goalsHome;
    }

    public Integer getGoalsAway() {
        return goalsAway;
    }

    public void setGoalsAway(Integer goalsAway) {
        this.goalsAway = goalsAway;
    }

    public Boolean getExtraTime() {
        return isExtraTime;
    }

    public void setExtraTime(Boolean extraTime) {
        isExtraTime = extraTime;
    }

    public Boolean getPenalty() {
        return isPenalty;
    }

    public void setPenalty(Boolean penalty) {
        isPenalty = penalty;
    }

    public Date getPlayDate() {
        return playDate;
    }

    public void setPlayDate(Date playDate) {
        this.playDate = playDate;
    }

    @NonNull
    @Override
    public String toString() {

        if (this.context == null) {
            return "ID: " + this.teamIdHome + " - ID: " + this.teamIdAway;
        }

        TeamsDBAccess teamsDBAccess = new TeamsDBAccess();
        TeamDAO homeTeam = teamsDBAccess.getTeamById(this.context, this.teamIdHome);
        TeamDAO awayTeam = teamsDBAccess.getTeamById(this.context, this.teamIdAway);

        String text = "";
        if (homeTeam != null) {
            text += homeTeam.getName();
        }
        else {
            text += context.getString(R.string.not_found);
        }
        text += " - ";

        if (awayTeam != null) {
            text += awayTeam.getName();
        }
        else {
            text += context.getString(R.string.not_found);
        }

        text += "\n";

        if (this.goalsHome > -1 && this.goalsAway > -1) {
            text += this.goalsHome + " : " + this.goalsAway;
        }
        else {
            text += "- : -";
        }

        if (isPenalty) {
            text += "  n.E.";
        }
        else if (isExtraTime) {
            text += "  n.V.";
        }

        return text;
    }

    public String toStringShort() {

        if (this.context == null) {
            return "ID: " + this.teamIdHome + " - ID: " + this.teamIdAway;
        }

        TeamsDBAccess teamsDBAccess = new TeamsDBAccess();
        TeamDAO homeTeam = teamsDBAccess.getTeamById(this.context, this.teamIdHome);
        TeamDAO awayTeam = teamsDBAccess.getTeamById(this.context, this.teamIdAway);

        String text = "";
        if (homeTeam != null) {
            text += homeTeam.getName();
        }
        else {
            text += context.getString(R.string.not_found);
        }
        text += " - ";

        if (awayTeam != null) {
            text += awayTeam.getName();
        }
        else {
            text += context.getString(R.string.not_found);
        }

        return text;
    }

    public String toResultOnly() {

        String text = "";

        if (this.goalsHome > -1 && this.goalsAway > -1) {
            text += this.goalsHome + " : " + this.goalsAway;
        }
        else {
            text += "- : -";
        }

        if (isPenalty) {
            text += "  n.E.";
        }
        else if (isExtraTime) {
            text += "  n.V.";
        }

        return text;
    }

    public boolean hasHomeGoalsSet() {
        return goalsHome > -1;
    }

    public boolean hasAwayGoalsSet() {
        return goalsAway > -1;
    }

    public boolean isFinished() {
        return hasHomeGoalsSet() && hasAwayGoalsSet();
    }
}

package de.herrmann.tippkick.spielrundenverwaltung.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompetitionDAO {

    private Integer id;
    private CompetitionType competitionType;
    private String name;
    private Integer numberOfTeams = 0;
    private Integer numberOfGroups = 0;
    private Integer numberOfTeamsPerGroup = 0;
    private boolean isStarted = false;
    private Date startedAt;

    private final List<CompetitionTeamsRelationDAO> teamRelations = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CompetitionDAO() {
    }

    public CompetitionDAO(Integer id, CompetitionType competitionType, String name,
                          Integer numberOfTeams, Integer numberOfGroups,
                          Integer numberOfTeamsPerGroup, boolean isStarted,
                          Date startedAt) {
        this.id = id;
        this.competitionType = competitionType;
        this.name = name;
        this.numberOfTeams = numberOfTeams;
        this.numberOfGroups = numberOfGroups;
        this.numberOfTeamsPerGroup = numberOfTeamsPerGroup;
        this.isStarted = isStarted;
        this.startedAt = startedAt;
    }

    public CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumberOfTeams() {
        return numberOfTeams;
    }

    public void setNumberOfTeams(Integer numberOfTeams) {
        this.numberOfTeams = numberOfTeams;
    }

    public Integer getNumberOfGroups() {
        return numberOfGroups;
    }

    public void setNumberOfGroups(Integer numberOfGroups) {
        this.numberOfGroups = numberOfGroups;
    }

    public Integer getNumberOfTeamsPerGroup() {
        return numberOfTeamsPerGroup;
    }

    public void setNumberOfTeamsPerGroup(Integer numberOfTeamsPerGroup) {
        this.numberOfTeamsPerGroup = numberOfTeamsPerGroup;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " (" + competitionType.toString() + ")";
    }

    public List<CompetitionTeamsRelationDAO> getTeamRelations() {
        return teamRelations;
    }

    public void addTeamRelations(List<CompetitionTeamsRelationDAO> relations) {
        this.teamRelations.addAll(relations);
    }
}

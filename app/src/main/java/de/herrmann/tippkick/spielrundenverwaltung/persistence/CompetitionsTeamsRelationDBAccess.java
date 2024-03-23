package de.herrmann.tippkick.spielrundenverwaltung.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionTeamsRelationDAO;

public class CompetitionsTeamsRelationDBAccess {

    public List<CompetitionTeamsRelationDAO> getCompetitionTeamRelationForCompetition(
            Context context, Integer competitionId) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_ID,
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_TEAM_ID,
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_COMPETITION_ID
        };

        String whereClause = DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_COMPETITION_ID+"=?";
        String [] whereArgs = {competitionId.toString()};

        Cursor cursor = database.query(
                DBHelper.COMPETITION_TEAMS_RELATION_TABLE_NAME,
                projection,                         // The columns to return
                whereClause,                        // The columns for the WHERE clause
                whereArgs,                          // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                null                                // sort
        );

        List<CompetitionTeamsRelationDAO> allRelations = new ArrayList<>();
        while (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            Integer teamId = cursor.getInt(1);
            Integer compId = cursor.getInt(2);

            allRelations.add(new CompetitionTeamsRelationDAO(id, compId, teamId));
        }

        cursor.close();
        return allRelations;
    }

    public List<CompetitionTeamsRelationDAO> getCompetitionTeamRelationForTeam(
            Context context, Integer teamId) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_ID,
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_TEAM_ID,
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_COMPETITION_ID
        };

        String whereClause = DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_TEAM_ID+"=?";
        String [] whereArgs = {teamId.toString()};

        Cursor cursor = database.query(
                DBHelper.COMPETITION_TEAMS_RELATION_TABLE_NAME,
                projection,                         // The columns to return
                whereClause,                        // The columns for the WHERE clause
                whereArgs,                          // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                null                                // sort
        );

        List<CompetitionTeamsRelationDAO> allRelations = new ArrayList<>();
        while (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            Integer tId = cursor.getInt(1);
            Integer compId = cursor.getInt(2);

            allRelations.add(new CompetitionTeamsRelationDAO(id, compId, tId));
        }

        cursor.close();
        return allRelations;
    }

    public void insertCompetitionTeamRelation(Context context, Integer teamId, Integer competitionId) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_TEAM_ID, teamId);
        values.put(DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_COMPETITION_ID, competitionId);

        database.insert(DBHelper.COMPETITION_TEAMS_RELATION_TABLE_NAME, null, values);
    }

    public void updateCompetitionTeamRelationsForCompetition(Context context,
                                                             Integer competitionId,
                                                             List<CompetitionTeamsRelationDAO> relations) {

        List<CompetitionTeamsRelationDAO> existingRelations = getCompetitionTeamRelationForCompetition(
                context, competitionId);

        // Delete relations that don't exist anymore.
        for (CompetitionTeamsRelationDAO existingRelation : existingRelations) {

            if (!containsTeamId(relations, existingRelation.getTeamId())) {
                deleteCompetitionTeamRelation(context, existingRelation.getId());
            }
        }

        // Insert new relations.
        for (CompetitionTeamsRelationDAO relation : relations) {

            if (!containsTeamId(existingRelations, relation.getTeamId())) {
                insertCompetitionTeamRelation(context, relation.getTeamId(), competitionId);
            }
        }
    }

    private boolean containsTeamId(List<CompetitionTeamsRelationDAO> relations, Integer teamId) {

        for (CompetitionTeamsRelationDAO relation : relations) {
            if (relation.getTeamId().equals(teamId)) {
                return true;
            }
        }

        return false;
    }

    public void deleteCompetitionTeamRelation(Context context, Integer id) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();
        database.delete(DBHelper.COMPETITION_TEAMS_RELATION_TABLE_NAME,
                DBHelper.COMPETITION_TEAMS_RELATION_COLUMN_ID + "=?",
                new String[]{id.toString()});
    }
}

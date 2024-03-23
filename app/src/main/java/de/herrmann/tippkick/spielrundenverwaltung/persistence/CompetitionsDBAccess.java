package de.herrmann.tippkick.spielrundenverwaltung.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionTeamsRelationDAO;
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionType;
import de.herrmann.tippkick.spielrundenverwaltung.R;
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO;

public class CompetitionsDBAccess {

    public long insertCompetition(Context context, CompetitionType competitionType, String name,
                                  Integer numberOfTeams, boolean startNow) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COMPETITION_COLUMN_COMPETITION_TYPE, competitionType.toString());
        values.put(DBHelper.COMPETITION_COLUMN_NAME, name);
        values.put(DBHelper.COMPETITION_COLUMN_NUMBER_OF_TEAMS, numberOfTeams);
        values.put(DBHelper.COMPETITION_COLUMN_IS_STARTED, startNow);
        if (startNow) {
            String date = DBHelper.dateToUTCString(new Date());
            values.put(DBHelper.COMPETITION_COLUMN_STARTED_AT, date);
        }

        return database.insert(DBHelper.COMPETITION_TABLE_NAME, null, values);
    }


    public CompetitionDAO getCompetitionById(Context context, Integer competitionId) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.COMPETITION_COLUMN_ID,
                DBHelper.COMPETITION_COLUMN_COMPETITION_TYPE,
                DBHelper.COMPETITION_COLUMN_NAME,
                DBHelper.COMPETITION_COLUMN_NUMBER_OF_TEAMS,
                DBHelper.COMPETITION_COLUMN_IS_STARTED,
                DBHelper.COMPETITION_COLUMN_STARTED_AT
        };

        String whereClause = DBHelper.COMPETITION_COLUMN_ID+"=?";
        String [] whereArgs = {competitionId.toString()};

        Cursor cursor = database.query(
                DBHelper.COMPETITION_TABLE_NAME,
                projection,                         // The columns to return
                whereClause,                        // The columns for the WHERE clause
                whereArgs,                          // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                DBHelper.COMPETITION_COLUMN_NAME    // sort
        );

        if (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            String competitionType = cursor.getString(1);
            String name = cursor.getString(2);
            Integer numberOfTeams = cursor.getInt(3);
            int isStartedInt = cursor.getInt(4);
            boolean isStarted = isStartedInt == 1;
            String startedAtUTC = cursor.getString(5);

            Date startedAt = null;
            if (startedAtUTC != null) {
                startedAt = DBHelper.uTCStringToDate(startedAtUTC);
            }
            cursor.close();

            CompetitionsTeamsRelationDBAccess teamsRelationDBAccess = new CompetitionsTeamsRelationDBAccess();
            List<CompetitionTeamsRelationDAO> teamRelations = teamsRelationDBAccess.
                    getCompetitionTeamRelationForCompetition(context, id);
            CompetitionDAO competition = new CompetitionDAO(id, CompetitionType.getEnum(competitionType),
                    name, numberOfTeams, isStarted, startedAt);
            competition.addTeamRelations(teamRelations);
            return competition;
        }
        else {
            cursor.close();
            return null;
        }
    }

    public List<CompetitionDAO> getAllCompetitions(Context context) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.COMPETITION_COLUMN_ID,
                DBHelper.COMPETITION_COLUMN_COMPETITION_TYPE,
                DBHelper.COMPETITION_COLUMN_NAME,
                DBHelper.COMPETITION_COLUMN_NUMBER_OF_TEAMS,
                DBHelper.COMPETITION_COLUMN_IS_STARTED,
                DBHelper.COMPETITION_COLUMN_STARTED_AT
        };

        Cursor cursor = database.query(
                DBHelper.COMPETITION_TABLE_NAME,
                projection,                         // The columns to return
                null,                               // The columns for the WHERE clause
                null,                               // The values for the WHERE clause
                null,                               // don't group the rows
                null,                               // don't filter by row groups
                DBHelper.COMPETITION_COLUMN_NAME    // sort
        );

        List<CompetitionDAO> allCompetitions = new ArrayList<>();
        while (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            String competitionType = cursor.getString(1);
            String name = cursor.getString(2);
            Integer numberOfTeams = cursor.getInt(3);
            int isStartedInt = cursor.getInt(4);
            boolean isStarted = isStartedInt == 1;
            String startedAtUTC = cursor.getString(5);

            Date startedAt = null;
            if (startedAtUTC != null) {
                startedAt = DBHelper.uTCStringToDate(startedAtUTC);
            }
            allCompetitions.add(new CompetitionDAO(id, CompetitionType.getEnum(competitionType),
                    name, numberOfTeams, isStarted, startedAt));
        }
        cursor.close();
        return allCompetitions;
    }

    public void deleteCompetition(Context context, Integer id) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();
        database.delete(DBHelper.COMPETITION_TABLE_NAME, DBHelper.COMPETITION_COLUMN_ID + "=?",
                new String[]{id.toString()});

        Toast.makeText(context, R.string.competition_deleted, Toast.LENGTH_LONG).show();
    }

    public void updateCompetition(Context context, CompetitionDAO competition) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.COMPETITION_COLUMN_COMPETITION_TYPE, competition.getCompetitionType().toString());
        values.put(DBHelper.COMPETITION_COLUMN_NAME, competition.getName());
        values.put(DBHelper.COMPETITION_COLUMN_NUMBER_OF_TEAMS, competition.getNumberOfTeams());
        values.put(DBHelper.COMPETITION_COLUMN_IS_STARTED, competition.isStarted());

        // Only change started at date if the competition shall now be started.
        if (competition.isStarted() && competition.getStartedAt() == null) {
            String startedAtUTC = DBHelper.dateToUTCString(new Date());
            values.put(DBHelper.COMPETITION_COLUMN_STARTED_AT, startedAtUTC);
        }

        database.update(DBHelper.COMPETITION_TABLE_NAME, values, DBHelper.COMPETITION_COLUMN_ID + "=?",
                new String[]{competition.getId().toString()});
    }
}

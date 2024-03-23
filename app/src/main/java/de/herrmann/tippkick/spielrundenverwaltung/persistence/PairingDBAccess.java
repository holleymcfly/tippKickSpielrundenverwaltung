package de.herrmann.tippkick.spielrundenverwaltung.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.herrmann.tippkick.spielrundenverwaltung.R;
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO;
import de.herrmann.tippkick.spielrundenverwaltung.model.TeamDAO;

public class PairingDBAccess {

    public void insertPairing(Context context, PairingDAO pairing) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.PAIRING_COLUMN_TEAM_ID_HOME, pairing.getTeamIdHome());
        values.put(DBHelper.PAIRING_COLUMN_TEAM_ID_AWAY, pairing.getTeamIdAway());
        values.put(DBHelper.PAIRING_COLUMN_COMPETITION_ID, pairing.getCompetitionId());
        values.put(DBHelper.PAIRING_COLUMN_ROUND, pairing.getRound());
        values.put(DBHelper.PAIRING_COLUMN_GOALS_HOME, pairing.getGoalsHome());
        values.put(DBHelper.PAIRING_COLUMN_GOALS_AWAY, pairing.getGoalsAway());
        values.put(DBHelper.PAIRING_COLUMN_EXTRA_TIME, pairing.getExtraTime());
        values.put(DBHelper.PAIRING_COLUMN_PENALTY, pairing.getPenalty());
        values.put(DBHelper.PAIRING_COLUMN_DATE, DBHelper.dateToUTCString(new Date()));

        database.insert(DBHelper.PAIRING_TABLE_NAME, null, values);
    }

    public List<PairingDAO> getPairingsForCompetition(Context context, Integer competitionId,
                                                      Integer round) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.PAIRING_COLUMN_ID,
                DBHelper.PAIRING_COLUMN_TEAM_ID_HOME,
                DBHelper.PAIRING_COLUMN_TEAM_ID_AWAY,
                DBHelper.PAIRING_COLUMN_COMPETITION_ID,
                DBHelper.PAIRING_COLUMN_ROUND,
                DBHelper.PAIRING_COLUMN_GOALS_HOME,
                DBHelper.PAIRING_COLUMN_GOALS_AWAY,
                DBHelper.PAIRING_COLUMN_EXTRA_TIME,
                DBHelper.PAIRING_COLUMN_PENALTY,
                DBHelper.PAIRING_COLUMN_DATE
        };

        String whereClause;
        String [] whereArgs;
        if (round == null) {
            whereClause = DBHelper.PAIRING_COLUMN_COMPETITION_ID+"=?";
            whereArgs = new String[1];
            whereArgs[0] = competitionId.toString();
        }
        else {
            whereClause = DBHelper.PAIRING_COLUMN_COMPETITION_ID+"=? AND " + DBHelper.PAIRING_COLUMN_ROUND + "=?";
            whereArgs = new String[2];
            whereArgs[0] = competitionId.toString();
            whereArgs[1] = round.toString();
        }

        Cursor cursor = database.query(
                DBHelper.PAIRING_TABLE_NAME,
                projection,                   // The columns to return
                whereClause,                  // The columns for the WHERE clause
                whereArgs,                    // The values for the WHERE clause
                null,                         // don't group the rows
                null,                         // don't filter by row groups
                null                          // sort
        );

        List<PairingDAO> allPairings = new ArrayList<>();
        while (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            Integer teamIdHome = cursor.getInt(1);
            Integer teamIdAway = cursor.getInt(2);
            Integer compId = cursor.getInt(3);
            Integer rd = cursor.getInt(4);
            Integer goalsHome = cursor.getInt(5);
            Integer goalsAway = cursor.getInt(6);
            Integer extraTime = cursor.getInt(7);
            Integer penalty = cursor.getInt(8);
            Date playDate = DBHelper.uTCStringToDate(cursor.getString(9));

            allPairings.add(new PairingDAO(id, teamIdHome, teamIdAway, compId, rd,
                    goalsHome, goalsAway, extraTime, penalty, playDate));
        }
        cursor.close();
        return allPairings;
    }

    public int getNumberOfFinishedPairingsForCompetition(Context context, Integer competitionId) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String sql = "SELECT COUNT (_id) from " + DBHelper.PAIRING_TABLE_NAME +
                " WHERE " + DBHelper.PAIRING_COLUMN_COMPETITION_ID + "=? AND " +
                DBHelper.PAIRING_COLUMN_DATE + " IS NOT NULL";

        Cursor cursor = database.rawQuery(sql, new String[] { competitionId.toString() });

        int count = 0;
        if (null != cursor) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
        }
        cursor.close();

        database.close();
        return count;
    }

    public void updatePairing(Context context, Integer pairingId, Integer goalsHome, Integer goalsAway,
                              Boolean extraTime, Boolean penalty) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.PAIRING_COLUMN_GOALS_HOME, goalsHome);
        values.put(DBHelper.PAIRING_COLUMN_GOALS_AWAY, goalsAway);
        values.put(DBHelper.PAIRING_COLUMN_EXTRA_TIME, extraTime);
        values.put(DBHelper.PAIRING_COLUMN_PENALTY, penalty);
        values.put(DBHelper.PAIRING_COLUMN_DATE, DBHelper.dateToUTCString(new Date()));

        database.update(DBHelper.PAIRING_TABLE_NAME, values, DBHelper.PAIRING_COLUMN_ID+"=?",
                new String[] { pairingId.toString() });
    }

    public void deletePairingsForCompetition(Context context, Integer competitionId) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();
        database.delete(DBHelper.PAIRING_TABLE_NAME, DBHelper.PAIRING_COLUMN_COMPETITION_ID+"=?",
                new String[] { competitionId.toString() });
    }
}

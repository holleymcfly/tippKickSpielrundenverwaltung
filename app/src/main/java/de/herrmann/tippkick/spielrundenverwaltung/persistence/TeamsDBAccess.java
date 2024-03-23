package de.herrmann.tippkick.spielrundenverwaltung.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.herrmann.tippkick.spielrundenverwaltung.R;
import de.herrmann.tippkick.spielrundenverwaltung.model.TeamDAO;

public class TeamsDBAccess {

    public void insertTeam(Context context, String name) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.TEAM_COLUMN_NAME, name);

        database.insert(DBHelper.TEAM_TABLE_NAME, null, values);
    }

    public List<TeamDAO> getAllTeams(Context context) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.TEAM_COLUMN_ID,
                DBHelper.TEAM_COLUMN_NAME
        };

        Cursor cursor = database.query(
                DBHelper.TEAM_TABLE_NAME,
                projection,                   // The columns to return
                null,                         // The columns for the WHERE clause
                null,                         // The values for the WHERE clause
                null,                         // don't group the rows
                null,                         // don't filter by row groups
                DBHelper.TEAM_COLUMN_NAME     // sort
        );

        List<TeamDAO> allTeams = new ArrayList<>();
        while (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            String name = cursor.getString(1);
            allTeams.add(new TeamDAO(id, name));
        }
        cursor.close();
        return allTeams;
    }

    public TeamDAO getTeamById(Context context, Integer teamId) {

        SQLiteDatabase database = new DBHelper(context).getReadableDatabase();

        String[] projection = {
                DBHelper.TEAM_COLUMN_ID,
                DBHelper.TEAM_COLUMN_NAME
        };

        String whereClause = DBHelper.TEAM_COLUMN_ID+"=?";
        String [] whereArgs = {teamId.toString()};

        Cursor cursor = database.query(
                DBHelper.TEAM_TABLE_NAME,
                projection,                   // The columns to return
                whereClause,                  // The columns for the WHERE clause
                whereArgs,                    // The values for the WHERE clause
                null,                         // don't group the rows
                null,                         // don't filter by row groups
                DBHelper.TEAM_COLUMN_NAME     // sort
        );

        if (cursor.moveToNext()) {
            Integer id = cursor.getInt(0);
            String name = cursor.getString(1);
            cursor.close();
            return new TeamDAO(id, name);
        }
        else {
            cursor.close();
            return null;
        }
    }

    public void deleteTeam(Context context, Integer id) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();
        database.delete(DBHelper.TEAM_TABLE_NAME, DBHelper.TEAM_COLUMN_ID+"=?",
                new String[] { id.toString() });

        Toast.makeText(context, R.string.team_deleted, Toast.LENGTH_LONG).show();
    }

    public void updateTeam(Context context, TeamDAO team) {

        SQLiteDatabase database = new DBHelper(context).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DBHelper.TEAM_COLUMN_NAME, team.getName());

        database.update(DBHelper.TEAM_TABLE_NAME, values, DBHelper.TEAM_COLUMN_ID+"=?",
                new String[] { team.getId().toString() });
    }
}

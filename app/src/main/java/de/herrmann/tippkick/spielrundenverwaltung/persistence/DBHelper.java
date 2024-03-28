package de.herrmann.tippkick.spielrundenverwaltung.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 24;
    public static final String DATABASE_NAME = "spielrundenverwaltung";

    /**
     * Table team
     */
    public static final String TEAM_TABLE_NAME = "team";
    public static final String TEAM_COLUMN_ID = "_id";
    public static final String TEAM_COLUMN_NAME = "name";

    /**
     * Table competition
     */
    public static final String COMPETITION_TABLE_NAME = "competition";
    public static final String COMPETITION_COLUMN_ID = "_id";
    public static final String COMPETITION_COLUMN_COMPETITION_TYPE = "competitionType";
    public static final String COMPETITION_COLUMN_NAME = "name";
    public static final String COMPETITION_COLUMN_NUMBER_OF_TEAMS = "numberOfTeams";
    public static final String COMPETITION_COLUMN_NUMBER_OF_GROUPS = "numberOfGroups";
    public static final String COMPETITION_COLUMN_NUMBER_OF_TEAMS_PER_GROUP = "numberOfTeamsPerGroup";
    public static final String COMPETITION_COLUMN_IS_STARTED = "isStarted";
    public static final String COMPETITION_COLUMN_STARTED_AT = "startedAt";

    /**
     * Table competitionTeamsRelation
     */
    public static final String COMPETITION_TEAMS_RELATION_TABLE_NAME = "competitionTeamsRelation";
    public static final String COMPETITION_TEAMS_RELATION_COLUMN_ID = "_id";
    public static final String COMPETITION_TEAMS_RELATION_COLUMN_TEAM_ID = "teamId";
    public static final String COMPETITION_TEAMS_RELATION_COLUMN_COMPETITION_ID = "competitionId";

    /**
     * Table pairing
     */
    public static final String PAIRING_TABLE_NAME = "pairing";
    public static final String PAIRING_COLUMN_ID = "_id";
    public static final String PAIRING_COLUMN_TEAM_ID_HOME = "teamIdHome";
    public static final String PAIRING_COLUMN_TEAM_ID_AWAY = "teamIdAway";
    public static final String PAIRING_COLUMN_COMPETITION_ID = "competitionId";
    public static final String PAIRING_COLUMN_ROUND = "round";
    public static final String PAIRING_COLUMN_GROUP = "theGroup";
    public static final String PAIRING_COLUMN_GOALS_HOME = "goalsHome";
    public static final String PAIRING_COLUMN_GOALS_AWAY = "goalsAway";
    public static final String PAIRING_COLUMN_EXTRA_TIME = "extraTime";
    public static final String PAIRING_COLUMN_PENALTY = "penalty";
    public static final String PAIRING_COLUMN_DATE = "playDate";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /*
        sqLiteDatabase.execSQL("CREATE TABLE " + TEAM_TABLE_NAME + " (" +
                TEAM_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TEAM_COLUMN_NAME + " TEXT" + ")");
        sqLiteDatabase.execSQL("CREATE TABLE " + COMPETITION_TABLE_NAME + " (" +
                COMPETITION_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COMPETITION_COLUMN_COMPETITION_TYPE + " TEXT, " +
                COMPETITION_COLUMN_NAME + " TEXT, " +
                COMPETITION_COLUMN_NUMBER_OF_TEAMS + " INTEGER, " +
                COMPETITION_COLUMN_NUMBER_OF_GROUPS + " INTEGER, " +
                COMPETITION_COLUMN_NUMBER_OF_TEAMS_PER_GROUP + " INTEGER, " +
                COMPETITION_COLUMN_IS_STARTED + " INTEGER, " +
                COMPETITION_COLUMN_STARTED_AT + " TEXT" + ")");
        sqLiteDatabase.execSQL("CREATE TABLE " + COMPETITION_TEAMS_RELATION_TABLE_NAME + " (" +
                COMPETITION_TEAMS_RELATION_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COMPETITION_TEAMS_RELATION_COLUMN_TEAM_ID + " INTEGER, " +
                COMPETITION_TEAMS_RELATION_COLUMN_COMPETITION_ID + " INTEGER" + ")");
        sqLiteDatabase.execSQL("CREATE TABLE " + PAIRING_TABLE_NAME + " (" +
                PAIRING_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PAIRING_COLUMN_TEAM_ID_HOME + " INTEGER, " +
                PAIRING_COLUMN_TEAM_ID_AWAY + " INTEGER, " +
                PAIRING_COLUMN_COMPETITION_ID + " INTEGER, " +
                PAIRING_COLUMN_ROUND + " INTEGER, " +
                PAIRING_COLUMN_GROUP + " INTEGER, " +
                PAIRING_COLUMN_GOALS_HOME + " INTEGER, " +
                PAIRING_COLUMN_GOALS_AWAY + " INTEGER," +
                PAIRING_COLUMN_EXTRA_TIME + " INTEGER," +
                PAIRING_COLUMN_PENALTY + " INTEGER, " +
                PAIRING_COLUMN_DATE + " TEXT" +  ")");
         */
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TEAM_TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + COMPETITION_TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + COMPETITION_TEAMS_RELATION_TABLE_NAME);
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PAIRING_TABLE_NAME);
//        onCreate(sqLiteDatabase);

        // VERSION 1.1
        /*
        sqLiteDatabase.execSQL("ALTER TABLE " + PAIRING_TABLE_NAME +
                " ADD " + PAIRING_COLUMN_DATE + " TEXT");
         */

        // VERSION 2.0
        /*
        sqLiteDatabase.execSQL("ALTER TABLE " + COMPETITION_TABLE_NAME +
                " ADD " + COMPETITION_COLUMN_NUMBER_OF_GROUPS + " INTEGER");
        sqLiteDatabase.execSQL("ALTER TABLE " + COMPETITION_TABLE_NAME +
                " ADD " + COMPETITION_COLUMN_NUMBER_OF_TEAMS_PER_GROUP + " INTEGER");
        sqLiteDatabase.execSQL("ALTER TABLE " + PAIRING_TABLE_NAME +
                " ADD " + PAIRING_COLUMN_GROUP + " INTEGER");
         */
    }

    public static String dateToUTCString(Date date) {

        if (date == null) {
            return null;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMANY);
        return formatter.format(new Date());
    }

    public static Date uTCStringToDate(String utcString) {

        if (utcString == null) {
            return null;
        }

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.GERMAN);
            return formatter.parse(utcString);
        }
        catch (Exception e) {
            throw new RuntimeException("Non parsable date: " + utcString);
        }
    }
}
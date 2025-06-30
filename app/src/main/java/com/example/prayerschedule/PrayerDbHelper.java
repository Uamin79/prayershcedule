package com.example.prayerschedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PrayerDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "prayer_schedule.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MOSQUE = "mosque";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";

    public static final String TABLE_ANNOUNCEMENTS = "announcements";
    public static final String COLUMN_ANNOUNCEMENT = "announcement";

    public static final String TABLE_PRAYER_TIMES = "prayer_times";
    public static final String COLUMN_PRAYER_NAME = "prayer_name";
    public static final String COLUMN_PRAYER_TIME = "prayer_time";

    private static final String CREATE_TABLE_MOSQUE =
            "CREATE TABLE " + TABLE_MOSQUE + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_ADDRESS + " TEXT);";

    private static final String CREATE_TABLE_ANNOUNCEMENTS =
            "CREATE TABLE " + TABLE_ANNOUNCEMENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ANNOUNCEMENT + " TEXT);";

    private static final String CREATE_TABLE_PRAYER_TIMES =
            "CREATE TABLE " + TABLE_PRAYER_TIMES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRAYER_NAME + " TEXT, " +
                    COLUMN_PRAYER_TIME + " TEXT);";

    public PrayerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MOSQUE);
        db.execSQL(CREATE_TABLE_ANNOUNCEMENTS);
        db.execSQL(CREATE_TABLE_PRAYER_TIMES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For simplicity, drop tables and recreate on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOSQUE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANNOUNCEMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRAYER_TIMES);
        onCreate(db);
    }
}

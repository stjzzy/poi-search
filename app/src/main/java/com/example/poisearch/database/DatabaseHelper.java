package com.example.poisearch.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "poi_search.db";
    private static final int DATABASE_VERSION = 1;

    // 收藏表
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POI_ID = "poi_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_ADD_TIME = "add_time";

    // 历史记录表
    public static final String TABLE_HISTORY = "history";
    public static final String COLUMN_KEYWORDS = "keywords";
    public static final String COLUMN_CATEGORY_CODE = "category_code";
    public static final String COLUMN_RADIUS = "radius";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";
    public static final String COLUMN_SEARCH_TIME = "search_time";
    public static final String COLUMN_RESULT_COUNT = "result_count";

    // 创建收藏表
    private static final String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_POI_ID + " TEXT UNIQUE, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_CATEGORY + " TEXT, "
            + COLUMN_ADDRESS + " TEXT, "
            + COLUMN_PHONE + " TEXT, "
            + COLUMN_LATITUDE + " REAL, "
            + COLUMN_LONGITUDE + " REAL, "
            + COLUMN_RATING + " REAL, "
            + COLUMN_ADD_TIME + " INTEGER)";

    // 创建历史记录表
    private static final String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_KEYWORDS + " TEXT, "
            + COLUMN_CATEGORY_CODE + " TEXT, "
            + COLUMN_RADIUS + " INTEGER, "
            + COLUMN_LAT + " REAL, "
            + COLUMN_LNG + " REAL, "
            + COLUMN_SEARCH_TIME + " INTEGER, "
            + COLUMN_RESULT_COUNT + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        onCreate(db);
    }
}

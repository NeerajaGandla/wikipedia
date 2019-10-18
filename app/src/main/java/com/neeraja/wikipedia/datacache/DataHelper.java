package com.neeraja.wikipedia.datacache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "wikipedia_db";
    private static DataHelper instance;

    public DataHelper(Context context) {
        super(context, DataHelper.DATABASE_NAME, null,
                DataHelper.DATABASE_VERSION);
    }

    public static synchronized DataHelper getHelper(Context context) {
        if (instance == null)
            instance = new DataHelper(context);

        return instance;
    }
    public static final String SEARCH_RESULTS_TABLE_NAME = "search_results";

    public static final String SEARCH_RESULTS_ID = "SEARCH_RESULTS_ID";
    public static final String SEARCH_RESULT_NAME = "SEARCH_RESULT_NAME";
    public static final String SEARCH_RESULT_DESCRIPTION = "SEARCH_RESULT_DESCRIPTION";
    public static final String SEARCH_RESULT_IMG_URL = "SEARCH_RESULT_IMG_URL";

    public static final String SEARCH_RESULT_CREATE_TABLE = "CREATE TABLE " + SEARCH_RESULTS_TABLE_NAME + "("
            + SEARCH_RESULTS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SEARCH_RESULT_NAME + " TEXT, " + SEARCH_RESULT_DESCRIPTION + " TEXT, "
            + SEARCH_RESULT_IMG_URL + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataHelper.SEARCH_RESULT_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

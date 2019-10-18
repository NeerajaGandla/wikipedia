package com.neeraja.wikipedia.datacache;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.neeraja.wikipedia.data.SearchResult;
import com.neeraja.wikipedia.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DataSource {
    private SQLiteDatabase database;
    private DataHelper dbHelper;
    private Context mContext;
    public SearchResultDb searchResultDb;

    public DataSource(Context context) {
        mContext = context;
        searchResultDb = new SearchResultDb();
        dbHelper = DataHelper.getHelper(mContext);
    }

    @SuppressLint("NewApi")
    public void open() throws SQLException {

        if ((database != null && !database.isOpen()) || database == null) {
            try {
                database = dbHelper.getWritableDatabase();
            }
            catch (Exception e) {
                Utils.logE(e.toString());
            }
        }
    }

    synchronized public void openRead() throws SQLException {
        if ((database != null && !database.isOpen()) || database == null) {
            database = dbHelper.getReadableDatabase();
            // isDatabaseOpen = true;
        }
    }

    public void close() {
        if ((database != null && database.isOpen())) {
        }
    }

    public class SearchResultDb {

        public int exists(String name) {
            int i = -1;
            Cursor cursor = null;
            try {
                String selectQuery = "SELECT  " + DataHelper.SEARCH_RESULTS_ID
                        + " FROM " + DataHelper.SEARCH_RESULTS_TABLE_NAME + " WHERE "
                        + DataHelper.SEARCH_RESULT_NAME + " = '" + name + "'";

                cursor = database.rawQuery(selectQuery, null);
                if (cursor.moveToFirst()) {
                    i = cursor.getInt(0);
                }
            } catch (Exception e) {
                Utils.logE(e.toString());
            } finally {
                if (cursor != null)
                    cursor.close();
            }

            return i;
        }

        public int saveSearchResult(SearchResult data) {
            Utils.logD("Insert");
            int numberOfRecords = 0;

            open();
            ContentValues values = new ContentValues();
                String name = data.getName();
                int i = exists(name);
                if (i == -1) {
                    values.put(DataHelper.SEARCH_RESULT_NAME, name);
                    values.put(DataHelper.SEARCH_RESULT_DESCRIPTION, data.getDescription());
                    values.put(DataHelper.SEARCH_RESULT_IMG_URL, data.getImageUrl());

                    long retVal = database.insert(DataHelper.SEARCH_RESULTS_TABLE_NAME, null, values);
                    if (retVal > -1)
                        numberOfRecords++;

                }
            close();
            Utils.logD("Close : " + numberOfRecords);
            return numberOfRecords;

        }

        public ArrayList<SearchResult> getMatchingCachedResults(String query) {
            ArrayList<SearchResult> searchResults = new ArrayList<>();

            openRead();

            String sql = "SELECT * FROM " + DataHelper.SEARCH_RESULTS_TABLE_NAME + " WHERE "
                    + DataHelper.SEARCH_RESULT_NAME + " LIKE '%" + query + "%'";

            Cursor cursor = database.rawQuery(sql, null);

            if (cursor.moveToFirst()) {
                int descIdx = cursor.getColumnIndex(DataHelper.SEARCH_RESULT_DESCRIPTION);
                int nameIdx = cursor
                        .getColumnIndex(DataHelper.SEARCH_RESULT_NAME);
                int imgUrlIdx = cursor.getColumnIndex(DataHelper.SEARCH_RESULT_IMG_URL);

                do {
                    SearchResult searchResult = new SearchResult();

                    String name = cursor.getString(nameIdx);
                    if (Utils.isValidString(name))
                        searchResult.setName(name);

                    String description = cursor.getString(descIdx);
                    if (Utils.isValidString(description))
                        searchResult.setDescription(description);

                    String imgUrl = cursor.getString(imgUrlIdx);
                    if (Utils.isValidString(imgUrl))
                        searchResult.setImageUrl(imgUrl);

                    if (searchResult != null)
                        searchResults.add(searchResult);
                } while (cursor.moveToNext());
            }

            cursor.close();
            close();

            return searchResults;
        }

    }

}

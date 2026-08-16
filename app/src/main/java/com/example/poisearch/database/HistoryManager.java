package com.example.poisearch.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.poisearch.model.POICategory;
import com.example.poisearch.model.SearchHistory;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private DatabaseHelper dbHelper;

    public HistoryManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加搜索历史
    public long addHistory(String keywords, String categoryCode, int radius, 
                          double lat, double lng, int resultCount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_KEYWORDS, keywords);
        values.put(DatabaseHelper.COLUMN_CATEGORY_CODE, categoryCode);
        values.put(DatabaseHelper.COLUMN_RADIUS, radius);
        values.put(DatabaseHelper.COLUMN_LAT, lat);
        values.put(DatabaseHelper.COLUMN_LNG, lng);
        values.put(DatabaseHelper.COLUMN_SEARCH_TIME, System.currentTimeMillis());
        values.put(DatabaseHelper.COLUMN_RESULT_COUNT, resultCount);
        
        return db.insert(DatabaseHelper.TABLE_HISTORY, null, values);
    }

    // 获取所有历史记录
    public List<SearchHistory> getAllHistory() {
        List<SearchHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_HISTORY, 
                null, null, null, null, null,
                DatabaseHelper.COLUMN_SEARCH_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                SearchHistory history = cursorToHistory(cursor);
                historyList.add(history);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return historyList;
    }

    // 获取最近N条历史
    public List<SearchHistory> getRecentHistory(int limit) {
        List<SearchHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_HISTORY, 
                null, null, null, null, null,
                DatabaseHelper.COLUMN_SEARCH_TIME + " DESC",
                String.valueOf(limit));
        
        if (cursor.moveToFirst()) {
            do {
                SearchHistory history = cursorToHistory(cursor);
                historyList.add(history);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return historyList;
    }

    // 删除单条历史
    public void deleteHistory(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_HISTORY, 
                DatabaseHelper.COLUMN_ID + " = ?", 
                new String[]{String.valueOf(id)});
    }

    // 清空所有历史
    public void clearAllHistory() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_HISTORY, null, null);
    }

    // 获取历史记录数量
    public int getHistoryCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_HISTORY, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private SearchHistory cursorToHistory(Cursor cursor) {
        SearchHistory history = new SearchHistory();
        history.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        history.setKeywords(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_KEYWORDS)));
        history.setCategoryCode(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY_CODE)));
        history.setRadius(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RADIUS)));
        history.setLat(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAT)));
        history.setLng(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LNG)));
        history.setSearchTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SEARCH_TIME)));
        history.setResultCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESULT_COUNT)));
        
        // 获取分类名称
        String categoryName = POICategory.getCategoryNameByCode(history.getCategoryCode());
        history.setCategoryName(categoryName.isEmpty() ? "全部" : categoryName);
        
        return history;
    }
}

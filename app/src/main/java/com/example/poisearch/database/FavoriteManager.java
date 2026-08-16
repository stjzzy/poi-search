package com.example.poisearch.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.poisearch.model.POI;

import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {

    private DatabaseHelper dbHelper;

    public FavoriteManager(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 添加收藏
    public long addFavorite(POI poi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_POI_ID, poi.getId());
        values.put(DatabaseHelper.COLUMN_NAME, poi.getName());
        values.put(DatabaseHelper.COLUMN_CATEGORY, poi.getCategory());
        values.put(DatabaseHelper.COLUMN_ADDRESS, poi.getAddress());
        values.put(DatabaseHelper.COLUMN_PHONE, poi.getPhone());
        values.put(DatabaseHelper.COLUMN_LATITUDE, poi.getLatitude());
        values.put(DatabaseHelper.COLUMN_LONGITUDE, poi.getLongitude());
        values.put(DatabaseHelper.COLUMN_RATING, poi.getRating());
        values.put(DatabaseHelper.COLUMN_ADD_TIME, System.currentTimeMillis());
        
        return db.insertWithOnConflict(DatabaseHelper.TABLE_FAVORITES, null, values, 
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    // 移除收藏
    public void removeFavorite(String poiId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_FAVORITES, 
                DatabaseHelper.COLUMN_POI_ID + " = ?", 
                new String[]{poiId});
    }

    // 检查是否已收藏
    public boolean isFavorite(String poiId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, 
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_POI_ID + " = ?",
                new String[]{poiId}, null, null, null);
        
        boolean isFav = cursor.getCount() > 0;
        cursor.close();
        return isFav;
    }

    // 获取所有收藏
    public List<POI> getAllFavorites() {
        List<POI> favorites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, 
                null, null, null, null, null,
                DatabaseHelper.COLUMN_ADD_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                POI poi = cursorToPOI(cursor);
                favorites.add(poi);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return favorites;
    }

    // 搜索收藏
    public List<POI> searchFavorites(String keyword) {
        List<POI> favorites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selection = DatabaseHelper.COLUMN_NAME + " LIKE ? OR " +
                DatabaseHelper.COLUMN_ADDRESS + " LIKE ? OR " +
                DatabaseHelper.COLUMN_CATEGORY + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, 
                null, selection, selectionArgs, null, null,
                DatabaseHelper.COLUMN_ADD_TIME + " DESC");
        
        if (cursor.moveToFirst()) {
            do {
                POI poi = cursorToPOI(cursor);
                favorites.add(poi);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return favorites;
    }

    // 清空所有收藏
    public void clearAllFavorites() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_FAVORITES, null, null);
    }

    // 获取收藏数量
    public int getFavoriteCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_FAVORITES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private POI cursorToPOI(Cursor cursor) {
        POI poi = new POI();
        poi.setId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_POI_ID)));
        poi.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)));
        poi.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
        poi.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)));
        poi.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)));
        poi.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE)));
        poi.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE)));
        poi.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RATING)));
        return poi;
    }
}

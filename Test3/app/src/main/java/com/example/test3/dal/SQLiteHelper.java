package com.example.test3.dal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.test3.model.Song;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "items.db";

    private static final int DATABASE_VERSION = 1;

    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE items(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, singer TEXT, album TEXT, genre TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    //get all
    public ArrayList<Song> getAll() {
        ArrayList<Song> list = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor rs = sqLiteDatabase
                .query("items", null, null, null, null, null, null);
        while(rs != null && rs.moveToNext()) {
            int id = rs.getInt(0);
            String name = rs.getString(1);
            String singer = rs.getString(2);
            String album = rs.getString(3);
            String genre = rs.getString(4);
            Song song = new Song(id, name, singer, album, genre);
            list.add(song);
        }
        return list;
    }

    public long addItem(Song song) {
        ContentValues values = new ContentValues();
        values.put("name", song.getName());
        values.put("singer", song.getSinger());
        values.put("album", song.getAlbum());
        values.put("genre", song.getGenre());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.insert("items", null, values);
    }

    public int updateItem(Song song) {
        ContentValues values = new ContentValues();
        values.put("name", song.getName());
        values.put("singer", song.getSinger());
        values.put("album", song.getAlbum());
        values.put("genre", song.getGenre());
        String whereClause = "id = ?";
        String[] whereArgs = {Long.toString(song.getId())};
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.update("items", values, whereClause, whereArgs);
    }

    public int deleteItem(long id) {
        String whereClause = "id = ?";
        String[] whereArgs = {Long.toString(id)};
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        return sqLiteDatabase.delete("items", whereClause, whereArgs);
    }

    public ArrayList<Song> getByGenre(String gen) {
        ArrayList<Song> list = new ArrayList<>();
        String whereClause = "genre like ?";
        String[] whereArgs = {gen};
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor rs = sqLiteDatabase
                .query("items", null, whereClause, whereArgs, null, null, null);
        while(rs != null && rs.moveToNext()) {
            int id = rs.getInt(0);
            String name = rs.getString(1);
            String singer = rs.getString(2);
            String album = rs.getString(3);
            String genre = rs.getString(4);
            list.add(new Song(id, name, singer, album, genre));
        }

        return list;
    }

    public ArrayList<Song> getByName(String keyword) {
        ArrayList<Song> list = new ArrayList<>();
        String whereClause = "name like ?";
        String[] whereArgs = {"%"+keyword+"%"};
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor rs = sqLiteDatabase.query(
                "items", null, whereClause, whereArgs, null, null, null);
        while(rs != null && rs.moveToNext()) {
            long id = rs.getInt(0);
            String name = rs.getString(1);
            String singer = rs.getString(2);
            String album = rs.getString(3);
            String genre = rs.getString(4);
            list.add(new Song(id, name, singer, album, genre));
        }
        return list;
    }
}

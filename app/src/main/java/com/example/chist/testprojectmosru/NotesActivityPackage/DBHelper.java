package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;

import java.io.File;

/**
 * Created by 1 on 27.02.2017.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static int dbVersion = 1;
    public static String dbName = "notes_db";
    public static String tableNotesName = "notetable";

    private SQLiteDatabase db;
    private static Context ctx;

    public boolean isOpen() {
        return db.isOpen();
    }

    public void open() {
        db = getWritableDatabase();
    }

    public void deleteAll() {
        db.delete(tableNotesName, null, null);
        ctx.getContentResolver().notifyChange(FirstLevelActivity.noteUri, null);
        if(new File(Utils.getImagePathInDevice().getAbsolutePath()).exists()){
            Utils.deleteAllFilesInDir(Utils.getImagePathInDevice(false)); // i don't want to delete above dirs
            Utils.deleteAllFilesInDir(Utils.getImagePathInDevice(true));
        }
    }

    public Cursor getNote(int idNote) {
        String selector = NoteColumns.ID + '=' + DatabaseUtils.sqlEscapeString(idNote+"");
        Cursor c;
        try {
            c = query(selector, Order.ID);
            c.moveToFirst();
            return c;
        } catch (Exception e) {
        }
        return null;
    }

    public Cursor query(String selection, Order order) {
        String orderBy = null;
        if (order != null)
            switch (order) {
                case ALPHABETHEADER:
                    orderBy = NoteColumns.HEADER + " COLLATE NOCASE ASC";
                    break;
                case ID:
                    orderBy = NoteColumns.ID + " ASC";
                    break;
            }

        Cursor c = db.query(tableNotesName, null, selection, null, null, null, orderBy);
        return c;
    }

    // get base columns from cursor
    public ContentValues getContentValuesFromCursor(Cursor c) {
        ContentValues cv = new ContentValues();
        cv.put(NoteColumns.HEADER, c.getString(c.getColumnIndex(DBHelper.NoteColumns.HEADER)));
        cv.put(NoteColumns.BODY, c.getString(c.getColumnIndex(DBHelper.NoteColumns.BODY)));
        cv.put(NoteColumns.MARKER, c.getInt(c.getColumnIndex(NoteColumns.MARKER)));
        cv.put(NoteColumns.TIME, System.currentTimeMillis());
        return cv;
    }

    public interface NoteColumns {
        String ID = "_id";
        String HEADER = "headerNote";
        String BODY = "textNote";
        String MARKER = "marker";
        String MAPX = "mapx";
        String MAPY = "mapy";
        String TIME = "time";
    }

    public enum Order {
        ALPHABETHEADER,
        ID
    }

    public DBHelper(Context context) {
        super(context, dbName, null, dbVersion);
        this.db = getWritableDatabase();
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Log.d(HELPER, "--- onCreate  ---");
        db.execSQL("create table " + tableNotesName + " ("
                + NoteColumns.ID + " integer primary key autoincrement," // noteId
                + NoteColumns.HEADER + " text not null unique," // title
                + NoteColumns.BODY + " text," // body
                + NoteColumns.MAPX + " TEXT, "
                + NoteColumns.MAPY + " TEXT, "
                + NoteColumns.TIME + " INTEGER, "
                + NoteColumns.MARKER + " integer" + ");"); // priority
    }

    public void close() {
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertNote(ContentValues cv) {
        cv.put(DBHelper.NoteColumns.TIME, System.currentTimeMillis());
        if(System.currentTimeMillis() - LaunchApplication.getInstance().getLasttimeUpdate() < LaunchApplication.validDeltaTime) {
            if(!cv.containsKey(NoteColumns.MAPX))
                cv.put(NoteColumns.MAPX, LaunchApplication.getInstance().getLastX());
            if(!cv.containsKey(NoteColumns.MAPY))
                cv.put(NoteColumns.MAPY, LaunchApplication.getInstance().getLastY());
        }
        db.insertWithOnConflict(tableNotesName, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        ctx.getContentResolver().notifyChange(FirstLevelActivity.noteUri, null);
    }

    public void deleteNote(ContentValues cv) {
        deleteNote(cv.getAsString(NoteColumns.HEADER), cv.getAsString(NoteColumns.BODY));
    }

    void deleteNote(String header, String body) {
        db.delete(tableNotesName, NoteColumns.HEADER + '=' + DatabaseUtils.sqlEscapeString(header) + " AND " + NoteColumns.BODY + '=' + DatabaseUtils.sqlEscapeString(body), null);
        ctx.getContentResolver().notifyChange(FirstLevelActivity.noteUri, null);
    }

    public Cursor getNotesCursor() {
        Cursor c = db.rawQuery("SELECT  * FROM " + tableNotesName, null);
        c.moveToFirst();

// Printing The DBData
//      if (c.moveToFirst()) {
//            int idColIndex = c.getColumnIndex(NoteColumns.ID);
//            int headerColIndex = c.getColumnIndex(NoteColumns.HEADER);
//            int bodyColIndex = c.getColumnIndex(NoteColumns.BODY);
//            do {
//                Log.d("IN_BASE",
//                        "ID = " + c.getInt(idColIndex) +
//                                ", header = " + c.getString(headerColIndex) +
//                                ", body = " + c.getString(bodyColIndex));
//            } while (c.moveToNext());
//            c.moveToFirst();
//      }
        return c;
    }




}

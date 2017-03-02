package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;

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

    public interface NoteColumns {
        String ID = "_id";
        String HEADER = "headerNote";
        String BODY = "textNote";
        String MARKER = "marker";
    }

    public enum Order {
        ALPHABETHEADER,
        PRIORITY
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
                + NoteColumns.MARKER + " integer" + ");"); // priority
    }

    public void close() {
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertNote(String header, String body, int marker) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.NoteColumns.HEADER,header);
        cv.put(DBHelper.NoteColumns.BODY, body);
        cv.put(DBHelper.NoteColumns.MARKER, marker);
        insertNote(cv);
    }

    public void insertNote(ContentValues cv) {
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

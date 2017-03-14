package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.LocationHolder;
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

    public SQLiteDatabase getDb() {
        return db;
    }

    public void open() {
        db = getWritableDatabase();
    }

    public void deleteAll() {
        db.delete(tableNotesName, null, null);
        ctx.getContentResolver().notifyChange(MainNoteActivity.noteUri, null);
        if (new File(Utils.getImagePathInDevice().getAbsolutePath()).exists()) {
            Utils.deleteAllFilesInDir(Utils.getImagePathInDevice(false)); // i don't want to delete above dirs
            Utils.deleteAllFilesInDir(Utils.getImagePathInDevice(true));
        }
    }

    public Cursor getNote(int idNote) {
        String selector = NoteColumns.ID + '=' + DatabaseUtils.sqlEscapeString(idNote + "");
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
        if (System.currentTimeMillis() - LocationHolder.getInstance(null).getLastTimeUpdate() < LocationHolder.validDeltaTime) {
            if (!cv.containsKey(NoteColumns.MAPX) || cv.getAsDouble(NoteColumns.MAPX) == 0)
                cv.put(NoteColumns.MAPX, LocationHolder.getInstance(null).getLastX());
            if (!cv.containsKey(NoteColumns.MAPY) || cv.getAsDouble(NoteColumns.MAPY) == 0)
                cv.put(NoteColumns.MAPY, LocationHolder.getInstance(null).getLastY());
        } else {
            //Toast.makeText(ctx, "old or empty gps data", Toast.LENGTH_SHORT).show();
        }
        try {
            db.insertWithOnConflict(tableNotesName, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            ctx.getContentResolver().notifyChange(MainNoteActivity.noteUri, null);
        } catch (Exception e) {
        } // there is error situation if will be conflict

    }

    public void deleteNote(ContentValues cv) {
        deleteNote(cv.getAsString(NoteColumns.HEADER), cv.getAsString(NoteColumns.BODY));
    }

    void deleteNote(String header, String body) {
        db.delete(tableNotesName, NoteColumns.HEADER + '=' + DatabaseUtils.sqlEscapeString(header) + " AND " + NoteColumns.BODY + '=' + DatabaseUtils.sqlEscapeString(body), null);
        ctx.getContentResolver().notifyChange(MainNoteActivity.noteUri, null);
    }

    void deleteNote(String id) {
        db.delete(tableNotesName, NoteColumns.ID + '=' + DatabaseUtils.sqlEscapeString(id), null);
        ctx.getContentResolver().notifyChange(MainNoteActivity.noteUri, null);
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

package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.chist.testprojectmosru.Application.LocationHolder;
import com.example.chist.testprojectmosru.Application.Utils;

/**
 * Created by 1 on 03.03.2017.
 */
public class BaseNoteActivity extends AppCompatActivity {

    DBHelper helper;
    protected ObserversHolder observers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observers = new ObserversHolder(getContentResolver());
        helper = new DBHelper(this);
        if(!helper.isOpen())
            helper.open();

    }

    public ObserversHolder getObservers() {
        return observers;
    }

    @Override
    protected void onStart() {
        if (!helper.isOpen()) {
            helper.open();
        }
        registerGpsChangedObserver();
        super.onStart();
    }

    private void registerGpsChangedObserver() {
        // Придет нотификация когда обновятся данные gps, проставим элементам эти данные (если актуально)
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Cursor c = helper.getNotesCursor();
                if (c.getCount() == 0) return;
                c.moveToFirst();
                do {
                    long time = c.getLong(c.getColumnIndex(DBHelper.NoteColumns.TIME));
                    boolean emptyGps = c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPX)) + c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPY)) == 0;
                    if (emptyGps && System.currentTimeMillis() - time < LocationHolder.validDeltaTime) {
                        ContentValues values = Utils.getContentValuesFromCursor(c);
                        values.put(DBHelper.NoteColumns.MAPX, LocationHolder.getInstance(null).getLastX());
                        values.put(DBHelper.NoteColumns.MAPY, LocationHolder.getInstance(null).getLastY());
                        helper.insertNote(values);
                    }
                }
                while (c.moveToNext());
            }
        };
        observers.register(Utils.getGeoDataUri(this), false, observer);
    }

    @Override
    protected void onStop() {
        if (helper.isOpen())
            helper.close();
        observers.unregisterAll();
        super.onStop();
    }
}

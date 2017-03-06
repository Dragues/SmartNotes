package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;

import java.util.LinkedList;

/**
 * Created by 1 on 03.03.2017.
 */
public class BaseNoteActivity extends AppCompatActivity {

    DBHelper helper;
    LinkedList<ContentObserver> observers = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new DBHelper(this);

        ContentObserver observer;

        // Придет нотификация когда обновятся данные gps в adapter-е заметок
        // решил не давать прямого доступа адаптеру на модификацию данных базы)
        getContentResolver().registerContentObserver(Utils.getGeoDataUri(), false, observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Cursor c = helper.getNotesCursor();
                if (c.getCount() == 0) return;
                c.moveToFirst();
                do {
                    long time = c.getLong(c.getColumnIndex(DBHelper.NoteColumns.TIME));
                    boolean emptyGps = c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPX)) + c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPY)) == 0;
                    if (emptyGps && System.currentTimeMillis() - time < LaunchApplication.validDeltaTime) {
                        ContentValues values = Utils.getContentValuesFromCursor(c);
                        values.put(DBHelper.NoteColumns.MAPX, LaunchApplication.getInstance().getLastX());
                        values.put(DBHelper.NoteColumns.MAPY, LaunchApplication.getInstance().getLastY());
                        helper.insertNote(values);
                    }
                }
                while (c.moveToNext());
            }
        });
        observers.add(observer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!helper.isOpen()) {
            helper.open();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (helper.isOpen())
            helper.close();
        for (ContentObserver item : observers) {
            getContentResolver().unregisterContentObserver(item);
        }
        observers.clear();
    }
}

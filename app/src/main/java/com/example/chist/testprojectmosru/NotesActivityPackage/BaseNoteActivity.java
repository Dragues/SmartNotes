package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.chist.testprojectmosru.Application.LocationHolder;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 1 on 03.03.2017.
 */
public class BaseNoteActivity extends AppCompatActivity {

    DatabaseHelper helper = null;
    protected ObserversHolder observers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        observers = new ObserversHolder(getContentResolver());
        helper = getHelper();
    }

    public ObserversHolder getObservers() {
        return observers;
    }

    @Override
    protected void onStart() {
        registerGpsChangedObserver();
        getHelper();
        super.onStart();
    }

    private void registerGpsChangedObserver() {
        // Придет нотификация когда обновятся данные gps, проставим элементам эти данные (если актуально)
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {

                try {
                    Dao<NoteDetails, Integer>  noteDao =  getHelper().getNoteDao();
                    List<NoteDetails> listNotes = noteDao.queryForAll();
                    if (listNotes.size() == 0) return;

                    for ( NoteDetails details : listNotes) {
                        long time = details.timestamp;
                        boolean emptyGps = details.x + details.y == 0;
                        if (emptyGps && System.currentTimeMillis() - time < LocationHolder.validDeltaTime) {
                            details.x = LocationHolder.getInstance(null).getLastX();
                            details.x = LocationHolder.getInstance(null).getLastY();
                            noteDao.createOrUpdate(details);
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        };
        observers.register(Utils.getGeoDataUri(this), false, observer);
    }

    @Override
    protected void onStop() {
        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }
        observers.unregisterAll();
        super.onStop();
    }

    // This is how, DatabaseHelper can be initialized for future use
    private DatabaseHelper getHelper() {
        if (helper == null) {
            helper = OpenHelperManager.getHelper(BaseNoteActivity.this, DatabaseHelper.class);
        }
        return helper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

		/*
		 * You'll need this in your class to release the helper when done.
		 */
        if (helper != null) {
            OpenHelperManager.releaseHelper();
            helper = null;
        }
    }
}

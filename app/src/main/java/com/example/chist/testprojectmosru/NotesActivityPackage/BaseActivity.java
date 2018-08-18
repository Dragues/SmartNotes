package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Activity;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
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
public class BaseActivity extends PermissionActivity {

    protected LaunchApplication mApp;
    private boolean mIsVisible;
    protected ObserversHolder observers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (LaunchApplication) this.getApplicationContext();
        observers = new ObserversHolder(getContentResolver());
    }

    public ObserversHolder getObservers() {
        return observers;
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerGpsChangedObserver();
        mApp.setCurrentActivity(this);
    }

    private void registerGpsChangedObserver() {
        // Придет нотификация когда обновятся данные gps, проставим элементам эти данные (если актуально)
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {

                try {
                    Dao<NoteDetails, Integer>  noteDao =  DatabaseHelper.getInstance().getNoteDao();
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
        clearReferences();
        observers.unregisterAll();
        super.onStop();
    }

    private void clearReferences() {
        Activity currActivity = mApp.getCurrentActivity();
        if (currActivity != null && this.equals(currActivity))
            mApp.setCurrentActivity(null);
    }

    protected boolean isVisible() {
        return mIsVisible;
    }
}

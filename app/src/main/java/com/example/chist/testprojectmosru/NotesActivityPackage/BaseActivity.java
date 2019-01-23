package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Activity;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;

import com.example.chist.testprojectmosru.application.LaunchApplication;
import com.example.chist.testprojectmosru.application.LocationHolder;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.example.chist.testprojectmosru.db.NoteDao;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by 1 on 03.03.2017.
 */
public class BaseActivity extends PermissionActivity {

    protected LaunchApplication mApp;
    private boolean mIsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (LaunchApplication) this.getApplicationContext();
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
                    NoteDao noteDao =  DatabaseHelper.getInstance().getNoteDao();
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
    }

    @Override
    protected void onStop() {
        clearReferences();
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

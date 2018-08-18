package com.example.chist.testprojectmosru.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static volatile DatabaseHelper sInstance;

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
        TIME,
        ID
    }


    /************************************************
     * Suggested Copy/Paste code. Everything from here to the done block.
     ************************************************/

    private static final String DATABASE_NAME = "notes_db.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<NoteDetails, Integer> noteDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    public static DatabaseHelper getInstance() {
        DatabaseHelper localInstance = sInstance;
        if (localInstance == null && LaunchApplication.getInstance() != null) {
            synchronized (DatabaseHelper.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new DatabaseHelper(LaunchApplication.getInstance());
                }
            }
        }
        return localInstance;
    }

    /************************************************
     * Suggested Copy/Paste Done
     ************************************************/

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {
            // Create tables. This onCreate() method will be invoked only once of the application life time i.e. the first time when the application starts.
            TableUtils.createTable(connectionSource, NoteDetails.class);

        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {
            TableUtils.dropTable(connectionSource, NoteDetails.class, true);
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
                    + newVer, e);
        }
    }

    // Create the getDao methods of all database tables to access those from android code.
    // Insert, delete, read, update everything will be happened through DAOs
    public Dao<NoteDetails, Integer> getNoteDao() throws SQLException {
        if (noteDao == null) {
            noteDao = getDao(NoteDetails.class);
        }
        return noteDao;
    }

    public NoteDetails getNote(int idNote) {
        try {
            List<NoteDetails> detailsList = noteDao.queryForAll();
            for (NoteDetails details : detailsList) {
                if (details.id == idNote)
                    return details;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // error or noData
        return null;
    }

    public void deleteNotes() {
        try {
            List<NoteDetails> detailsList = noteDao.queryForAll();
            for (NoteDetails details : detailsList) {
                deleteNote(details);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteNote(NoteDetails details) {
        try {
            noteDao.delete(details);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public void notifyChange(Uri noteUri) {
//        observers.notifyChange(MainNoteActivity.noteUri, null);
//    }
}


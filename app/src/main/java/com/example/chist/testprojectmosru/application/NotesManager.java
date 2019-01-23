package com.example.chist.testprojectmosru.application;

import com.example.chist.testprojectmosru.NotesActivityPackage.Note;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.example.chist.testprojectmosru.utils.DelegatesHolder;
import com.j256.ormlite.misc.TransactionManager;

import org.apache.commons.collections4.Closure;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;

public class NotesManager  {

    protected DelegatesHolder<NoteDelegate> mDelegates = new DelegatesHolder<>();

    public static NotesManager getInstance() {
        return InstanceHolder.sInstance;
    }


    private static class InstanceHolder {
        public static final NotesManager sInstance = new NotesManager();
    }

    private static void deleteAllNotes() {
        DatabaseHelper.getInstance().clearTable(Note.class);
    }

    private static void updateNoteUser(NoteDetails note) {
        Timber.d("Updating note in db. user: " + note);
        try {
            TransactionManager.callInTransaction(DatabaseHelper.getInstance().getConnectionSource(), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    DatabaseHelper.getInstance().getNoteDao().createOrUpdate(note);
                    Timber.d("Note updated in DB");
                    return null;
                }
            });
        } catch (SQLException e) {
            Timber.d(e, "Error updating user. Users in db:");
            List<NoteDetails> notes;
            try {
                notes = DatabaseHelper.getInstance().getNoteDao().queryForAll();
                for (NoteDetails temp : notes) {
                    Timber.d("Saved user:" + temp);
                }
            } catch (SQLException e1) {
                //do nothing
            }
            throw new RuntimeException(e);
        }
    }

    public interface NoteDelegate {
        void onNoteChanged(NoteDetails note);
        void onDataChanged(int id);
    }

    public void addDelegate(NoteDelegate delegate) {
        mDelegates.add(delegate);
    }

    public void removeDelegate(NoteDelegate delegate) {
        mDelegates.remove(delegate);
    }

    private void notifyNoteChanged(NoteDetails noteDelegate) {
        Utils.runUi(() -> {
            mDelegates.removeNulled();
            mDelegates.forAllDo(delegate -> {
                if (delegate instanceof NoteDelegate)
                    ((NoteDelegate) delegate).onNoteChanged(noteDelegate);
            });
        });
    }

    public void notifyOnDataChanged(int id) {
        mDelegates.removeNulled();
        mDelegates.forAllDo(new Closure<NoteDelegate>() {
            @Override
            public void execute(NoteDelegate input) {
                input.onDataChanged(id);
            }
        });
    }
}

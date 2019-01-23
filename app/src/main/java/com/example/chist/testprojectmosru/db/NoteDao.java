package com.example.chist.testprojectmosru.db;

import android.text.TextUtils;

import com.example.chist.testprojectmosru.data.NoteDetails;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;
import java.util.List;

public class NoteDao extends BaseDaoImpl<NoteDetails, Long> {
    private ThreadLocal<PreparedQuery<NoteDetails>> mMutableFieldsQuery;
    private boolean mQueriesPrepared;

    public NoteDao(Class<NoteDetails> dataClass) throws SQLException {
        super(dataClass);
    }

    public NoteDao(ConnectionSource connectionSource, Class<NoteDetails> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public NoteDao(ConnectionSource connectionSource, DatabaseTableConfig<NoteDetails> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    public NoteDetails noteById(long bookId) {
        NoteDetails result = null;
        try {
            result = queryForId(bookId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void updateNote(NoteDetails book) {
        try {
            update(book);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createOrUpdateBooks(List<NoteDetails> notes) throws SQLException {
        for (NoteDetails note : notes)
            createOrUpdate(note);
    }

}

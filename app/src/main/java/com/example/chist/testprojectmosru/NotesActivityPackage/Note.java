package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.database.Cursor;

import com.example.chist.testprojectmosru.data.DatabaseHelper;

/**
 * Created by 1 on 04.03.2017.
 */
public class Note implements Cloneable{
    public String header;
    public String body;
    public int marker;
    public double x;
    public double y;
    public long timestamp;
    public int id;

    public Note(String header, String body, int marker, double mapx, double mapy, int timestamp, int id) {
        this.header = header;
        this.body = body;
        this.marker = marker;
        this.x = mapx;
        this.y = mapy;
        this.timestamp = timestamp;
        this.id = id;
    }

    public Note(Cursor c) {
        this.header = c.getString(c.getColumnIndex(DatabaseHelper.NoteColumns.HEADER));
        this.body = c.getString(c.getColumnIndex(DatabaseHelper.NoteColumns.BODY));
        this.marker = c.getInt(c.getColumnIndex(DatabaseHelper.NoteColumns.MARKER));
        this.x =  c.getDouble(c.getColumnIndex(DatabaseHelper.NoteColumns.MAPX));
        this.y =  c.getDouble(c.getColumnIndex(DatabaseHelper.NoteColumns.MAPY));
        this.timestamp = c.getInt(c.getColumnIndex(DatabaseHelper.NoteColumns.TIME));
        this.id = c.getInt(c.getColumnIndex(DatabaseHelper.NoteColumns.ID));
    }

    public boolean checkEquals(Note obj) {
        return this.header.equals(obj.header) &&
                this.body.equals(obj.body) &&
                this.x == obj.x &&
                this.y == obj.y &&
                this.timestamp == obj.timestamp &&
                this.marker == obj.marker &&
                this.id  == obj.id;
    }

    protected Object clone()  {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

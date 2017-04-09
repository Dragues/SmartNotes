package com.example.chist.testprojectmosru.data;

import com.j256.ormlite.field.DatabaseField;
import java.io.Serializable;

/**
 * Created by 1 on 04.03.2017.
 */
public class NoteDetails implements Serializable, Cloneable{

    /**
     *  Model class for teacher_details database table
     */
    private static final long serialVersionUID = -222864131214757024L;

    // Primary key defined as an auto generated integer
    // If the database table column name differs than the Model class variable name, the way to map to use columnName
    @DatabaseField(generatedId = true, unique = true, columnName = "_id")
    public int id;

    // Define a String type field to hold note's name
    @DatabaseField(columnName = "headerNote")
    public String header;

    @DatabaseField(columnName = "textNote")
    public String body;

    @DatabaseField(columnName = "marker")
    public int marker;

    @DatabaseField(columnName = "mapx")
    public double x;

    @DatabaseField(columnName = "mapy")
    public double y;

    @DatabaseField(columnName = "time")
    public long timestamp;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public NoteDetails(){
        this.marker = 1;
        this.timestamp = System.currentTimeMillis();
        this.body = "";
        this.header = "";
    }

    public NoteDetails(String header, String body, int marker, double mapx, double mapy, int timestamp, int id) {
        this.header = header;
        this.body = body;
        this.marker = marker;
        this.x = mapx;
        this.y = mapy;
        this.timestamp = timestamp;
        this.id = id;
    }

    public boolean checkEquals(NoteDetails obj) {
        return this.header.equals(obj.header) &&
                this.body.equals(obj.body) &&
                this.x == obj.x &&
                this.y == obj.y &&
                this.timestamp == obj.timestamp &&
                this.marker == obj.marker &&
                this.id  == obj.id;
    }

    public Object clone()  {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

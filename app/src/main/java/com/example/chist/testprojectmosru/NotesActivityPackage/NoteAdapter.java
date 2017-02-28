package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;

/**
 * Created by 1 on 27.02.2017.
 */
public class NoteAdapter extends CursorAdapter {

    private Context ctx;

    protected static class ViewHolder {
        protected TextView header;
        protected TextView body;
        protected ImageView photo;
        private  ImageView edit;
        private TextView coordsAndTimeStamp;
    }


    public NoteAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        this.ctx = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View   view    =   LayoutInflater.from(context).inflate(R.layout.note_item, null);
        ViewHolder holder  =   new ViewHolder();
        holder.header = (TextView) view.findViewById(R.id.header);
        holder.body = (TextView) view.findViewById(R.id.body);
        holder.edit = (ImageView) view.findViewById(R.id.edit);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //holder.photo = (ImageView) view.findViewById(R.id.marker);
        ViewHolder holder  =   (ViewHolder)    view.getTag();
        final String header = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.HEADER));
        final String body = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.BODY));
        final int  marker = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MARKER));
        holder.header.setText(header);
        holder.body.setText(body);
        holder.edit.setOnClickListener(new View.OnClickListener() { // I don't like set listeners here =(
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialogs.AddingDialog(ctx, header, body, marker, ((FirstLevelActivity)ctx).helper);
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        view.setBackgroundColor(Utils.getBackGroundColorFromMarker(ctx, marker));
        view.setTag(holder);
    }

}

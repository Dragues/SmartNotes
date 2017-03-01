package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
        private  ImageView export;
        private Uri imageUri;
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
        holder.export = (ImageView) view.findViewById(R.id.export);
        holder.photo = (ImageView) view.findViewById(R.id.photo);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder  =   (ViewHolder)    view.getTag();
        final String header = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.HEADER));
        final String body = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.BODY));
        final int  marker = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MARKER));

        holder.header.setText(header);
        holder.body.setText(body);
        holder.export.setBackground(ctx.getResources().getDrawable(Utils.containsFile(header) ? R.drawable.exported : R.drawable.non_exported));
        holder.export.setOnClickListener(new ExportListener(holder.export, header, body));
        holder.imageUri = Utils.getImageUri(header);
        holder.edit.setOnClickListener(new View.OnClickListener() { // I don't like set listeners here =(
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialogs.AddingDialog(ctx, header, body, marker, ((FirstLevelActivity) ctx).helper);
                dialog.setCancelable(true);
                dialog.show();
            }
        });
        ctx.getContentResolver().registerContentObserver(holder.imageUri, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Bitmap bitmap = Utils.getSavedBitmap(header, false);
                if(bitmap != null)
                    holder.photo.setImageBitmap(bitmap);
                else
                    holder.photo.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                            R.drawable.no_data));
            }
        });
        holder.photo.setOnClickListener(new LoadImageListener(ctx,header));
        Bitmap bitmap = Utils.getSavedBitmap(header, false);
        if(bitmap != null)
            holder.photo.setImageBitmap(bitmap);
        else
            holder.photo.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.no_data));

        view.setBackgroundColor(Utils.getBackGroundColorFromMarker(ctx, marker));
        view.setTag(holder);
    }




    private class ExportListener implements View.OnClickListener {

        private String header;
        private String body;
        private ImageView exportView;

        public ExportListener(ImageView export, String header, String body) {
            this.header = header;
            this.body = body;
            this.exportView = export;
        }

        @Override
        public void onClick(View v) {
            if(!Utils.containsFile(header))
                Utils.exportToFile(ctx, header, body);
            else
                Utils.deleteFileFromSd(ctx, header);
            // Export or delele can be process with errors, so i prefer check it there.
            exportView.setBackground(ctx.getResources().getDrawable(
                    Utils.containsFile(header) ? R.drawable.exported : R.drawable.non_exported));
        }
    }

    private class LoadImageListener implements View.OnClickListener {

        private String header;
        private Context ctx;

        public LoadImageListener(Context ctx, String header) {
            this.header = header;
            this.ctx = ctx;
        }

        @Override
        public void onClick(View v) {
            // Send action to gallery for choosing image
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            ((FirstLevelActivity)ctx).setHeaderOnImageUpdate(header);
            photoPickerIntent.setType("image/*");
            ((Activity)ctx).startActivityForResult(photoPickerIntent, FirstLevelActivity.SELECT_PHOTO);
        }
    }
}

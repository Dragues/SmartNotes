package com.example.chist.testprojectmosru.NotesActivityPackage;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chist.testprojectmosru.Application.LaunchApplication;
import com.example.chist.testprojectmosru.Application.SocialUtils;
import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.Dialogs.Dialogs;
import com.example.chist.testprojectmosru.R;
import com.vk.sdk.VKSdk;

/**
 * Created by 1 on 27.02.2017.
 */
// In this task i want to use CursorAdapter
public class NoteAdapter extends CursorAdapter {

    private Context ctx;

    protected static class ViewHolder {
        protected TextView header;
        protected TextView body;
        protected ImageView photo;
        private  ImageView edit;
        private  ImageView export;
        private Uri imageUri;
        private int id;
        private TextView coords;
        private ImageView vkView;

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
        holder.coords = (TextView) view.findViewById(R.id.coords);
        holder.vkView = (ImageView) view.findViewById(R.id.vk);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        final ViewHolder holder  =   (ViewHolder)    view.getTag();
        final String header = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.HEADER));
        final String body = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.BODY));
        final int  marker = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MARKER));
        final double  X = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MAPX));
        final double  Y = cursor.getDouble(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.MAPY));
        final long  time = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.TIME));

        holder.id = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.NoteColumns.ID));
        holder.header.setText(header);
        holder.body.setText(body);
        holder.export.setBackground(ctx.getResources().getDrawable(Utils.containsFile(header) ? R.drawable.exported : R.drawable.non_exported));
        holder.export.setOnClickListener(new ExportListener(holder.export, String.valueOf(holder.id), "HEADER: " + header + " BODY: " + body));
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = Utils.getContentValuesFromCursor(cursor);
                Dialog dialog = new Dialogs.AddingDialog(ctx, values, ((FirstLevelActivity) ctx).helper);
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        // IMAGE (обновляем сколько угодно раз, оставляю наблюдатель пока жив адаптер)
        holder.imageUri = Utils.getImageUri(holder.id);
        holder.photo.setOnClickListener(new LoadImageListener(ctx, holder.id));
        Bitmap bitmap = Utils.getSavedBitmap(holder.id, false);
        if(bitmap != null)
            holder.photo.setImageBitmap(bitmap);
        else
            holder.photo.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                    R.drawable.no_data));
        ctx.getContentResolver().registerContentObserver(holder.imageUri, false, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                Bitmap bitmap = Utils.getSavedBitmap(holder.id, false);
                if (bitmap != null)
                    holder.photo.setImageBitmap(bitmap);
                else
                    holder.photo.setImageBitmap(BitmapFactory.decodeResource(ctx.getResources(),
                            R.drawable.no_data));
            }
        });

        // GPS
        if (X != 0 && Y != 0)
            holder.coords.setText("X: " + X + "\n" + "Y: "  + Y);
        else {
            final ContentObserver gpsObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    if ((X == 0 || Y == 0) && System.currentTimeMillis() - time < LaunchApplication.validDeltaTime) {
                        ctx.getContentResolver().notifyChange(Utils.getGeoDataUri(),null);
                        holder.coords.setText("X: " + LaunchApplication.getInstance().getLastX() + "\n" +
                                "Y: " + LaunchApplication.getInstance().getLastY());
                        ctx.getContentResolver().unregisterContentObserver(this); // сделал единоразовую подписку
                    }

                }
            };
            holder.coords.setText(ctx.getResources().getString(R.string.no_data));
            ctx.getContentResolver().registerContentObserver(Utils.getGeoDataUriAdapter(), false, gpsObserver);
        }

        view.setBackgroundColor(Utils.getBackGroundColorFromMarker(ctx, marker));
        view.setTag(holder);
    }



    private class ExportListener implements View.OnClickListener {

        private String id;
        private String bodyHeader;
        private ImageView exportView;

        public ExportListener(ImageView export, String id, String bodyHeader) {
            this.id = id;
            this.bodyHeader = bodyHeader;
            this.exportView = export;
        }

        @Override
        public void onClick(View v) {
            if(!Utils.containsFile(id))
                Utils.exportToFile(ctx, id, bodyHeader);
            else
                Utils.deleteFileFromSd(ctx, id);
            // Export or delele can be process with errors, so i prefer check it there.
            exportView.setBackground(ctx.getResources().getDrawable(
                    Utils.containsFile(id) ? R.drawable.exported : R.drawable.non_exported));
        }
    }

    private class LoadImageListener implements View.OnClickListener {

        private int id;
        private Context ctx;

        public LoadImageListener(Context ctx, int id) {
            this.id = id;
            this.ctx = ctx;
        }

        @Override
        public void onClick(View v) {
            // Send action to gallery for choosing image
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            ((FirstLevelActivity)ctx).setHeaderOnImageUpdate(id);
            photoPickerIntent.setType("image/*");
            ((Activity)ctx).startActivityForResult(photoPickerIntent, FirstLevelActivity.SELECT_PHOTO);
        }
    }



}

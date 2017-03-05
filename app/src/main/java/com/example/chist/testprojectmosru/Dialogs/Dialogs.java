package com.example.chist.testprojectmosru.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chist.testprojectmosru.Application.Utils;
import com.example.chist.testprojectmosru.NotesActivityPackage.NoteActivity;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.NotesActivityPackage.DBHelper;


/**
 * Created by 1 on 27.02.2017.
 */
public class Dialogs {

    public static class AddingDialog extends Dialog {

        private Context ctx;

        public AddingDialog(Context ctx, ContentValues values, DBHelper helper) {
            super(ctx, R.style.ContainerDialogTheme);
            this.ctx = ctx;
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(values, helper);
        }

        private void populate(final ContentValues values, final DBHelper helper) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_note_item, null);
            final EditText headerView = (EditText) view.findViewById(R.id.header);
            final EditText bodyView = (EditText) view.findViewById(R.id.body);
            final SeekBar barPriority = (SeekBar) view.findViewById(R.id.priority);
            TextView addButton = (TextView) view.findViewById(R.id.addbutton);
            TextView cancelButton = (TextView) view.findViewById(R.id.cancelbutton);

            if (values != null) {
                if(values.containsKey(DBHelper.NoteColumns.MARKER))
                    barPriority.setProgress(values.getAsInteger(DBHelper.NoteColumns.MARKER));
                if (values.containsKey(DBHelper.NoteColumns.HEADER))
                    headerView.setText(values.getAsString(DBHelper.NoteColumns.HEADER));
                if (values.containsKey(DBHelper.NoteColumns.BODY))
                    bodyView.setText(values.getAsString(DBHelper.NoteColumns.BODY));
            }

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues valuesNew =  new ContentValues();
                    if (values != null)
                        valuesNew.putAll(values);
                    valuesNew.put(DBHelper.NoteColumns.HEADER, headerView.getText().toString());
                    valuesNew.put(DBHelper.NoteColumns.BODY, bodyView.getText().toString());
                    valuesNew.put(DBHelper.NoteColumns.MARKER, barPriority.getProgress());
                    if (headerView.getText().toString().length() != 0 && bodyView.getText().toString().length() != 0) {
                        if(values != null && values.getAsString(DBHelper.NoteColumns.HEADER).equals(headerView.getText().toString()) &&
                                values.getAsString(DBHelper.NoteColumns.BODY).equals(headerView.getText().toString())) {
                            Toast.makeText(ctx, "Change data!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        helper.insertNote(valuesNew);
                    }
                    else {
                        Toast.makeText(ctx, "Change data!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dismiss();
                }
            });
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    public static class ConfirmDialog extends Dialog {

        private Context ctx;

        public ConfirmDialog(Context ctx, Runnable runnable) {
            super(ctx, R.style.ContainerDialogTheme);
            this.ctx = ctx;
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(runnable);
        }

        private void populate(final Runnable runnable) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.confirm_dialog, null);

            TextView positive = (TextView) view.findViewById(R.id.yes);
            TextView negative = (TextView) view.findViewById(R.id.no);

            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runnable.run();
                    dismiss();
                }
            });

            negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }


    // has some differences (can be optimized)
    public static class ModifyDialog extends Dialog {

        private Context ctx;

        public ModifyDialog(Context ctx, String header, String body, int marker) {
            super(ctx, R.style.ContainerDialogTheme);
            this.ctx = ctx;
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(header, body, marker);
        }

        private void populate(final String header, final String body, int marker) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_note_item, null);
            final EditText headerView = (EditText) view.findViewById(R.id.header);
            final EditText bodyView = (EditText) view.findViewById(R.id.body);
            final SeekBar barPriority = (SeekBar) view.findViewById(R.id.priority);
            barPriority.setProgress(marker);
            if (header != null)
                headerView.setText(header);
            if (body != null)
                bodyView.setText(body);

            TextView modify = (TextView) view.findViewById(R.id.addbutton);
            TextView cancelButton = (TextView) view.findViewById(R.id.cancelbutton);

            modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (headerView.getText().length() != 0 && bodyView.getText().length() != 0) {
                        sendSerialPendingRequestByCode(NoteActivity.requestCodeUpdateData, headerView.getText().toString(), bodyView.getText().toString(), barPriority.getProgress());
                    }
                    dismiss();
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        private void sendSerialPendingRequestByCode(int requestCode, String headerNew, String bodyNew, int marker) {
            Intent intent = new Intent("ANYTHINGTITLE" + "_" + System.currentTimeMillis()); // intents can be cached (it's may occur the errors), name can be anything
            Bundle b = new Bundle();
            switch (requestCode) {
                case NoteActivity.requestCodeUpdateData: // success serial result
                    b.putString(NoteActivity.HEADERTAG, headerNew);
                    b.putString(NoteActivity.BODYTAG, bodyNew);
                    b.putInt(NoteActivity.MARKERTAG, marker);
                    break;
            }
            intent.putExtras(b);
            PendingIntent pi = ((Activity) ctx).createPendingResult(requestCode, intent, 0);
            try {
                pi.send();
            } catch (Exception e) {
            }
        }
    }


}

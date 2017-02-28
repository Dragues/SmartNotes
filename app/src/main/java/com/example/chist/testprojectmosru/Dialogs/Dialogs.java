package com.example.chist.testprojectmosru.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.chist.testprojectmosru.NotesActivityPackage.NoteActivity;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.NotesActivityPackage.DBHelper;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by 1 on 27.02.2017.
 */
public class Dialogs {

    public static class AddingDialog extends Dialog {

        private Context ctx;

        public AddingDialog(Context ctx, String header, String body, DBHelper helper) {
            super(ctx, R.style.ContainerDialogTheme);
            this.ctx = ctx;
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(header, body, helper);


            this.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }
            });
        }

        private void populate(final String header, final String body, final DBHelper helper) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_note_item, null);
                final EditText headerView = (EditText) view.findViewById(R.id.header);
                final EditText bodyView = (EditText) view.findViewById(R.id.body);
                final SeekBar barPriority = (SeekBar) view.findViewById(R.id.priority);
                if (header != null)
                    headerView.setText(header);
                if (body != null)
                    bodyView.setText(body);

            TextView addButton = (TextView) view.findViewById(R.id.addbutton);
            TextView cancelButton = (TextView) view.findViewById(R.id.cancelbutton);

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(headerView.getText().length()!=0 && bodyView.getText().length()!=0) {
                        ContentValues cv = new ContentValues();
                        cv.put(DBHelper.NoteColumns.HEADER,headerView.getText().toString());
                        cv.put(DBHelper.NoteColumns.BODY, bodyView.getText().toString());
                        cv.put(DBHelper.NoteColumns.MARKER, barPriority.getProgress());
                        helper.insertNote(cv);

                        if(header != null && body != null) {
                            ContentValues cvOld = new ContentValues();
                            cvOld.put(DBHelper.NoteColumns.HEADER,header);
                            cvOld.put(DBHelper.NoteColumns.BODY, body);
                            helper.deleteNote(cvOld);
                        }
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

        public ModifyDialog(Context ctx, String header, String body) {
            super(ctx, R.style.ContainerDialogTheme);
            this.ctx = ctx;
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(header, body);


            this.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }
            });
        }

        private void populate(final String header, final String body) {
            View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_note_item, null);
            final EditText headerView = (EditText) view.findViewById(R.id.header);
            final EditText bodyView = (EditText) view.findViewById(R.id.body);
            final SeekBar barPriority = (SeekBar) view.findViewById(R.id.priority);
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
                        sendSerialPendingRequestByCode(100500,headerView.getText().toString(), bodyView.getText().toString());
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

        private void sendSerialPendingRequestByCode(int requestCode, String headerNew, String bodyNew) {
            Intent intent = new Intent("PENDING_MODIFY" + "_" + System.currentTimeMillis()); // intents can be cached (it's may occur the errors), name can be anything
            Bundle b = new Bundle();
            switch(requestCode) {
                case 100500: // success serial result
                    b.putString(NoteActivity.HEADERTAG, headerNew);
                    b.putSerializable(NoteActivity.BODYTAG, bodyNew);
                    break;
            }
            intent.putExtras(b);
            PendingIntent pi = ((Activity)ctx).createPendingResult(requestCode, intent, 0);
            try {
                pi.send();
            }
            catch(Exception e) {}
        }
    }







}

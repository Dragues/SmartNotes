package com.example.chist.testprojectmosru.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chist.testprojectmosru.NotesActivityPackage.MainNoteActivity;
import com.example.chist.testprojectmosru.NotesActivityPackage.NoteActivity;
import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.example.chist.testprojectmosru.data.NoteDetails;
import com.example.chist.testprojectmosru.db.NoteDao;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;


/**
 * Created by 1 on 27.02.2017.
 */
public class Dialogs {
    public static class AddingDialog extends Dialog {
        public AddingDialog(Context ctx, NoteDetails noteDetails) {
            super(ctx, R.style.ContainerDialogTheme);
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(ctx, noteDetails == null ? new NoteDetails() : noteDetails);
        }

        private void populate(Context ctx, final NoteDetails noteDetails) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_note_item, null);
            final EditText headerView = (EditText) view.findViewById(R.id.header);
            final EditText bodyView = (EditText) view.findViewById(R.id.body);
            final SeekBar barPriority = (SeekBar) view.findViewById(R.id.priority);
            TextView addButton = (TextView) view.findViewById(R.id.addbutton);
            TextView cancelButton = (TextView) view.findViewById(R.id.cancelbutton);
            ((TextView) view.findViewById(R.id.dialogheader)).setText(getContext().getResources().getString(R.string.new_note));

            barPriority.setProgress(noteDetails.marker);
            headerView.setText(noteDetails.header);
            bodyView.setText(noteDetails.body);

            addButton.setOnClickListener(createAddButtonOnClickListener(ctx, noteDetails, headerView, bodyView, barPriority));
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        @NonNull
        private View.OnClickListener createAddButtonOnClickListener(final Context ctx, final NoteDetails noteDetails, final EditText headerView, final EditText bodyView, final SeekBar barPriority) {
            return v -> {
                if (noteDetails.header.equals(headerView.getText().toString()) &&
                        noteDetails.body.equals(headerView.getText().toString())) {
                    Toast.makeText(getContext(), "Change data!", Toast.LENGTH_SHORT).show();
                    return;
                }

                noteDetails.header = headerView.getText().toString().trim();
                noteDetails.body = bodyView.getText().toString().trim();
                noteDetails.marker = barPriority.getProgress();
                noteDetails.timestamp = System.currentTimeMillis();

                if (noteDetails.header.length() != 0 && noteDetails.body.length() != 0) {
                    if (ctx instanceof NoteActivity) {
                        sendSerialPendingRequestByCode(ctx, NoteActivity.requestCodeUpdateData, noteDetails);
                        dismiss();
                        return;
                    }
                    try {
                        final NoteDao noteDao = DatabaseHelper.getInstance().getNoteDao();
                        noteDao.createOrUpdate(noteDetails);
                        if (ctx instanceof MainNoteActivity)
                            ((MainNoteActivity) ctx).refresh();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "Change data!", Toast.LENGTH_SHORT).show();
                    return;
                }
                dismiss();
            };
        }

        private void sendSerialPendingRequestByCode(Context ctx, int requestCode, NoteDetails noteDetails) {
            Intent intent = new Intent("ANYTHINGTITLE" + "_" + System.currentTimeMillis()); // intents can be cached (it's may occur the errors), name can be anything
            Bundle b = new Bundle();
            switch (requestCode) {
                case NoteActivity.requestCodeUpdateData: // success serial result
                    b.putSerializable(NoteActivity.HEADERTAG, noteDetails);
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

    public static class ConfirmDialog extends Dialog {
        public ConfirmDialog(Context ctx, Runnable runnable) {
            super(ctx, R.style.ContainerDialogTheme);
            setCancelable(true);
            setCanceledOnTouchOutside(false);
            populate(runnable);
        }

        private void populate(final Runnable runnable) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.confirm_dialog, null);

            TextView positive = (TextView) view.findViewById(R.id.yes);
            TextView negative = (TextView) view.findViewById(R.id.no);

            positive.setOnClickListener(v -> {
                runnable.run();
                dismiss();
            });

            negative.setOnClickListener(v -> dismiss());
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    // has some differences (can be optimized)
//    public static class ModifyDialog extends Dialog {
//        Context ctx;
//
//        public ModifyDialog(Context ctx, NoteDetails noteDetails) {
//            super(ctx, R.style.ContainerDialogTheme);
//            this.ctx = ctx;
//            setCancelable(true);
//            setCanceledOnTouchOutside(false);
//            populate(noteDetails);
//        }
//
//        private void populate(NoteDetails noteDetails) {
//            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_note_item, null);
//            final EditText headerView = (EditText) view.findViewById(R.id.header);
//            final EditText bodyView = (EditText) view.findViewById(R.id.body);
//            final SeekBar barPriority = (SeekBar) view.findViewById(R.id.priority);
//            barPriority.setProgress(marker);
//            if (header != null)
//                headerView.setText(header);
//            if (body != null)
//                bodyView.setText(body);
//
//            TextView modify = (TextView) view.findViewById(R.id.addbutton);
//            TextView cancelButton = (TextView) view.findViewById(R.id.cancelbutton);
//            ((TextView) view.findViewById(R.id.dialogheader)).setText(getContext().getResources().getString(R.string.edit_note));
//
//            modify.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (headerView.getText().length() != 0 && bodyView.getText().length() != 0) {
//                        sendSerialPendingRequestByCode(NoteActivity.requestCodeUpdateData, headerView.getText().toString(), bodyView.getText().toString(), barPriority.getProgress());
//                    }
//                    dismiss();
//                }
//            });
//
//            cancelButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dismiss();
//                }
//            });
//            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        }

//        private void sendSerialPendingRequestByCode(int requestCode, String headerNew, String bodyNew, int marker) {
//            Intent intent = new Intent("ANYTHINGTITLE" + "_" + System.currentTimeMillis()); // intents can be cached (it's may occur the errors), name can be anything
//            Bundle b = new Bundle();
//            switch (requestCode) {
//                case NoteActivity.requestCodeUpdateData: // success serial result
//                    b.putString(NoteActivity.HEADERTAG, headerNew);
//                    b.putString(NoteActivity.BODYTAG, bodyNew);
//                    b.putInt(NoteActivity.MARKERTAG, marker);
//                    break;
//            }
//            intent.putExtras(b);
//            PendingIntent pi = ((Activity) ctx).createPendingResult(requestCode, intent, 0);
//            try {
//                pi.send();
//            } catch (Exception e) {
//            }
//        }
    // }

    public static class ShowPhoto extends Dialog {
        public ShowPhoto(Context ctx, Bitmap bitmap) {
            super(ctx, R.style.ContainerDialogTheme);
            setCancelable(true);
            setCanceledOnTouchOutside(true);
            populate(bitmap);
        }

        private void populate(Bitmap bitmap) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.show_photo_dialog, null);
            final ImageView photo = (ImageView) view.findViewById(R.id.photo);
            photo.setImageBitmap(bitmap);
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    public static class InfoDialog extends Dialog {
        public InfoDialog(Context ctx) {
            super(ctx, R.style.ContainerDialogTheme);
            setCancelable(true);
            setCanceledOnTouchOutside(true);
            populate();
        }

        private void populate() {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.app_info_dialog, null);
            view.findViewById(R.id.ok).setOnClickListener(v -> dismiss());
            setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }
}

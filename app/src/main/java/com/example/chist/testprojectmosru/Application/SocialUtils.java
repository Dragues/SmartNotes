package com.example.chist.testprojectmosru.Application;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.example.chist.testprojectmosru.NotesActivityPackage.Note;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.dialogs.VKShareDialogBuilder;

/**
 * Created by 1 on 05.03.2017.
 */
public class SocialUtils {

    public static void shareWithDialog(final Context ctx, final Bitmap bitmap, Note noteNew, FragmentManager fragmentManager) {
        VKShareDialog builder = new VKShareDialog();
        String postResult = "Header note: " + noteNew.header + "\n" +
                "Description: " + noteNew.body;
        if (noteNew.x != 0 && noteNew.y != 0) {
            postResult += "\n" + "Lat: " + noteNew.x;
            postResult += "Lon: " + noteNew.y;
        }
        postResult += "\n#vkApi";

        builder.setText(postResult);
        builder.setAttachmentImages(new VKUploadImage[]{
                new VKUploadImage(bitmap, VKImageParameters.pngImage())
        });

        builder.setAttachmentLink("Smart Notes",
                "https://play.google.com/store?hl=ru");
        builder.setShareDialogListener(createVKShareDialog(ctx));
        builder.show(fragmentManager, "VK_SHARE_DIALOG");
    }

    @NonNull
    private static VKShareDialog.VKShareDialogListener createVKShareDialog(final Context ctx) {
        return new VKShareDialog.VKShareDialogListener() {
            @Override
            public void onVkShareComplete(int postId) {
                Toast.makeText(ctx, "Complete posting", Toast.LENGTH_LONG).show();
                // recycle bitmap if need
            }

            @Override
            public void onVkShareCancel() {
                Toast.makeText(ctx, "Cancel posting", Toast.LENGTH_LONG).show();
                // recycle bitmap if need
            }

            @Override
            public void onVkShareError(VKError error) {
                Toast.makeText(ctx, "Error posting", Toast.LENGTH_LONG).show();
                // recycle bitmap if need
            }
        };
    }

    public static void shareWithFaceBookDialog(final Context ctx, final Bitmap bitmap, Note noteNew) {
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(bitmap)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();
        ShareDialog shareDialog = new ShareDialog((Activity) ctx);
        shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
    }
}

package com.example.chist.testprojectmosru.Application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.chist.testprojectmosru.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 1 on 28.02.2017.
 */
public class Utils {

    public static String notesDir = "notes";
    public static String imagesDir = "images";
    public static String imagesSmall = "small";
    public static String imagesLarge = "large";
    public static String exportTag = "DATAEXPORT";

    public static int getBackGroundColorFromMarker(Context ctx, int marker) {
        switch(marker){
            case 0:
                return ctx.getResources().getColor(R.color.greenalpha);
            case 1:
                return ctx.getResources().getColor(R.color.yellowalpha);
            case 2:
                return ctx.getResources().getColor(R.color.redalpha);
            default:
                return ctx.getResources().getColor(R.color.yellowalpha);
        }
    }

    public static boolean exportToFile(Context ctx, String filename, String content) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(exportTag, "SD-карта не доступна: " + Environment.getExternalStorageState());
            Toast.makeText(ctx, "SD-карта не доступна", Toast.LENGTH_LONG).show();
            return false;
        }
        else {
            return writeFileSD(ctx, filename, content);
        }
    }

    public static boolean deleteFileFromSd(Context ctx, String filename){

        File notePath = getNotePathInDevice();
        if(!notePath.exists())
            return false;

        File sdFile = new File(notePath, filename);
        if (sdFile.delete()){
            Toast.makeText(ctx, "Deleting successfull, filename " + filename, Toast.LENGTH_LONG).show();
            return true;
        }
        else {
            Toast.makeText(ctx, "Deleting is not successfull, filename " + filename, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static boolean writeFileSD(Context ctx, String filename, String content) {

        File notePath = getNotePathInDevice();
        if(!notePath.exists())
            notePath.mkdirs();

        File sdFile = new File(notePath, filename);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            bw.write(content);
            bw.close();
            Log.d(exportTag, "Файл успешно записан на SD: " + sdFile.getAbsolutePath());
            Toast.makeText(ctx, "Export was successfull with filename " + filename, Toast.LENGTH_LONG).show();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ctx, "Export was continue with unknown error", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static boolean containsFile(String header) {
        File notePath = getNotePathInDevice();
        if(!notePath.exists())
            return false;
        else {
            for (File f : notePath.listFiles()) {
                if (f.isFile() && f.getName().equals(header))
                    return true;
            }
        }
        return false;
    }

    public static File getNotePathInDevice() {
        // Path to SD
        File sdPath = getSdPath();
        // Creating Own catalog
        sdPath = new File(sdPath.getAbsolutePath() + "/" + notesDir);
        return sdPath;
    }

    public static File getImagePathInDevice() {
        // Path to SD
        File sdPath = getSdPath();
        // Creating Own catalog
        sdPath = new File(sdPath.getAbsolutePath() + "/" + imagesDir);
        return sdPath;
    }

    public static File getImagePathInDevice(boolean large) {
        // Path to SD
        File sdPath = getSdPath();
        // Creating Own catalog
        sdPath = new File(sdPath.getAbsolutePath() + "/" + imagesDir + "/" + (large ? imagesLarge : imagesSmall));
        return sdPath;
    }

    public static File getSdPath() {
       return Environment.getExternalStorageDirectory();
    }

    public static Uri getImageUri(String header) {
        return Uri.parse("content://" + LaunchApplication.getInstance().getPackageName() + "/image/" + header);
    }

    public static void saveBitmap(Bitmap yourSelectedImage, String header) {
        File globalImagePath = getImagePathInDevice();
        if(!globalImagePath.exists())
            globalImagePath.mkdirs();
        File smallImagePath = getImagePathInDevice(false);
        if(!smallImagePath.exists())
            smallImagePath.mkdirs();
        File largeImagePath = getImagePathInDevice(true);
        if(!largeImagePath.exists())
            largeImagePath.mkdirs();

        saveFileInDirectory(largeImagePath, header, yourSelectedImage, false);
        saveFileInDirectory(smallImagePath, header, yourSelectedImage, true);

    }

    private static void saveFileInDirectory(File smallImagePath, String header, Bitmap bitmap, Boolean needCrop) {
        File file = new File (smallImagePath, header);
        if (file.exists ()) file.delete();
        try {
            Bitmap resizeBitMap = needCrop ? Bitmap.createScaledBitmap(bitmap, 120, 120, false) : bitmap;
            FileOutputStream out = new FileOutputStream(file);
            resizeBitMap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getSavedBitmap(String header, boolean large) {
        Bitmap bitmap=null;
        File f = new File(Utils.getImagePathInDevice(large).getAbsolutePath() + "/"+header);
        if (!f.exists())
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public static void renameFiles(String oldName, String newName) {
        File fLargeOld = new File(getImagePathInDevice(true),oldName);
        File fLargeNew = new File(getImagePathInDevice(true),newName);
        rename(fLargeOld, fLargeNew);
        File fSmallOld = new File(getImagePathInDevice(false),oldName);
        File fSmallNew = new File(getImagePathInDevice(false),newName);
        rename(fSmallOld, fSmallNew);
    }

    private static boolean rename(File from, File to) {
        return from.getParentFile().exists() && from.exists() && from.renameTo(to);
    }
}

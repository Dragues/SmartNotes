package com.example.chist.testprojectmosru.Application;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.example.chist.testprojectmosru.NotesActivityPackage.DBHelper;
import com.example.chist.testprojectmosru.NotesActivityPackage.Note;
import com.example.chist.testprojectmosru.R;
import com.vk.sdk.VKAccessToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
        switch (marker) {
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
        } else {
            return writeFileSD(ctx, filename, content);
        }
    }

    public static boolean deleteFileFromSd(Context ctx, String filename) {
        File notePath = getNotePathInDevice();
        if (!notePath.exists())
            return false;

        File sdFile = new File(notePath, filename);
        if (sdFile.delete()) {
            Toast.makeText(ctx, "Deleting successfull, filename " + filename, Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(ctx, "Deleting is not successfull, filename " + filename, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    // delete saved images from memory
    public static void deletesCachedImages(Context ctx, String filename) {

        File notePathLarge = getImagePathInDevice(true);
        File notePathSmall = getImagePathInDevice(false);

        File sdFileLarge = new File(notePathLarge, filename);
        File sdFileSmall = new File(notePathSmall, filename);

        // need call it subsequently
        boolean a = sdFileLarge.delete();
        boolean b = sdFileSmall.delete();
        if (a || b)
            Toast.makeText(ctx, "Related cached files was deleted " + filename, Toast.LENGTH_LONG).show();

    }

    public static boolean writeFileSD(Context ctx, String filename, String content) {
        File notePath = getNotePathInDevice();
        if (!notePath.exists())
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
        if (!notePath.exists())
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
        return new File(getSdPath().getAbsolutePath() + "/" + notesDir);
    }

    public static File getImagePathInDevice() {
        return new File(getSdPath().getAbsolutePath() + "/" + imagesDir);
    }

    public static File getImagePathInDevice(boolean large) {
        return new File(getSdPath().getAbsolutePath() + "/" + imagesDir + "/" + (large ? imagesLarge : imagesSmall));
    }

    public static File getSdPath() {
        return Environment.getExternalStorageDirectory();
    }

    public static Uri getImageUri(Context context, int id) {
        return Uri.parse("content://" + context.getPackageName() + "/image/" + id);
    }

    public static void saveBitmap(Bitmap yourSelectedImage, String header) {
        File globalImagePath = getImagePathInDevice();
        if (!globalImagePath.exists())
            globalImagePath.mkdirs();
        File smallImagePath = getImagePathInDevice(false);
        if (!smallImagePath.exists())
            smallImagePath.mkdirs();
        File largeImagePath = getImagePathInDevice(true);
        if (!largeImagePath.exists())
            largeImagePath.mkdirs();

        saveFileInDirectory(largeImagePath, header, yourSelectedImage, false);
        saveFileInDirectory(smallImagePath, header, yourSelectedImage, true);
    }

    private static void saveFileInDirectory(File smallImagePath, String header, Bitmap bitmap, Boolean needCrop) {
        File file = new File(smallImagePath, header);
        if (file.exists()) file.delete();
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

    // get seved cache notes images
    public static Bitmap getSavedBitmap(int id, boolean large) {
        Bitmap bitmap = null;
        File f = new File(Utils.getImagePathInDevice(large).getAbsolutePath() + "/" + id);
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

    // Prepare Base Columns
    public static ContentValues prepareContentValues(int id, String header, String body, int marker) {
        ContentValues values = new ContentValues();
        if (id != -1)
            values.put(DBHelper.NoteColumns.ID, id);
        if (header != null)
            values.put(DBHelper.NoteColumns.HEADER, header);
        if (body != null)
            values.put(DBHelper.NoteColumns.BODY, body);
        values.put(DBHelper.NoteColumns.MARKER, marker);
        return values;
    }

    // Firstly i use the id-field for identify note from sqlite. It wasn't good idea.
    // Method can be useful =)
    public static void renameFiles(String oldName, String newName) {
        File fLargeOld = new File(getImagePathInDevice(true), oldName);
        File fLargeNew = new File(getImagePathInDevice(true), newName);
        rename(fLargeOld, fLargeNew);
        File fSmallOld = new File(getImagePathInDevice(false), oldName);
        File fSmallNew = new File(getImagePathInDevice(false), newName);
        rename(fSmallOld, fSmallNew);
    }

    private static boolean rename(File from, File to) {
        return from.getParentFile().exists() && from.exists() && from.renameTo(to);
    }

    // delete directory and all files into it.
    public static void deleteAllFilesInDir(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                child.delete();
    }

    public static Uri getGeoDataUriAdapter(Context context) {
        return Uri.parse("content://" + context.getPackageName() + "/geo/adapter");
    }

    public static Uri getGeoDataUri(Context context) {
        return Uri.parse("content://" + context.getPackageName() + "/geo");
    }

    public static ContentValues prepareContentValues(Note noteNew) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.NoteColumns.ID, noteNew.id);
        values.put(DBHelper.NoteColumns.HEADER, noteNew.header);
        values.put(DBHelper.NoteColumns.MARKER, noteNew.marker);
        values.put(DBHelper.NoteColumns.BODY, noteNew.body);
        values.put(DBHelper.NoteColumns.MAPX, noteNew.x);
        values.put(DBHelper.NoteColumns.MAPY, noteNew.y);
        return values;
    }

    // get values from cursor
    public static ContentValues getContentValuesFromCursor(Cursor c) {
        ContentValues cv = new ContentValues();
        cv.put(DBHelper.NoteColumns.HEADER, c.getString(c.getColumnIndex(DBHelper.NoteColumns.HEADER)));
        cv.put(DBHelper.NoteColumns.BODY, c.getString(c.getColumnIndex(DBHelper.NoteColumns.BODY)));
        cv.put(DBHelper.NoteColumns.MARKER, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.MARKER)));
        cv.put(DBHelper.NoteColumns.TIME, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.TIME)));
        cv.put(DBHelper.NoteColumns.MAPX, c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPX)));
        cv.put(DBHelper.NoteColumns.MAPY, c.getDouble(c.getColumnIndex(DBHelper.NoteColumns.MAPY)));
        cv.put(DBHelper.NoteColumns.ID, c.getInt(c.getColumnIndex(DBHelper.NoteColumns.ID)));
        return cv;
    }

    public static int getID() {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        return vkAccessToken != null ? Integer.parseInt(vkAccessToken.userId) : 0;
    }


    public static Bitmap getScaledBitMapBaseOnScreenSize(Context ctx, Bitmap bitmapOriginal) {

        Bitmap scaledBitmap = null;
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);


            int width = bitmapOriginal.getWidth();
            int height = bitmapOriginal.getHeight();

            float scaleWidth = metrics.scaledDensity;
            float scaleHeight = metrics.scaledDensity;

            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(scaleWidth, scaleHeight);

            // recreate the new Bitmap
            scaledBitmap = Bitmap.createBitmap(bitmapOriginal, 0, 0, width, height, matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scaledBitmap;
    }

    // Dangerous method
    public static String getAddressFromCoords(Context ctx, int latitude, int longitude){
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String zip = addresses.get(0).getPostalCode();
            String country = addresses.get(0).getCountryName();
            return state + ", " +zip + ", " +  city + ", " + country;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

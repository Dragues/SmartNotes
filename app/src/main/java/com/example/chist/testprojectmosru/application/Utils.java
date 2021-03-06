package com.example.chist.testprojectmosru.application;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.example.chist.testprojectmosru.R;
import com.example.chist.testprojectmosru.data.DatabaseHelper;
import com.vk.sdk.VKAccessToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, needCrop ? 60 : 100, out);
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
    public static String getAddressFromCoords(Context ctx, int latitude, int longitude) {
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String zip = addresses.get(0).getPostalCode();
            String country = addresses.get(0).getCountryName();
            return state + ", " + zip + ", " + city + ", " + country;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void runUi(Runnable run) {
        new Handler(Looper.getMainLooper()).post(run);
    }

    public static String getDateFromMillis(long time) {
        //1322018752992-Nov 22, 2011 9:25:52 PM
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Central"));
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }

    public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        int MAX_IMAGE_DIMENSION = (int)context.getResources().getDimension(R.dimen.max_bitmap_height);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }
}

package com.example.chist.testprojectmosru.Application;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.chist.testprojectmosru.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by 1 on 28.02.2017.
 */
public class Utils {

    public static String notesDir = "notes";
    public static String exportTag = "DATAEXPORT";
    private static File notePathInDevice;

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
        File sdPath = Environment.getExternalStorageDirectory();
        // Creating Own catalog
        sdPath = new File(sdPath.getAbsolutePath() + "/" + notesDir);
        return sdPath;
    }
}

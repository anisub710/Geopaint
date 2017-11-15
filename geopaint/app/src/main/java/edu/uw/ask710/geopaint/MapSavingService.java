package edu.uw.ask710.geopaint;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Anirudh Subramanyam on 11/14/2017.
 */

public class MapSavingService extends IntentService {
    private static final String TAG = "MapSaving";
    private static final int WRITE_REQUEST_CODE = 1;
    private Handler mHandler;


    public MapSavingService() {
        super("MapSavingService");
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "Service started");
        mHandler = new Handler();
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Toast.makeText(this, "Intent received", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Intent received");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(TAG, "Handling Intent");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String saveFile = sharedPreferences.getString(MapsActivity.PREF_FILE_KEY, null) + ".geojson";
        String converted = intent.getStringExtra(MapsActivity.CONVERTED_KEY);

//        Log.v(TAG, "Here is the converted GEOJSON: " + converted);
//        Log.v(TAG, "Here is the file name " + saveFile);

        if(isExternalStorageWritable()){
            saveToExternalFile(saveFile, converted);
//            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
//            }else{
//                //CHECK HOW TO DO THIS!!!!!
////                ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
//            }
        }


    }

    private void saveToExternalFile(String saveFile, String converted){
        try{
            File dir = getExternalFilesDir(null);
            if(!dir.exists()){
                dir.mkdirs();
            }
            File file = new File(dir, saveFile);
            if(file.exists()){
                file.delete();
            }
            PrintWriter out = new PrintWriter(new FileWriter(file, true));
            out.println(converted);
            out.close();
            Log.v(TAG, "Saved file successfully to " + saveFile);
        }catch(IOException ioe){
            Log.d(TAG, Log.getStackTraceString(ioe));
        }
    }

    public static boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }
}
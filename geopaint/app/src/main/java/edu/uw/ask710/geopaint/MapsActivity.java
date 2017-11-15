package edu.uw.ask710.geopaint;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class  MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    private PolylineOptions polyline;
    private SharedPreferences sharedPreferences;
    private GeoJsonConverter converter;
    private List<Polyline> polylineList;
    public static final String TAG = "MapsActivity";
    private static final int LOCATION_REQUEST_CODE = 1;
    private String fileName;
    private String geojson;
    private boolean penDown = false;
    private List<PolylineOptions> resultList;
//    private PolylineOptions old;
//    public static final String PREF_PEN_KEY = "pref_pen";
    public static final String CHOSEN_COLOR = "chosen_color";
    public static final String PREF_FILE_KEY = "pref_file";
    public static final String CONVERTED_KEY = "converted";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        polylineList = new ArrayList<Polyline>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPreferences.getString(PREF_FILE_KEY, null) == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter file name to store this art: ");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    fileName = input.getText().toString();
                    sharedPreferences.edit().putString(PREF_FILE_KEY, fileName).commit();
                    Log.v(TAG, "This is the file name: " + fileName);
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }else{
            Uri getFile = Uri.fromFile(new File(getExternalFilesDir(null).getAbsolutePath() +
                    File.separator + sharedPreferences.getString(PREF_FILE_KEY, null) + ".geojson"));
            File read = new File(getFile.getPath());
            try{
                FileInputStream inputStream = new FileInputStream(new File(read.getAbsolutePath()));
                if(inputStream != null){
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String result = "";
                    StringBuilder stringBuilder = new StringBuilder();
                    while((result = bufferedReader.readLine()) != null){
                        stringBuilder.append(result);
                    }
                    inputStream.close();
                    geojson = stringBuilder.toString();
                    Log.v(TAG, "I'm here: " + geojson);
                    resultList = converter.convertFromGeoJson(geojson);
//                    old = resultList.get(0);
//                    Log.v(TAG, "This is the geojson: " + resultList.get(0));
                }
            }catch(FileNotFoundException e){
                Log.d(TAG, "File not found: " + e.toString());
            }catch(IOException ioe){
                Log.d(TAG, Log.getStackTraceString(ioe));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

//        if(!sharedPreferences.getBoolean(PREF_PEN_KEY, false)){
//            sharedPreferences.edit().putBoolean(PREF_PEN_KEY, false);
////            polyline = new PolylineOptions()
////                    .width(25)
////                    .color(sharedPreferences.getInt(CHOSEN_COLOR, Color.BLUE));
//        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.setRetainInstance(true);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if(resultList != null){
            for(int i = 0; i < resultList.size(); i++){
                PolylineOptions old = resultList.get(i);
                Log.v(TAG, "Old polyline: "  + old);
                mMap.addPolyline(old);
            }
        }
    }

    @Override
    protected void onStart() {
//        polyline = new PolylineOptions()
//                .width(25)
//                .color(sharedPreferences.getInt(CHOSEN_COLOR, Color.BLUE));
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.setType("text/plain");
        Uri shareFile = Uri.fromFile(new File(getExternalFilesDir(null).getAbsolutePath() +
                File.separator + sharedPreferences.getString(PREF_FILE_KEY, null) + ".geojson"));
        myShareIntent.putExtra(Intent.EXTRA_STREAM, shareFile);
        mShareActionProvider.setShareIntent(myShareIntent);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
                return true;

            case R.id.toggle_pen:
                if(penDown){
                    item.setTitle("Lower Pen");
                    penDown = false;
                }else{
                    polyline = new PolylineOptions()
                            .width(25)
                            .color(sharedPreferences.getInt(CHOSEN_COLOR, Color.BLUE));
                    item.setTitle("Raise Pen");
                    penDown = true;
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "Location services connected.");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(locationPermission == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            Log.v(TAG, "latitude: " + lat + ", longitude: " + lng);
            LatLng latLng = new LatLng(lat, lng);
//            MarkerOptions options = new MarkerOptions()
//                    .position(latLng)
//                    .title("Current Location");
//            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

//            if(sharedPreferences.getBoolean(PREF_PEN_KEY, true)) {
            if(penDown){
                if(polyline == null){
                    polyline = new PolylineOptions()
                            .width(25)
                            .color(sharedPreferences.getInt(CHOSEN_COLOR, Color.BLUE));
                }
                polyline.add(latLng);
                Polyline line = mMap.addPolyline(polyline);
                if(polylineList != null){
                    polylineList.clear();
                }
                polylineList.add(line);
                Log.v(TAG, "List size: " + polylineList.size());
                String converted = converter.convertToGeoJson(polylineList);
                Intent intent = new Intent(MapsActivity.this, MapSavingService.class);
                intent.putExtra(CONVERTED_KEY, converted);
                startService(intent);
            }
        }
    }

    public void draw(){

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    onConnected(null);
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

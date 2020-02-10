package ru.denfad.mapgamev02;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;


public class MyService extends Service implements LocationListener,OnMapReadyCallback {
    private double x = 0, y = 0;
    private static List<LatLng> LatLng = new ArrayList<>();




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);
        Log.i("Inservice", "I working");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Inservice", "I stop working");

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            y = location.getLongitude();
            x = location.getLatitude();
            LatLng.add(new LatLng(x,y));
            Log.i("Inservice", "I get LatLng");

        }
        else{
            x=180;
            y=180;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
    public static List<com.google.android.gms.maps.model.LatLng> getLetLng(){
        return LatLng;
    }
}

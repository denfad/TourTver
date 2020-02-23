package ru.denfad.mapgamev02;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.denfad.mapgamev02.model.Sight;

import static android.location.LocationManager.GPS_PROVIDER;
import static java.lang.String.format;


public class MainActivity extends Activity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    MapFragment mapFragment;


    private BottomSheetBehavior mBottomSheetBehavior;

    private GoogleMap map;

    private List<Sight> sightList = new ArrayList<>();

    private Sight activeSight=new Sight();

    private TextView nameText;
    private TextView typeText;
    private TextView textText;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);

        textText=findViewById(R.id.text);
        nameText=findViewById(R.id.name);
        typeText=findViewById(R.id.type);
        View bottomSheet = findViewById(R.id.bottom_sheet);


        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    protected void onRestart() {
        super.onRestart();


        stopService(new Intent(this, MyService.class));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Thread thread = new Thread(new ActiveSightThread(marker.getTitle()));
        thread.start();
        if (sightList != null) {
            for (Sight s : sightList) {
                if (s.getName().equals(marker.getTitle())) {
                    nameText.setText(s.getName());
                    typeText.setText(s.getType());
                    textText.setText(s.getText());
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
            }
        }


        return true;
    }


    private class ActiveSightThread implements Runnable{
        private String name;
        ActiveSightThread(String name){
            this.name=name;
        }
        @Override
        public void run() {
            synchronized (activeSight) {

            }
        }
    }
    private  class HttpThread extends AsyncTask<Void, Void, List<Sight>> {

        @Override
        protected void onPostExecute(List<Sight> sights) {
            sightList=sights;
            generateMarkers();
        }

        @Override
        protected List<Sight> doInBackground(Void... voids) {
            OkHttpClient httpClient = new OkHttpClient();
            //ipconfig
            Request request = new Request.Builder()
                    .url("http://192.168.1.4:8080/sight/")
                    .get()
                    .build();

            List<Sight> sights = new ArrayList<>();

            try(Response response = httpClient.newCall(request).execute()) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                sights = Arrays.asList(gsonBuilder.create().fromJson(response.body().string(), Sight[].class));

            } catch (IOException e) {
                e.printStackTrace();

            }

            return sights;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        startService(new Intent(MainActivity.this, MyService.class));
        Log.i("servixe", "I working");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    //Создание карты с заданными настройками из setUpMap
    @Override
    public void onMapReady(GoogleMap retMap) {
        map = retMap;
        setUpMap();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    //Настройки карты
    public void setUpMap() {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMarkerClickListener(this);
        new HttpThread().execute();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);

    }

    private void generateMarkers(){
        if(sightList!=null) {
            for (Sight s : sightList) {
                map.addMarker(new MarkerOptions().title(s.getName()).position(new LatLng(s.getLatitude(), s.getLongitude())));
            }
        }
    }

    //Изменение локации
    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(Location location) {

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
}
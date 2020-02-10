package ru.denfad.mapgamev02;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import java.util.List;
import static android.location.LocationManager.GPS_PROVIDER;
import static java.lang.String.format;


public class MainActivity extends Activity implements OnMapReadyCallback, LocationListener {

    MapFragment mapFragment;
    TextView distance;

    private BottomSheetBehavior mBottomSheetBehavior;
    private double x = 0;
    private double y = 0;
    private double x2 = 0;
    private double y2 = 0;
    private double home_x = 0;
    private double home_y = 0;
    private double generalArea;
    private int numberPoint = 0;
    private boolean Home = true;
    private GoogleMap map;
    private List<Polyline> polyline = new ArrayList<>();
    private List<LatLng> LatLng = new ArrayList<>();

    private AreaSum areaSum = new AreaSum();


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

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (MyService.getLetLng().size() >= 2) {
            polyline.add(map.addPolyline(new PolylineOptions().addAll(MyService.getLetLng())
                    .width(10)
                    .color(R.color.lines)));

        }
        LatLng.addAll(MyService.getLetLng());
        stopService(new Intent(this, MyService.class));

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

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
    }

    //Изменение локации
    @SuppressLint("DefaultLocale")
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (Home) {
                Start_Coordinate(location);
                y2 = location.getLongitude();
                x2 = location.getLatitude();
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Домашние координаты определены", Toast.LENGTH_SHORT);
                toast.show();
                numberPoint++;
                LatLng.add(new LatLng(x2, y2));

            }
            y = location.getLongitude();
            x = location.getLatitude();
            if (homeReturn(x, y) && numberPoint >= 3) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Вы дома", Toast.LENGTH_SHORT);
                toast.show();
                LatLng.add(new LatLng(x2, y2));
                generalArea = areaSum.getGeneralArea(LatLng);
                Log.i("Area sum", "Площадь- " + generalArea);

            }

            if (outHome(x, x2, y, y2)) {
                DrawLine(x, y, x2, y2);
                x2 = x;
                y2 = y;
                numberPoint++;

            }


        } else {
            x = 180;
            y = 180;
        }
    }

    public void Start_Coordinate(Location location) {
        if (Home) {
            home_y = location.getLongitude();
            home_x = location.getLatitude();
            Home = false;
        }
    }


    //возвращает true или false в зависимотси от того дома игрок или нет
    public boolean homeReturn(double x, double y) {
        return (x - home_x) * (x - home_x) + (y - home_y) * (y - home_y) <= (2.0 / 3600.0) * (2.0 / 3600.0);
    }

    //рисует всю территорию удаляя линии
    public void DrawArea() {
        DrawLine(x, y, x2, y2);
        for (Polyline line : polyline) {
            line.remove();
        }
        polyline.get(polyline.size() - 1).remove();
        map.addPolygon(new PolygonOptions().addAll(LatLng)
                .strokeWidth(10)
                .strokeColor(R.color.lines)
                .fillColor(R.color.fill));
    }

    //рисует линии
    public void DrawLine(double x, double y, double x2, double y2) {
        map.addPolyline(new PolylineOptions().add(new LatLng(x2, y2), new LatLng(x, y))
                .width(10)
                .color(R.color.lines));
    }

    //определяет прошли ли мы больше 0.5 угловой секунды
    public boolean outHome(double x, double x2, double y, double y2) {
        return Math.abs(x2 - x) >= (0.5 / 3600.0) || Math.abs(y2 - y) >= (0.5 / 3600.0);
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
package ru.denfad.mapgamev02;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class AreaSum {


    private double middleArea;



    private double sumArea(List<LatLng> latLngList){
        double generalArea = 0;
        for (int i=0; i<latLngList.size();i++) {
            if(i==latLngList.size()-1) {
                generalArea=generalArea+ middleAreaSum(latLngList.get(i).latitude, latLngList.get(i).longitude, latLngList.get(0).latitude, latLngList.get(0).longitude);
            }
            else{
                generalArea=generalArea+middleAreaSum(latLngList.get(i).latitude, latLngList.get(i).longitude, latLngList.get(i+1).latitude, latLngList.get(i+1).longitude);
            }
        }
        return generalArea;
    }

    public double getGeneralArea(List<LatLng> latLngList){
        return sumArea(latLngList);

    }

    private double middleAreaSum(double x,double y, double x2, double y2){
        middleArea=0.5*(y+y2)*(x2-x);
        return middleArea;
    }

}

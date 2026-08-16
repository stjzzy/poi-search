package com.example.poisearch;

import com.example.poisearch.model.POI;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态数据持有者 - 用于在Activity间传递大量POI数据
 * 避免Intent传递大数据导致的TransactionTooLargeException崩溃
 */
public class POIDataHolder {
    private static POIDataHolder instance;
    private List<POI> poiList;
    private double centerLat;
    private double centerLng;
    private int radius;
    private double startLat;
    private double startLng;

    private POIDataHolder() {}

    public static synchronized POIDataHolder getInstance() {
        if (instance == null) {
            instance = new POIDataHolder();
        }
        return instance;
    }

    public static void setData(List<POI> pois, double lat, double lng, int r) {
        getInstance();
        instance.poiList = pois != null ? new ArrayList<>(pois) : new ArrayList<>();
        instance.centerLat = lat;
        instance.centerLng = lng;
        instance.radius = r;
    }

    public List<POI> getPoiListInstance() {
        return poiList;
    }

    public static List<POI> getPoiList() {
        return getInstance().poiList;
    }

    public static double getCenterLat() {
        return getInstance().centerLat;
    }

    public static double getCenterLng() {
        return getInstance().centerLng;
    }

    public static int getRadius() {
        return getInstance().radius;
    }

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLng() {
        return startLng;
    }

    public void setStartLng(double startLng) {
        this.startLng = startLng;
    }

    public static void clear() {
        getInstance().poiList = null;
    }
}

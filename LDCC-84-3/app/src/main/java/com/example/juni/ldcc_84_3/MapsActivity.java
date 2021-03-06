package com.example.juni.ldcc_84_3;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<String> sevenArr;
    ArrayList<String> xposArr;
    ArrayList<String> yposArr;
    String curx;
    String cury;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent intent = getIntent();
        sevenArr = intent.getStringArrayListExtra("sevenarr");
        xposArr = intent.getStringArrayListExtra("xposarr");
        yposArr = intent.getStringArrayListExtra("yposarr");
        curx = intent.getStringExtra("curx");
        cury = intent.getStringExtra("cury");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        for(int i =0; i < sevenArr.size(); i++){
            LatLng position = new LatLng(Float.valueOf(xposArr.get(i)), Float.valueOf(yposArr.get(i)));
            mMap.addMarker(new MarkerOptions().position(position).title(getResources().getStringArray(R.array.seven_name)[Integer.valueOf(sevenArr.get(i)) - 1])
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        LatLng position = new LatLng(Float.valueOf(curx), Float.valueOf(cury));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16.0f));
        mMap.addMarker(new MarkerOptions().
                position(position).
                title(getResources().getString(R.string.currentpos)).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
}

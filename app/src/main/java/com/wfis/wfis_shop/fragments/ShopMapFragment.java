package com.wfis.wfis_shop.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener;
import com.wfis.wfis_shop.Common.Common;
import com.wfis.wfis_shop.R;
import com.wfis.wfis_shop.core.BaseFragment;
import com.wfis.wfis_shop.models.City;
import com.wfis.wfis_shop.models.Map;
import com.wfis.wfis_shop.models.Shop;
import com.wfis.wfis_shop.utils.ShopClusterRenderer;

import java.util.ArrayList;
import java.util.List;

public class ShopMapFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ClusterManager.OnClusterItemInfoWindowClickListener<Shop>, ClusterManager.OnClusterClickListener<Shop>, ClusterManager.OnClusterItemClickListener<Shop>, GoogleMap.OnCameraIdleListener {

    private MapView mapView;
    private GoogleMap googleMap;
    private ClusterManager<Shop> clusterManager;
    private Map map;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    public static ShopMapFragment newInstance() {

        Bundle args = new Bundle();

        ShopMapFragment fragment = new ShopMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_map, container, false);
        map = new Map();
        database= FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Common.city+"/"+"Map");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                map = dataSnapshot.getValue(Map.class);
//                Log.e("title", map.title);
//                Log.e("lat", map.lat.toString());
//                Log.e("lng", map.lng.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Read Fail", "Error");
            }
        });



        mapView = view.findViewById(R.id.fragment_shop_map_map);
        mapView.getMapAsync(this);
        mapView.onCreate(savedInstanceState);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (googleMap != null) {
            googleMap.setOnMarkerClickListener(null);
        }
        clusterManager.setOnClusterClickListener(null);
        clusterManager.setOnClusterItemClickListener(null);
        clusterManager.setOnClusterItemInfoWindowClickListener(null);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        clusterManager.onMarkerClick(marker);
        Toast.makeText(getContext(),marker.getTitle(),Toast.LENGTH_SHORT).show();
        CameraPosition MARKER_POS = new CameraPosition.Builder()
                .target(marker.getPosition())
                .zoom(16)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(MARKER_POS));
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener(this);
        this.googleMap.setOnCameraIdleListener(this::onCameraIdle);



        this.googleMap.addMarker(new MarkerOptions()
                .title(map.title)
                .position(new LatLng(map.lat, map.lng))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        );

        centerOnPoland();
        clusterManager = new ClusterManager(getContext(), googleMap);
        clusterManager.clearItems();
//        clusterManager.addItems(testShops());
        clusterManager.setRenderer(new ShopClusterRenderer(getContext(), googleMap, clusterManager));

        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);
        clusterManager.setOnClusterItemInfoWindowClickListener(this);
        clusterManager.cluster();
    }
    //51.064949, 19.442058
    private void centerOnPoland() {
        LatLngBounds.Builder builder = LatLngBounds.builder();
//        builder.include(new LatLng(51.089150, 19.525181));
//        builder.include(new LatLng(51.040575, 19.396054));
        builder.include(new LatLng(map.lat - 0.024201, map.lng - 0.083123));
        builder.include(new LatLng(map.lat + 0.024374, map.lng + 0.046004));
        LatLngBounds bounds = builder.build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));


    }
    //PUNKTY NA MAPIE
//    private List<Shop> testShops() {
//        List<Shop> list = new ArrayList<>();
//        Shop shop;
//        shop = new Shop();
//        shop.setLat(51.067894);
//        shop.setLon(19.444650);
//        shop.setName("Museum" );
//
//        list.add(shop);
////        for (int i = 0; i < 20; i++) {
////            shop = new Shop();
////            shop.setLat(51.777003 + (i * 0.002));
////            shop.setLon(19.489993 + (i * 0.002));
////            shop.setName("shop " + i);
////            list.add(shop);
////        }
//
//        return list;
//    }


    @Override
    public boolean onClusterItemClick(Shop shop) {
        if (shop == null || googleMap == null) {
            return true;
        }

//        show info window
        Marker marker = ((ShopClusterRenderer) clusterManager.getRenderer()).getMarker(shop);
        marker.setTag(shop);
//        marker.showInfoWindow();


        //animate camera to position
        LatLng shopPos = new LatLng(shop.getLat(), shop.getLon());
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(shopPos), 300, null);

        return true;
    }

    @Override
    public void onClusterItemInfoWindowClick(Shop shop) {

    }

    @Override
    public void onCameraIdle() {
        clusterManager.onCameraIdle();
    }

    @Override
    public boolean onClusterClick(Cluster<Shop> cluster) {
        if (googleMap == null) {
            return false;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ClusterItem place : cluster.getItems()) {
            builder.include(place.getPosition());
        }
        LatLngBounds bounds = builder.build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 5);
        googleMap.animateCamera(cameraUpdate);

        return true;
    }
}

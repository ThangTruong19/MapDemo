package com.thangtruong19.mapdemo2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.thangtruong19.mapdemo2.model.PlaceInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE=99;

    private static final String TAG="MapActivity";

    private AutoCompleteTextView searchText;
    private ImageView mGps;
    private PlaceAutoCompleteAdapter placeAutoCompleteAdapter;
    protected GeoDataClient mGeoDataClient;
    private static final LatLngBounds LAT_LNG_BOUNDS=new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));
    private PlaceInfo mPlace;
    private Marker mMarker;
    private static final int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        searchText=findViewById(R.id.input_search);
        searchText.setOnItemClickListener(mAutoCompleteClickListener);

        mGps=findViewById(R.id.ic_gps);



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildGoogleApiClient();
        mGeoDataClient= Places.getGeoDataClient(this,null);
        initSearchText();
    }

    private  void searchNearByPlace(final double latitude, final double longtitude){

        String url=getUrl(latitude,longtitude);
        Log.d(TAG,"url when click button: "+url);
        System.out.println("Url for search nearby place : "+url);
        Object dataTransfer[]=new Object[2];
        dataTransfer[0]=mMap;
        dataTransfer[1]=url;

        GetNearbyPlacesData getNearbyPlacesData=new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);
        System.out.print(url);
    }

    private String getUrl(double latitude,double longtitude){
        StringBuilder googlePlaceUrl=new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longtitude);
        googlePlaceUrl.append("&radius="+5000);
        googlePlaceUrl.append("&type="+"cafe");
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyBqa-GmqP0BruW9ghJnfrG-WyNKsKS9UTM");


        return googlePlaceUrl.toString();

    }

    private void initSearchText(){
        Log.d(TAG,"InitSearchText: init");

        placeAutoCompleteAdapter=new PlaceAutoCompleteAdapter(MapsActivity.this,mGeoDataClient,LAT_LNG_BOUNDS,null);
        searchText.setAdapter(placeAutoCompleteAdapter);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH
                        ||actionId==EditorInfo.IME_ACTION_DONE
                        ||event.getAction()==KeyEvent.ACTION_DOWN
                        ||event.getAction()==KeyEvent.KEYCODE_ENTER){
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: click gps icon");
                mMap.clear();
                locationRequest= new LocationRequest();

                locationRequest.setInterval(1000);
                locationRequest.setFastestInterval(1000);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, MapsActivity.this);
                }
            }
        });

    }

    private void geoLocate(){
        Log.d(TAG,"geoLocate: geolocate");

        String searchString=searchText.getText().toString();

        Geocoder geocoder=new Geocoder(MapsActivity.this);
        List<Address>list =new ArrayList<>();
        try{
            list=geocoder.getFromLocationName(searchString,1);
        }catch(IOException e){
            e.printStackTrace();
        }

        if(list.size()>0){
            Address address=list.get(0);
            Log.d(TAG,"found a location: "+address.toString());

            LatLng latLng=new LatLng(address.getLatitude(),address.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));

            mMap.clear();
            searchNearByPlace(address.getLatitude(),address.getLongitude());

            MarkerOptions options=new MarkerOptions()
                    .position(latLng)
                    .title(address.getAddressLine(0));

            mMap.addMarker(options);

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        if(client==null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(this,"Permission Denied!",Toast.LENGTH_LONG).show();
                }
        }
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
    }

    protected synchronized void buildGoogleApiClient(){
        client=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        // client.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation=location;

        if(currentLocationMarker!=null){
            currentLocationMarker.remove();
        }

        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());

        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        currentLocationMarker=mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,20));
        searchNearByPlace(location.getLatitude(),location.getLongitude());

        //mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
        if(client!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest= new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            return false;
        }else{
            return true;
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private AdapterView.OnItemClickListener mAutoCompleteClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item=placeAutoCompleteAdapter.getItem(position);
            final String placeId=item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult=Places.GeoDataApi
                    .getPlaceById(client,placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback=new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG,"onResult: Place query did not complete successfully"+places.getStatus().toString());
                places.release();
                return;
            }
            final Place place=places.get(0);

            try {
                mPlace = new PlaceInfo(place.getName().toString(), place.getAddress().toString(), place.getPhoneNumber().toString()
                        , place.getId(), place.getWebsiteUri(), place.getLatLng(), place.getRating());

                Log.d(TAG, "onResult: place details: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG,"onResult: NullPointerException: "+e.getMessage());
            }
            /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPlace.getLatLng(),20));
            MarkerOptions options=new MarkerOptions()
                    .position(mPlace.getLatLng())
                    .title(place.getName().toString());

            mMap.addMarker(options);*/
            moveCamera(mPlace.getLatLng(),20,mPlace);
            places.release();

        }
    };

    private void moveCamera(LatLng latLng,int zoom,PlaceInfo placeInfo){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        mMap.clear();
        searchNearByPlace(latLng.latitude,latLng.longitude);
        //mMap.setInfoWindowAdapter(new CustomWindowInfoAdapter(MapsActivity.this));

        if(placeInfo!=null){
            try{
                String snippet="Address: "+placeInfo.getAddress()+"\n"
                        +"Phone Number: "+placeInfo.getPhoneNumber()+"\n"
                        +"Website: "+placeInfo.getWebsiteUri()+"\n"
                        +"Price Rating: "+placeInfo.getRating()+"\n";

                MarkerOptions options=new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);

                mMarker=mMap.addMarker(options);
            }catch (NullPointerException e){
                Log.e(TAG,"moveCamera: NullPointerException: "+e.getMessage());
            }
        }else {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
    }
}

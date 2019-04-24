package com.sccodesoft.dago;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sccodesoft.dago.Common.Common;
import com.sccodesoft.dago.Helper.DirectionJSONParser;
import com.sccodesoft.dago.Model.DataMessage;
import com.sccodesoft.dago.Model.FCMResponse;
import com.sccodesoft.dago.Model.Token;
import com.sccodesoft.dago.Remote.IFCMServices;
import com.sccodesoft.dago.Remote.IGoogleApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverTracking extends FragmentActivity implements
        OnMapReadyCallback {

    private GoogleMap mMap;

    String riderLat;
    String riderLng;
    String destLat;
    String destLng;
    String customerToken, cusID;

    IFCMServices mFCMService;

    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private Circle riderMarker;
    private Marker driverMarker;

    private Polyline direction;

    boolean isKandy;

    IGoogleApi mService;
    IFCMServices mFCMServices;

    GeoFire geoFire;

    Button btnStartTrip;

    Location pickUpLocation;

    TextView txtCurrFare;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;

    //  Date StartTripTime,TempTripTime =null;

    Handler handler;

    int Seconds, Minutes, MilliSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFCMService = Common.getFCMService();

        if (getIntent() != null) {
            riderLat = getIntent().getStringExtra("lat");
            riderLng = getIntent().getStringExtra("lng");
            destLat = getIntent().getStringExtra("destlat");
            destLng = getIntent().getStringExtra("destlng");
            customerToken = getIntent().getStringExtra("customerToken");
            cusID = getIntent().getStringExtra("cusID");
            isKandy = getIntent().getBooleanExtra("isKandy", false);
        }

        txtCurrFare = (TextView) findViewById(R.id.txtCurrFare);

        FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Common.distancefare = 0;
                    Common.waitingtime = 0;
                    Common.totfare = 0;
                    Common.distance =0;
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Rider", cusID);
                    map.put("distancefare", String.valueOf(Common.distancefare));
                    map.put("waitingfare", String.valueOf(Common.waitingtime));
                    map.put("total", String.valueOf(Common.totfare));
                    map.put("pickuplat",riderLat);
                    map.put("pickuplng",riderLng);
                    map.put("destLat",destLat);
                    map.put("destLng",destLng);
                    map.put("customerToken",customerToken);
                    map.put("isKandy",String.valueOf(isKandy));
                    FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(map)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Map<String,String> content = new HashMap<>();
                                    content.put("title","Request Tracked!");
                                    content.put("driverID",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    DataMessage dataMessage = new DataMessage(customerToken,content);

                                    mFCMService.sendMessage(dataMessage)
                                            .enqueue(new Callback<FCMResponse>() {
                                                @Override
                                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                    if(response.body().success==1)
                                                    {
                                                        Toast.makeText(DriverTracking.this, "Request Tracked..", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<FCMResponse> call, Throwable t) {

                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DriverTracking.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Common.distancefare = Double.valueOf(dataSnapshot.child("distancefare").getValue().toString());
                    Common.waitingtime = Double.valueOf(dataSnapshot.child("waitingfare").getValue().toString());
                    Common.totfare = Double.valueOf(dataSnapshot.child("total").getValue().toString());

                    riderLat = dataSnapshot.child("pickuplat").getValue().toString();
                    riderLng = dataSnapshot.child("pickuplng").getValue().toString();
                    destLat = dataSnapshot.child("destLat").getValue().toString();
                    destLng = dataSnapshot.child("destLng").getValue().toString();
                    customerToken = dataSnapshot.child("customerToken").getValue().toString();
                    cusID = dataSnapshot.child("Rider").getValue().toString();
                    isKandy = dataSnapshot.child("isKandy").getValue().equals("true")?true:false;

                    Toast.makeText(DriverTracking.this, "Previous Trip Loaded..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);



        mService = Common.getGoogleAPI();
        mFCMServices = Common.getFCMService();

        handler = new Handler();

        btnStartTrip = (Button) findViewById(R.id.btnStartTrip);
        //  btnWaiting = (Button)findViewById(R.id.btnWaiting);
        /*btnWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnWaiting.getText().equals("START WAITING")) {
                    btnWaiting.setText("STOP WAITING");

                    StartTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 0);

                }else if(btnWaiting.getText().equals("STOP WAITING"))
                {
                    TimeBuff += MillisecondTime;
                    btnWaiting.setText("START WAITING");
                    MillisecondTime = 0L ;
                    StartTime = 0L ;
                    TimeBuff = 0L ;
                    UpdateTime = 0L ;
                    Seconds = 0 ;
                    Minutes = 0 ;
                    MilliSeconds = 0 ;
                    handler.removeCallbacks(runnable);
                }
            }
        });*/

        setUpLocation(riderLat, riderLng);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnStartTrip.getText().equals("START TRIP")) {
                    setUpLocation(destLat, destLng);
                    pickUpLocation = Common.mLastLocation;
                    Common.tempLoc1 = Common.mLastLocation;
                    Common.tempLoc2 = Common.mLastLocation;
                    btnStartTrip.setText("DROP OFF HERE");

                    Common.distancefare = 0;
                    Common.waitingtime = 0;
                    Common.totfare = 0;
                    Common.distance =0;

                    distanceCheck();

                    sendStartNotification(customerToken);

                    StartTime = SystemClock.uptimeMillis();
                    handler.postDelayed(runnable, 5000);

                    // StartTripTime = Calendar.getInstance().getTime();
                } else if (btnStartTrip.getText().equals("DROP OFF HERE")) {
                    handler.removeCallbacks(runnable);
                    FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).
                            child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("reserved").setValue("0");
                    calculateCashFee(pickUpLocation, Common.mLastLocation);
                }
            }
        });
    }

    private void sendStartNotification(String customerToken) {
        Map<String,String> content = new HashMap<>();
        content.put("title","Start Trip!");
        content.put("driverID",FirebaseAuth.getInstance().getCurrentUser().getUid());
        DataMessage dataMessage = new DataMessage(customerToken,content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success==1)
                        {
                            Toast.makeText(DriverTracking.this, "Trip Started..", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Common.tempLoc1 = location;
                        }
                    });


            if(Common.tempLoc1.distanceTo(Common.tempLoc2)<4)
            {

                if (Common.currentDriver.getCarType().equals("DAGO X")) {
                    Common.waitingtime = round(Common.waitingtime + ((0.1) * Common.time_ratex),2);
                } else if (Common.currentDriver.getCarType().equals("DAGO Black")) {
                    Common.waitingtime = round(Common.waitingtime + ((0.1) * Common.time_rateb),2);
                }
            }
            else
            {
                Common.distance = round(Common.distance + Common.tempLoc1.distanceTo(Common.tempLoc2),2);
                distanceCheck();

            }

            Common.totfare = Common.distancefare+Common.waitingtime;

            HashMap<String,String> map = new HashMap<>();
            map.put("Rider", cusID);
            map.put("distancefare",String.valueOf(Common.distancefare));
            map.put("waitingfare",String.valueOf(Common.waitingtime));
            map.put("total",String.valueOf(Common.totfare));
            map.put("pickuplat",riderLat);
            map.put("pickuplng",riderLng);
            map.put("destLat",destLat);
            map.put("destLng",destLng);
            map.put("customerToken",customerToken);
            map.put("isKandy",String.valueOf(isKandy));

            FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(map)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DriverTracking.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            txtCurrFare.setText("Rs. " + Common.totfare);

            Common.tempLoc2 = Common.tempLoc1;

            handler.postDelayed(this, 6000);
        }

    };

    private void distanceCheck() {
        if(Common.distance<=1000 && Common.currentDriver.getCarType().equals("DAGO X") && isKandy)
        {
            Common.distancefare = Common.base_farexk;
        }
        else if(Common.distance<=1000 && Common.currentDriver.getCarType().equals("DAGO X"))
        {
            Common.distancefare = Common.base_farex;
        }
        else if(Common.distance>1000 && Common.currentDriver.getCarType().equals("DAGO X") && isKandy)
        {
            Common.distancefare = round((Common.base_farexk + ((Common.distance-1000)/1000)*Common.distance_ratexk),2);
        }
        else if(Common.distance>1000 && Common.currentDriver.getCarType().equals("DAGO X"))
        {
            Common.distancefare = round((Common.base_farex + ((Common.distance-1000)/1000)*Common.distance_ratex),2);
        }
        else if(Common.distance<=2000 && Common.currentDriver.getCarType().equals("DAGO Black"))
        {
            Common.distancefare = Common.base_fareb;
        }
        else if(Common.distance>2000 && Common.currentDriver.getCarType().equals("DAGO Black"))
        {
            Common.distancefare = round((Common.base_fareb + ((Common.distance-2000)/1000)*Common.distance_rateb),2);
        }
    }

    private void calculateCashFee(final Location pickUpLocation, Location mLastLocation) {

        String requestApi = null;

        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+pickUpLocation.getLatitude()+","+pickUpLocation.getLongitude()+"&"+
                    "destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {

                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");
                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));

                                //Get Time
                                JSONObject time = legsObject.getJSONObject("duration");
                                String time_text = time.getString("text");
                                Integer time_value = Integer.parseInt(time_text.replaceAll("\\D+",""));

                                sendDropOffNotification(customerToken,legsObject.getString("start_address"),legsObject.getString("end_address"),String.valueOf(time_value),String.valueOf(distance_value),Common.formulaPrice(distance_value,time_value));

                                Intent intent = new Intent(DriverTracking.this,TripDetails.class);
                                intent.putExtra("start_address",legsObject.getString("start_address"));
                                intent.putExtra("end_address",legsObject.getString("end_address"));
                                intent.putExtra("time",String.valueOf(time_value));
                                intent.putExtra("distance",String.valueOf(round((Common.distance/1000),2)));
                                intent.putExtra("total",Common.totfare);
                                intent.putExtra("location_start",String.format("%f,%f",pickUpLocation.getLatitude(),pickUpLocation.getLongitude()));
                                intent.putExtra("location_end",String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

                                startActivity(intent);
                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setUpLocation(String lat,String lng)
    {
            buildLocationRequest();
            buildLocationCallBack();
            displayLocation(lat,lng);

    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }


    private void displayLocation(final String lat, final String lng)
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        Common.mLastLocation = location;

                        if(Common.mLastLocation != null)
                        {
                            final double latitude = Common.mLastLocation.getLatitude();
                            final double longitude = Common.mLastLocation.getLongitude();

                            if(driverMarker != null)
                                driverMarker.remove();
                            driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude))
                                    .title("You")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),17.0f));

                            if(direction != null)
                                direction.remove(); // remove old directions
                            getDirection(lat,lng);

                           // displayCashFee(pickUpLocation,Common.mLastLocation,isKandy);
                        }
                        else
                        {
                            Log.d("ERROR","Can't Get Your Location");
                        }

                    }
                });

    }

    private void getDirection(String lat,String lng) {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;

        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.d("REQUEST_API",requestApi);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        try{
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.map_style)
            );

            if(!isSuccess)
                Log.e("ERROR","Map Style Failed To Load!");
        }catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }


        mMap = googleMap;

        riderMarker = mMap.addCircle(new CircleOptions()
                .center(new LatLng(Double.parseDouble(riderLat),Double.parseDouble(riderLng)))
                .radius(100)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));

        //Geo Fencing with 50m radius
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(Common.currentDriver.getCarType()));
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(Double.parseDouble(riderLat),Double.parseDouble(riderLng)),0.05);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(customerToken);
                btnStartTrip.setEnabled(true);
               // btnWaiting.setVisibility(View.VISIBLE);

                Common.distancefare = 0;
                Common.waitingtime = 0;
                Common.totfare = 0;
                Common.distance =0;

                txtCurrFare.setVisibility(View.VISIBLE);
                geoQuery.removeAllListeners();
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());

    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildLocationCallBack() {


        locationCallback =  new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations())
                {
                    Common.mLastLocation = location;
                }

                if(btnStartTrip.getText().equals("DROP OFF HERE"))
                {
                    displayLocation(destLat,destLng);
                }
                else
                {
                    displayLocation(riderLat,riderLng);
                }
            }
        };
    }

  /*  private void displayCashFee(final Location pickUpLocation, Location mLastLocation, final boolean isKandy) {
        String requestApi = null;

        *//*Date currentTime = Calendar.getInstance().getTime();

        long diff = currentTime.getTime() - StartTripTime.getTime();
        final long elapsed_seconds = diff / 1000;

        Log.i("CUUTIME",String.valueOf(currentTime));*//*

        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+pickUpLocation.getLatitude()+","+pickUpLocation.getLongitude()+"&"+
                    "destination="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {

                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                JSONArray legs = object.getJSONArray("legs");

                                JSONObject legsObject = legs.getJSONObject(0);

                                JSONObject distance = legsObject.getJSONObject("distance");
                                String distance_text = distance.getString("text");
                                Double distance_value = Double.parseDouble(distance_text.replaceAll("[^0-9\\\\.]+",""));


                                if(distance_value<=1000 && Common.currentDriver.getCarType().equals("DAGO X") && isKandy)
                                {
                                    Common.distancefare = Common.base_farexk;
                                }
                                else if(distance_value<=1000 && Common.currentDriver.getCarType().equals("DAGO X"))
                                {
                                    Common.distancefare = Common.base_farex;
                                }
                                else if(distance_value>1000 && Common.currentDriver.getCarType().equals("DAGO X") && isKandy)
                                {
                                    Common.distancefare = Common.base_farexk + (distance_value-1000)*Common.distance_ratexk;
                                }
                                else if(distance_value>1000 && Common.currentDriver.getCarType().equals("DAGO X"))
                                {
                                    Common.distancefare = Common.base_farex + (distance_value-1000)*Common.distance_ratex;
                                }
                                else if(distance_value<=2000 && Common.currentDriver.getCarType().equals("DAGO Black"))
                                {
                                    Common.distancefare = Common.base_fareb;
                                }
                                else if(distance_value>2000 && Common.currentDriver.getCarType().equals("DAGO Black"))
                                {
                                    Common.distancefare = Common.base_fareb + (distance_value-2000)*Common.distance_rateb;
                                }



                              *//*  if(distance_value/elapsed_seconds<3)
                                {
                                    if (Common.currentDriver.getCarType().equals("DAGO X")) {
                                        Common.waitingtime = Common.waitingtime + (elapsed_seconds/60) * Common.time_ratex;
                                    } else if (Common.currentDriver.getCarType().equals("DAGO Black")) {
                                        Common.waitingtime = Common.waitingtime + (elapsed_seconds/60) * Common.time_rateb;
                                    }
                                }*//*

                                Common.totfare = Common.distancefare+Common.waitingtime ;

                                txtCurrFare.setText("Rs. "+Common.totfare);

                                HashMap<String,String> map = new HashMap<>();
                                map.put("Rider", cusID);
                                map.put("distancefare",String.valueOf(Common.distancefare));
                                map.put("waitingfare",String.valueOf(Common.waitingtime));
                                map.put("total",String.valueOf(Common.totfare));
                                FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(map)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(DriverTracking.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverTracking.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
*/
    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
        /*Notification notification = new Notification("Driver Arrived!",String.format("The Driver %s has arrived at your location",Common.currentDriver.getName()));
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","Driver Arrived!");
        content.put("driverID",FirebaseAuth.getInstance().getCurrentUser().getUid());
        content.put("message",String.format("The Driver %s has arrived at your location",Common.currentDriver.getName()));
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMServices.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "Failed..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }


    private void sendDropOffNotification(String customerId, String start_address, String end_address, String time, String distance, double fee) {
        Token token = new Token(customerId);
      /*  Notification notification = new Notification("Drop Off",customerToken);
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","Drop Off");
        content.put("message",customerId);
        content.put("start_address",start_address);
        content.put("end_address",end_address);
        content.put("time",time);
        content.put("distance",String.valueOf(Common.distance/1000));
        content.put("total",String.valueOf(fee));
        content.put("cartype",Common.currentDriver.getCarType());
        content.put("driverID",FirebaseAuth.getInstance().getCurrentUser().getUid());
        content.put("location_start",String.format("%f,%f",pickUpLocation.getLatitude(),pickUpLocation.getLongitude()));
        content.put("location_end",String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMServices.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success!=1)
                {
                    Toast.makeText(DriverTracking.this, "Failed..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }




    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        ProgressDialog mDialog = new ProgressDialog(DriverTracking.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Wait..");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String,String>>> routes = null;
            try
            {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();

            ArrayList points=null;
            PolylineOptions polylineOptions = null;

            for(int i=0;i<lists.size();i++)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for(int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat,lng);

                    points.add(position);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }
}

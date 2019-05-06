package com.sccodesoft.dagorider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Helper.DirectionJSONParser;
import com.sccodesoft.dagorider.Remote.IGoogleAPI;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    IGoogleAPI mService;

    Marker mCurrent;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private Polyline direction;
    private Circle riderMarker;
    private Marker driverMarker;

    TextView txtArrived,txtCarType,txtVehicleNo,txtDriverName,txtStars,txtDriverPhone;

    LinearLayout ll1,ll2,ll3;

    String destLat;
    String destLng;

    private LocationRequest mLocationRequest;

    TextView Ride,Waiting,Total;

    CircleImageView driver_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_trip);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initializeFields();
        mService = Common.getGoogleService();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        if(getIntent()!=null)
        {
            if(getIntent().getBooleanExtra("arrived",false)==true)
            {
                txtArrived.setText("You are in Trip..");
                ll1.setVisibility(View.VISIBLE);
                ll2.setVisibility(View.VISIBLE);
                ll3.setVisibility(View.VISIBLE);
                setDriverDetails(getIntent().getStringExtra("driverID"));
                displayFees(getIntent().getStringExtra("driverID"));

                setUpLocation();
            }

            setDriverDetails(getIntent().getStringExtra("driverID"));
            startDriverTracking(getIntent().getStringExtra("driverID"), new MyCallback() {
                @Override
                public void onCallback(String lat, String lng) {
                    Common.pickLng = lng;
                    Common.pickLat = lat;
                }
            });

            Common.driverId = getIntent().getStringExtra("driverID");

        }

    }

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(InTripActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(InTripActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());
    }

    private void buildLocationCallBack() {

        locationCallback =  new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations())
                {
                    Common.mLastLocation = location;
                }
                displayLocation();
            }
        };
    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }


    private void displayLocation()
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

                            if(mCurrent != null)
                                mCurrent.remove();
                            mCurrent = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude,longitude))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                    .title("Your Location"));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));

                        }
                            else
                            {
                                Log.d("ERROR","Can't Get Your Location");
                            }
                        }
                    });

    }

    private void startDriverTracking(String driverId, final MyCallback myCallback) {
        FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String pickLat = dataSnapshot.child("pickuplat").getValue().toString();
                String pickLng = dataSnapshot.child("pickuplng").getValue().toString();
                myCallback.onCallback(pickLat,pickLng);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDriverDetails(final String driverid) {

        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(driverid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        txtDriverName.setText(dataSnapshot.child("name").getValue().toString());
                        txtCarType.setText(dataSnapshot.child("carType").getValue().toString());
                        txtVehicleNo.setText(dataSnapshot.child("vNumber").getValue().toString());
                        txtStars.setText(dataSnapshot.child("rates").getValue().toString());
                        txtDriverPhone.setText(dataSnapshot.child("phone").getValue().toString());

                        Picasso.with(InTripActivity.this)
                                .load(dataSnapshot.child("avatarUrl").getValue().toString())
                                .into(driver_image);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void initializeFields() {
        txtArrived = (TextView)findViewById(R.id.txtArrived);
        txtCarType = (TextView)findViewById(R.id.txtCarType);
        txtVehicleNo = (TextView)findViewById(R.id.txtVehicleNo);
        txtDriverName = (TextView)findViewById(R.id.txtDriverName);
        txtStars = (TextView)findViewById(R.id.txtStars);
        txtDriverPhone = (TextView)findViewById(R.id.txtDriverPhone);

        driver_image = (CircleImageView)findViewById(R.id.driver_image);

        ll1 = (LinearLayout)findViewById(R.id.ll1);
        ll2 = (LinearLayout)findViewById(R.id.ll2);
        ll3 = (LinearLayout)findViewById(R.id.ll3);
        Ride = (TextView)findViewById(R.id.txtRiderC);
        Waiting = (TextView)findViewById(R.id.txtWait);
        Total = (TextView)findViewById(R.id.txtTot);
    }

    private void displayFees(String driverId) {

        FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(driverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Ride.setText(": Rs." + dataSnapshot.child("distancefare").getValue().toString());
                    Waiting.setText(": Rs." + dataSnapshot.child("waitingfare").getValue().toString());
                    Total.setText(": Rs." + dataSnapshot.child("total").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Can't Go Back At This Stage..", Toast.LENGTH_SHORT).show();
    }

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

        if(getIntent()!=null)
        {
            if(getIntent().getBooleanExtra("arrived",false)==true) {
                setUpLocation();
            }
            else {
                startDriverTracking(Common.driverId, new MyCallback() {
                    @Override
                    public void onCallback(String lat, String lng) {
                        Common.pickLat = lat;
                        Common.pickLng = lng;

                        riderMarker = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(Double.parseDouble(Common.pickLat), Double.parseDouble(Common.pickLng)))
                                .radius(100)
                                .strokeColor(Color.BLUE)
                                .fillColor(0x220000FF)
                                .strokeWidth(5.0f));

                        GeoFire gfDrivers = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(txtCarType.getText().toString()));

                        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(Double.valueOf(Common.pickLat), Double.valueOf(Common.pickLng)), 5000);

                        geoQuery.removeAllListeners();
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                if (key.equals(Common.driverId)) {
                                    if (driverMarker != null)
                                        driverMarker.remove();
                                    driverMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude))
                                            .title("Your Driver")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.latitude, location.longitude), 17.0f));

                                    if (direction != null)
                                        direction.remove(); // remove old directions
                                    getDirection(String.valueOf(location.latitude), String.valueOf(location.longitude), Common.pickLat, Common.pickLng);
                                }
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
                    }
                });
            }
        }

    }

    private void getDirection(String dlat,String dlng,String lat,String lng) {

        String requestApi = null;

        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+dlat+","+dlng+"&"+
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
                            Toast.makeText(InTripActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        ProgressDialog mDialog = new ProgressDialog(InTripActivity.this);

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

    public interface MyCallback {
        void onCallback(String lat,String lng);
    }
}

package com.sccodesoft.dago;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sccodesoft.dago.Common.Common;
import com.sccodesoft.dago.Model.Driver;
import com.sccodesoft.dago.Model.Token;
import com.sccodesoft.dago.Remote.IGoogleApi;
import com.sccodesoft.dago.Utill.GpsUtils;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback
{

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    DatabaseReference drivers;
    GeoFire geoFire;

    Marker mCurrent;

    MaterialAnimatedSwitch location_switch;
    TextView txtStatus;
    SupportMapFragment mapFragment;

    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    private String destination;
    private PolylineOptions polylineOptions,blackpolylineOptions;
    private Polyline blackPolyline,grayPolyline;

    private IGoogleApi mService;

    //Presense System
    DatabaseReference onlineRef,currentUserRef;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String avatarUrl=null;

    CircleImageView image_upload;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if(index<polyLineList.size()-1)
            {
                index++;
                next=index+1;
            }
            if(index<polyLineList.size()-1)
            {
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat = v*endPosition.latitude+(1-v)*startPosition.latitude;

                    LatLng newPos = new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);

        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition)
    {
        double lat = Math.abs(startPosition.latitude-endPosition.latitude);
        double lng = Math.abs(startPosition.longitude-endPosition.longitude);

        if(startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if(startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if(startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+270);
        return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                Common.isGPS = isGPSEnable;
            }
        });

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView)navigationHeaderView.findViewById(R.id.txtDriverName);
        TextView txtStars = (TextView)navigationHeaderView.findViewById(R.id.txtStars);
        CircleImageView imageAvatar = (CircleImageView)navigationHeaderView.findViewById(R.id.image_avatar);

        txtName.setText(Common.currentDriver.getName());
        txtStars.setText(Common.currentDriver.getRates());

        if(Common.currentDriver.getAvatarUrl() != null &&
                !TextUtils.isEmpty(Common.currentDriver.getAvatarUrl()))
        {
            Picasso.with(this)
                    .load(Common.currentDriver.getAvatarUrl())
                    .into(imageAvatar);
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        txtStatus = (TextView)findViewById(R.id.txtStatus);
        location_switch = (MaterialAnimatedSwitch)findViewById(R.id.location_switch);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline)
            {
                if(isOnline)
                {
                    FirebaseDatabase.getInstance().goOnline();

                    if(ActivityCompat.checkSelfPermission(DriverHome.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(DriverHome.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        return;
                    }
                    buildLocationCallBack();
                    buildLocationRequest();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());

                    drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(Common.currentDriver.getCarType());
                    geoFire = new GeoFire(drivers);
                    displayLocation();

                    FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).
                            child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("reserved").setValue("0");

                    txtStatus.setText("YOU ARE ONLINE");
                    txtStatus.setBackground(getResources().getDrawable(R.drawable.btn_sign_in_background));
                    Snackbar.make(mapFragment.getView(),"You are Online",Snackbar.LENGTH_SHORT).show();
                }
                else
                {
                    FirebaseDatabase.getInstance().goOffline();

                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

                    if(mCurrent != null)
                        mCurrent.remove();
                    mMap.clear();
                    if(handler!=null)
                        handler.removeCallbacks(drawPathRunnable);

                    txtStatus.setText("YOU ARE OFFLINE");
                    txtStatus.setBackground(getResources().getDrawable(R.drawable.btn_oflline_background));
                    Snackbar.make(mapFragment.getView(),"You are Offline",Snackbar.LENGTH_SHORT).show();
                }

            }
        });

        polyLineList = new ArrayList<>();


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_direction_api));
        }

        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG));

        autocompleteSupportFragment.setCountry("LK");

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(location_switch.isChecked())
                {
                    destination = place.getAddress();
                    destination = destination.replace(" ","+");

                    getDirection();
                }
                else
                {
                    Toast.makeText(DriverHome.this, "Please change your status to ONLINE", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(DriverHome.this, ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });


        setUpLocation();

        mService = Common.getGoogleAPI();

        updateFirebaseToken();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        //Presense System
        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(Common.currentDriver.getCarType())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Remove driver when disconnected
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        FirebaseDatabase.getInstance().goOffline();

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        if(mCurrent != null)
            mCurrent.remove();
        mMap.clear();
        if(handler!=null)
            handler.removeCallbacks(drawPathRunnable);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }


    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        final DatabaseReference tokens = db.getReference(Common.token_tbl);

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        if(FirebaseAuth.getInstance().getCurrentUser() != null)
                            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(token);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DriverHome.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;

        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.d("REQUEST_API",requestApi);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");

                                for(int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }

                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for(LatLng latLng:polyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                grayPolyline= mMap.addPolyline(polylineOptions);

                                blackpolylineOptions = new PolylineOptions();
                                blackpolylineOptions.color(getResources().getColor(R.color.colorAccent));
                                blackpolylineOptions.width(5);
                                blackpolylineOptions.startCap(new SquareCap());
                                blackpolylineOptions.endCap(new SquareCap());
                                blackpolylineOptions.jointType(JointType.ROUND);
                                blackPolyline= mMap.addPolyline(blackpolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size()-1))
                                        .title("PickUp Location"));

                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {
                                        List<LatLng> points = grayPolyline.getPoints();
                                        int precentValue = (int)animation.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int)(size * (precentValue/100.0f));
                                        List<LatLng> p = points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });

                                polyLineAnimator.start();

                                carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                handler = new Handler();
                                index =-1;
                                next=1;
                                handler.postDelayed(drawPathRunnable,3000);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(DriverHome.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                   buildLocationCallBack();
                   buildLocationRequest();
                        if(location_switch.isChecked())
                            displayLocation();

                }
        }
    }

    private void setUpLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            buildLocationRequest();
            buildLocationCallBack();
            if(location_switch.isChecked()) {
                drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(Common.currentDriver.getCarType());
                geoFire = new GeoFire(drivers);
                displayLocation();
            }

        }
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
                            if(location_switch.isChecked())
                            {
                                final double latitude = Common.mLastLocation.getLatitude();
                                final double longitude = Common.mLastLocation.getLongitude();

                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        if(mCurrent != null)
                                            mCurrent.remove();
                                        mCurrent = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude,longitude))
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                                                .title("Your Location"));

                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),15.0f));

                                    }
                                });
                            }
                            else
                            {
                                Log.d("ERROR","Can't Get Your Location");
                            }
                        }
                    }
                });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_update_carType) {
            showDialogUpdateCarType();
        }else if (id == R.id.nav_trip_history) {
            startActivity(new Intent(DriverHome.this, HistoryActivity.class));
        }
        else if (id == R.id.nav_sign_out) {
            signOut();
        }
        else if (id == R.id.nav_update_info) {
            showDialogUpdateInfo();
        }else if (id == R.id.nav_myInvCode) {
            showInviteCode();
        }
        else if(id == R.id.nav_tnc)
        {
            showTermsConditions();
        }
        else if(id == R.id.nav_privacy)
        {
            String url = "https://dagosrilanka.wixsite.com/dago";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
           // showPrivacy();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showPrivacy() {
        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View pr_layout = inflater.inflate(R.layout.layout_privacy,null);

        dialog.setView(pr_layout);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showTermsConditions() {

        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View tnc_layout = inflater.inflate(R.layout.layout_tnc,null);

        dialog.setView(tnc_layout);

        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();

    }

    private void showInviteCode() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("YOUR INVITE CODE");

        LayoutInflater inflater = LayoutInflater.from(this);
        View invCode = inflater.inflate(R.layout.layout_mycode,null);

        TextView myCode = (TextView)invCode.findViewById(R.id.myInvCode);
        Button shareCode = (Button)invCode.findViewById(R.id.btnShareCode);

        shareCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Please use my invite code "+Common.currentDriver.getMyCode()+" to sign up for DAGO Driver App.";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DAGO Driver Invitation");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        myCode.setText(Common.currentDriver.getMyCode());

        alertDialog.setView(invCode);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showDialogUpdateCarType() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("UPDATE VEHICLE TYPE");
        alertDialog.setMessage("Please select your car Type");

        LayoutInflater inflater = LayoutInflater.from(this);
        View carType = inflater.inflate(R.layout.layout_update_cartype,null);

        final RadioButton type_DagoX = (RadioButton)carType.findViewById(R.id.type_dagoX);
        final RadioButton type_DagoBlack = (RadioButton)carType.findViewById(R.id.type_dagoBlack);

        if(Common.currentDriver.getCarType().equals("DAGO X"))
            type_DagoX.setChecked(true);
        else if (Common.currentDriver.getCarType().equals("DAGO Black"))
            type_DagoBlack.setChecked(true);

        alertDialog.setView(carType);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    final android.app.AlertDialog waitingDialog = new SpotsDialog(DriverHome.this);
                    waitingDialog.show();

                    Map<String,Object> updateInfo = new HashMap<>();

                    if(type_DagoX.isChecked())
                        updateInfo.put("carType",type_DagoX.getText().toString());
                    else if (type_DagoBlack.isChecked())
                        updateInfo.put("carType",type_DagoBlack.getText().toString());


                    DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                    driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(updateInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                                                .child(Common.currentDriver.getCarType())
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                                        Toast.makeText(DriverHome.this, "Vehicle Type Updated..", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                        Toast.makeText(DriverHome.this, "Error Occurred while Updating Vehicle Type.. ", Toast.LENGTH_SHORT).show();
                                    waitingDialog.dismiss();
                                }
                            });

                    driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Common.currentDriver = dataSnapshot.getValue(Driver.class);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("UPDATE INFORMATION");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_updateInfo = inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName = layout_updateInfo.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = layout_updateInfo.findViewById(R.id.edtPhone);
        image_upload = layout_updateInfo.findViewById(R.id.image_upload);

        edtName.setText(Common.currentDriver.getName());
        edtPhone.setText(Common.currentDriver.getPhone());
        if(Common.currentDriver.getAvatarUrl() != null &&
                !TextUtils.isEmpty(Common.currentDriver.getAvatarUrl()))
        {
            Picasso.with(this)
                    .load(Common.currentDriver.getAvatarUrl())
                    .into(image_upload);

            avatarUrl = Common.currentDriver.getAvatarUrl();
        }

        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_updateInfo);

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(edtName.getText().toString().isEmpty())
                {
                    Toast.makeText(DriverHome.this, "Please Enter Your Name..", Toast.LENGTH_SHORT).show();
                } else if(edtPhone.getText().toString().isEmpty())
                {
                    Toast.makeText(DriverHome.this, "Please Enter Your Phone Number..", Toast.LENGTH_SHORT).show();
                } else if(avatarUrl==null)
                {
                    Toast.makeText(DriverHome.this, "Please Select a Profile Image..", Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.dismiss();
                    final android.app.AlertDialog waitingDialog = new SpotsDialog(DriverHome.this);
                    waitingDialog.show();

                    String name = edtName.getText().toString();
                    String phone = edtPhone.getText().toString();

                    Map<String,Object> updateInfo = new HashMap<>();
                    updateInfo.put("name",name);
                    updateInfo.put("phone",phone);
                    updateInfo.put("avatarUrl",avatarUrl);

                    DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                    driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(updateInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        Toast.makeText(DriverHome.this, "Driver Information Updated..", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(DriverHome.this, "Error Occurred while Updating Information.. ", Toast.LENGTH_SHORT).show();
                                    waitingDialog.dismiss();
                                }
                            });

                    driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Common.currentDriver = dataSnapshot.getValue(Driver.class);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }

            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Image : "),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Common.GPS_REQUEST) {
                Common.isGPS = true; // flag maintain before get location
            }
        }

        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null)
        {
            Uri saveUri = data.getData();
            if(saveUri != null)
            {
                CropImage.activity(saveUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                Uri uri = result.getUri();
                image_upload.setImageURI(uri);

                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading..");
                mDialog.show();

                String imageName = FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("driverImages/"+imageName+".jpg");
                imageFolder.putFile(uri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    mDialog.dismiss();
                                    Toast.makeText(DriverHome.this, "Image Uploaded..", Toast.LENGTH_SHORT).show();


                                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            avatarUrl = uri.toString();
                                        }
                                    });
                                }
                                else
                                {
                                    Toast.makeText(DriverHome.this, "Error Occured : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Uploading "+progress+"%");
                            }
                        });
            }
        }

    }


    private void signOut() {

        AlertDialog.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(this);

        builder.setMessage("Do You Want To Sign Out?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(DriverHome.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if(ActivityCompat.checkSelfPermission(DriverHome.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(DriverHome.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        buildLocationCallBack();
        buildLocationRequest();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());
    }

}

package com.sccodesoft.dagorider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arsy.maps_library.MapRipple;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Helper.CustomInfoWindow;
import com.sccodesoft.dagorider.Model.Rider;
import com.sccodesoft.dagorider.Model.Token;
import com.sccodesoft.dagorider.Remote.IFCMServices;
import com.sccodesoft.dagorider.Remote.IGoogleAPI;
import com.sccodesoft.dagorider.Utill.GpsUtils;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener, ValueEventListener {

    SupportMapFragment mapFragment;
    private AutocompleteSupportFragment place_location, place_destination;
    String mPlaceLocation,mPlaceDestination;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private PolylineOptions polylineOptions;
    private Polyline direction;

    DatabaseReference ref;
    GeoFire geoFire;


    Marker mUserMarker, markerDestination;

    //BottomSheet
    ImageView imgExpandable;
    BottomSheetDialogFragment mBottomSheet;
    Button btnPickupRequest;

    int radius=1;
    int distance=1;
    private static final int LIMIT=3;

    //Send Alert
    IFCMServices mService;
    IGoogleAPI mGoogleService;

    //Presense System
    DatabaseReference driversAvailabale;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String avatarUrl=null;

    CircleImageView image_upload;

    ImageView carDagoX,carDagoBlack;
    boolean isDagoX=true;
    boolean isKandy=false;

    private List<LatLng> polyLineList;

    //Map Animation
    MapRipple mapRipple;

    private BroadcastReceiver mCancelBroadCast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            btnPickupRequest.setText("SEARCH DRIVERS");

            Common.driverId = "";
            Common.isDriverFound=false;

            btnPickupRequest.setEnabled(true);

            mUserMarker.hideInfoWindow();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                Common.isGPS = isGPSEnable;
            }
        });

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast,new IntentFilter(Common.CANCEL_BROADCAST_STRING));

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCancelBroadCast,new IntentFilter(Common.DROPOFF_BROADCAST_STRING));

        mService = Common.getFCMService();
        mGoogleService = Common.getGoogleService();

        polyLineList = new ArrayList<>();

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

        carDagoX = (ImageView)findViewById(R.id.select_dagoX);
        carDagoBlack = (ImageView)findViewById(R.id.select_dagoBlack);

        carDagoX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDagoX=true;
                if(isDagoX)
                {
                    carDagoX.setImageResource(R.drawable.dagocar_cui_select);
                    carDagoBlack.setImageResource(R.drawable.dagocar_vip);
                }
                else
                {
                    carDagoX.setImageResource(R.drawable.dagocar_cui);
                    carDagoBlack.setImageResource(R.drawable.dagocar_vip_select);
                }
                if(driversAvailabale != null)
                    driversAvailabale.removeEventListener(Home.this);
                driversAvailabale = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isDagoX?"DAGO X":"DAGO Black");
                driversAvailabale.addValueEventListener(Home.this);
                loadAllAvailabaleDriver(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
            }
        });

        carDagoBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDagoX=false;
                if(isDagoX)
                {
                    carDagoX.setImageResource(R.drawable.dagocar_cui_select);
                    carDagoBlack.setImageResource(R.drawable.dagocar_vip);
                }
                else
                {
                    carDagoX.setImageResource(R.drawable.dagocar_cui);
                    carDagoBlack.setImageResource(R.drawable.dagocar_vip_select);
                }
                if(driversAvailabale != null)
                    driversAvailabale.removeEventListener(Home.this);
                driversAvailabale = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isDagoX?"DAGO X":"DAGO Black");
                driversAvailabale.addValueEventListener(Home.this);
                loadAllAvailabaleDriver(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
            }
        });

        txtName.setText(Common.currentUser.getName());
        txtStars.setText(Common.currentUser.getRates());

        if(Common.currentUser.getAvatarUrl() != null &&
                !TextUtils.isEmpty(Common.currentUser.getAvatarUrl()))
        {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageAvatar);
        }

        //Maps
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        btnPickupRequest = (Button)findViewById(R.id.btnPickupRequest);

        if(getIntent().getExtras()  != null) {
            if (getIntent().getStringExtra("rated").equals("rated")) {
                btnPickupRequest.setText("SEARCH DRIVERS");

                Common.driverId = "";
                Common.isDriverFound = false;

                btnPickupRequest.setEnabled(true);
            }
        }
        else
        {
            FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                            {
                                if(postSnapShot.child("Rider").getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                                {
                                    Intent intent = new Intent(Home.this,InTripActivity.class);
                                    intent.putExtra("arrived",true);
                                    intent.putExtra("driverID",postSnapShot.getKey());
                                    startActivity(intent);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Common.isDriverFound) {
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }
                else if(Common.mDestination!=null) {
                    btnPickupRequest.setEnabled(false);
                    Common.sendRequestToDriver(Common.driverId, mService, Home.this, Common.mLastLocation, Common.mDestination,isKandy);
                }else
                {
                        Toast.makeText(Home.this, "Please Select Destination..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Autocompletetextview
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_direction_api));
        }

        place_location = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_location);
        place_location.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG));
        place_destination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_destination);
        place_destination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.ADDRESS,Place.Field.LAT_LNG));

        place_location.setCountry("LK");
        place_destination.setCountry("LK");

        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                isKandy=false;

                mPlaceLocation = place.getName()+", "+place.getAddress();

                Location temp = new Location(LocationManager.GPS_PROVIDER);
                temp.setLatitude(place.getLatLng().latitude);
                temp.setLongitude(place.getLatLng().longitude);

                checkKandy(temp);

                Common.mLastLocation = temp;

                mMap.clear();

                mUserMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            .title("Pickup Here"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mPlaceDestination = place.getName().toString()+", "+place.getAddress().toString();

                Common.mDestination = place.getLatLng();

                if(markerDestination != null)
                    markerDestination.remove();

                markerDestination = mMap.addMarker(new MarkerOptions()
                                    .position(place.getLatLng())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                    .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

                if(direction != null)
                    direction.remove(); // remove old directions
                getDirection(String.valueOf(place.getLatLng().latitude),String.valueOf(place.getLatLng().longitude),String.valueOf(Common.mLastLocation.getLatitude()),String.valueOf(Common.mLastLocation.getLongitude()));


                //Show bottom info
                BottomSheetDialogFragment mBottomSheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),mPlaceDestination,false,isDagoX,isKandy);
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });


        setUpLocation();

        updateFirebaseToken();

    }

    private void checkKandy(Location temp) {
        Location d = new Location("Kandy Center");
        d.setLatitude(7.289616);
        d.setLongitude(80.632326);

       if (temp.distanceTo(d) <= 4000)
       {
           isKandy=true;
           Log.i("ISKANDYd",String.valueOf(isKandy));
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
                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    private void requestPickupHere(String uid)
    {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid, new GeoLocation(Common.mLastLocation.getLatitude(), Common.mLastLocation.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

        if(mUserMarker.isVisible())
            mUserMarker.remove();

        //new marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));

        mUserMarker.showInfoWindow();

        mapRipple = new MapRipple(mMap,new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),this);
        mapRipple.withNumberOfRipples(1);
        mapRipple.withDistance(500);
        mapRipple.withRippleDuration(1000);
        mapRipple.withTransparency(0.5f);

        mapRipple.startRippleMapAnimation();

        btnPickupRequest.setText("Getting Your Driver...");

        findDriver();
    }

    private void findDriver() {
        DatabaseReference driverLocation;
        if(isDagoX)
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("DAGO X");
        else
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("DAGO Black");

        GeoFire gfDrivers = new GeoFire(driverLocation);

        final GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()),radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                //If Found
                if(!Common.isDriverFound)
                {
                    Common.isDriverFound=true;
                    Common.driverId=key;
                    btnPickupRequest.setText("CALL DRIVER");
                   // Toast.makeText(Home.this, key, Toast.LENGTH_SHORT).show();
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
                //Increase radius if no driver found
                if(!Common.isDriverFound && radius < LIMIT)
                {
                    radius++;
                    findDriver();
                }
                else
                {
                    if(!Common.isDriverFound) {
                        btnPickupRequest.setText("SEARCH DRIVERS");

                        Common.driverId = "";

                        btnPickupRequest.setEnabled(true);

                        if(mapRipple.isAnimationRunning())
                            mapRipple.stopRippleMapAnimation();

                        mUserMarker.hideInfoWindow();

                        geoQuery.removeAllListeners();

                        Toast.makeText(Home.this, "No any available driver near your location..", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void setUpLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.CALL_PHONE
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
                buildLocationCallBack();
                createLocationRequest();
                displayLocation();
        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Common.mLastLocation = locationResult.getLastLocation();
                Common.mLastLocation = locationResult.getLocations().get(locationResult.getLocations().size()-1);
                displayLocation();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                        buildLocationCallBack();
                        createLocationRequest();
                        displayLocation();
                }
                break;
        }
    }

    private void createLocationRequest()
    {
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

       fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
           @Override
           public void onSuccess(Location location) {
               Common.mLastLocation = location;
               if(Common.mLastLocation != null)
               {
                   //Presense System
                   driversAvailabale = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(isDagoX?"DAGO X":"DAGO Black");
                   driversAvailabale.addValueEventListener(Home.this);

                   loadAllAvailabaleDriver(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));

               }
               else
               {
                   Log.d("ERROR","Can't Get Your Location");
               }
           }
       });

    }


    public void setLocationAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(Home.this, Locale.getDefault());
        isKandy=false;
        Location loc = new Location("tem");
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        checkKandy(loc);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);

            place_location.setText(add);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllAvailabaleDriver(final LatLng location) {

       mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title("Pickup Here"));


        setLocationAddress(location.latitude,location.longitude);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15.0f));


        //Load all available drivers in distance 3km
        DatabaseReference driverLocation;
        if(isDagoX)
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("DAGO X");
        else
            driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child("DAGO Black");

        GeoFire gf = new GeoFire(driverLocation);

        GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                        .child(key)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                //Rider and Driver has same properties so using rider model
                                Rider rider = dataSnapshot.getValue(Rider.class);

                                if(!rider.getReserved().equals("1")) {

                                    if (isDagoX) {
                                        if (rider.getCarType().equals("DAGO X")) {
                                            //On Map
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(location.latitude, location.longitude))
                                                    .flat(true)
                                                    .title(rider.getName())
                                                    .snippet("Driver ID : " + dataSnapshot.getKey())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                        }
                                    } else {
                                        if (rider.getCarType().equals("DAGO Black")) {
                                            //On Map
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(new LatLng(location.latitude, location.longitude))
                                                    .flat(true)
                                                    .title(rider.getName())
                                                    .snippet("Driver ID : " + dataSnapshot.getKey())
                                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
                                        }
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(distance<LIMIT)
                {
                    distance++;
                    loadAllAvailabaleDriver(location);
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            signOut();
        }
        else if (id == R.id.nav_trip_history) {
            startActivity(new Intent(Home.this,HistoryActivity.class));
        }
        else if (id == R.id.nav_update_info) {
            showDialogUpdateInfo();
        }
        else if(id == R.id.nav_tnc)
        {
            showTermsConditions();
        }
        else if(id == R.id.nav_myInvCode)
        {
            showInviteCode();
        }
        else if(id == R.id.nav_privacy)
        {
            String url = "https://dagosrilanka.wixsite.com/dago";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            //showPrivacy();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                String shareBody = "Please use my invite code "+Common.currentUser.getMyCode()+" to sign up for DAGO Driver App.";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "DAGO Driver Invitation");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        myCode.setText(Common.currentUser.getMyCode());

        alertDialog.setView(invCode);

        alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
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

    private void showDialogUpdateInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("UPDATE INFORMATION");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_updateInfo = inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName = layout_updateInfo.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = layout_updateInfo.findViewById(R.id.edtPhone);
        image_upload = layout_updateInfo.findViewById(R.id.image_upload);

        edtName.setText(Common.currentUser.getName());
        edtPhone.setText(Common.currentUser.getPhone());
        if(Common.currentUser.getAvatarUrl() != null &&
                !TextUtils.isEmpty(Common.currentUser.getAvatarUrl()))
        {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(image_upload);

            avatarUrl = Common.currentUser.getAvatarUrl();
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
                    Toast.makeText(Home.this, "Please Enter Your Name..", Toast.LENGTH_SHORT).show();
                } else if(edtPhone.getText().toString().isEmpty())
                {
                    Toast.makeText(Home.this, "Please Enter Your Phone Number..", Toast.LENGTH_SHORT).show();
                } else if(avatarUrl==null)
                {
                    Toast.makeText(Home.this, "Please Select a Profile Image..", Toast.LENGTH_SHORT).show();
                }
                else {
                    dialog.dismiss();
                    final android.app.AlertDialog waitingDialog = new SpotsDialog(Home.this);
                    waitingDialog.show();

                    String name = edtName.getText().toString();
                    String phone = edtPhone.getText().toString();

                    Map<String,Object> updateInfo = new HashMap<>();
                    updateInfo.put("name",name);
                    updateInfo.put("phone",phone);
                    updateInfo.put("avatarUrl",avatarUrl);

                    DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);
                    driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .updateChildren(updateInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                        Toast.makeText(Home.this, "User Information Updated..", Toast.LENGTH_SHORT).show();
                                    else
                                        Toast.makeText(Home.this, "Error Occurred while Updating Information.. ", Toast.LENGTH_SHORT).show();
                                    waitingDialog.dismiss();
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
                final StorageReference imageFolder = storageReference.child("riderImages/"+imageName+".jpg");
                imageFolder.putFile(uri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    mDialog.dismiss();
                                    Toast.makeText(Home.this, "Image Uploaded..", Toast.LENGTH_SHORT).show();


                                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            avatarUrl = uri.toString();
                                        }
                                    });
                                }
                                else
                                {
                                    Toast.makeText(Home.this, "Error Occured : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Uploading..");
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
                        Intent intent = new Intent(Home.this,MainActivity.class);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(markerDestination != null)
                    markerDestination.remove();
                markerDestination = mMap.addMarker(new MarkerOptions()
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                            .position(latLng)
                                            .title("Destination"));
                Common.mDestination=markerDestination.getPosition();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));

                if(direction != null)
                    direction.remove(); // remove old directions
                getDirection(String.valueOf(latLng.latitude),String.valueOf(latLng.longitude),String.valueOf(Common.mLastLocation.getLatitude()),String.valueOf(Common.mLastLocation.getLongitude()));


                BottomSheetDialogFragment mBottomSheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f",Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude())
                                                            ,String.format("%f,%f",latLng.latitude,latLng.longitude),true,isDagoX,isKandy);
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }
        });

        mMap.setOnInfoWindowClickListener(this);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());

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

            mGoogleService.getPath(requestApi)
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

                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.RED);
                                    polylineOptions.addAll(polyLineList);
                                    direction = mMap.addPolyline(polylineOptions);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(Home.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
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

    /*private class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>>
    {
        ProgressDialog mDialog = new ProgressDialog(Home.this);

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
    }*/

    @Override
    public void onInfoWindowClick(Marker marker) {

        if(marker.getTitle().equals("Pickup Here"))
        {
            Toast.makeText(this, "This Will Be Your Pick Up Location..", Toast.LENGTH_SHORT).show();
        }
        else if(marker.getTitle().equals("You"))
        {
            Toast.makeText(this, "Your Current Location..", Toast.LENGTH_SHORT).show();
        }
        else
            {
            Intent intent = new Intent(Home.this,CallDriver.class);
            String driveridc =marker.getSnippet().substring(12);
            intent.putExtra("driverID",driveridc);
            intent.putExtra("isKandy",isKandy);
            intent.putExtra("lat",Common.mLastLocation.getLatitude());
            intent.putExtra("lng",Common.mLastLocation.getLongitude());

            startActivity(intent);
        }

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        loadAllAvailabaleDriver(new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude()));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCancelBroadCast);
        super.onDestroy();
    }
}

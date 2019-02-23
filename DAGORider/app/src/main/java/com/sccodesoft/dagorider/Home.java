package com.sccodesoft.dagorider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Helper.CustomInfoWindow;
import com.sccodesoft.dagorider.Model.FCMResponse;
import com.sccodesoft.dagorider.Model.Notification;
import com.sccodesoft.dagorider.Model.Rider;
import com.sccodesoft.dagorider.Model.Sender;
import com.sccodesoft.dagorider.Model.Token;
import com.sccodesoft.dagorider.Remote.IFCMServices;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    SupportMapFragment mapFragment;
    private AutocompleteSupportFragment place_location, place_destination;
    String mPlaceLocation,mPlaceDestination;


    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

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

    //Presense System
    DatabaseReference driversAvailabale;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String avatarUrl=null;

    CircleImageView image_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mService = Common.getFCMService();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

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

        //Init View
        imgExpandable = (ImageView)findViewById(R.id.imgExpandable);


        btnPickupRequest = (Button)findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Common.isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(Common.driverId);
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


        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {

                mPlaceLocation = place.getName().toString()+", "+place.getAddress().toString();

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

                mUserMarker = mMap.addMarker(new MarkerOptions()
                                    .position(place.getLatLng())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker))
                                    .title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

                //Show bottom info
                BottomSheetDialogFragment mBottomSheet = BottomSheetRiderFragment.newInstance(mPlaceLocation,mPlaceDestination,false);
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        setUpLocation();

        updateFirebaseToken();
    }

    private void updateFirebaseToken() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void sendRequestToDriver(String driverId) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            //Get token obj from db with key
                            Token token = postSnapShot.getValue(Token.class);

                            //Make raw payload convert LatLng to json
                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken,json_lat_lng);
                            Sender content = new Sender(token.getToken(),data);

                            mService.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().success==1)
                                                Toast.makeText(Home.this, "Request Sent!", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(Home.this, "Failed !", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void requestPickupHere(String uid)
    {
        DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl);
        GeoFire mGeoFire = new GeoFire(dbRequest);
        mGeoFire.setLocation(uid,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        if(mUserMarker.isVisible())
            mUserMarker.remove();

        //new marker
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .title("Pickup Here")
                .snippet("")
                .position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        mUserMarker.showInfoWindow();

        btnPickupRequest.setText("Getting Your Driver...");

        findDriver();
    }

    private void findDriver() {
        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire gfDrivers = new GeoFire(drivers);

        final GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
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
                        Toast.makeText(Home.this, "No any available driver near your location..", Toast.LENGTH_SHORT).show();
                        btnPickupRequest.setText("REQUEST PICKUP");
                        geoQuery.removeAllListeners();
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
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            if(checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
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

    private void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS)
        {
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This Device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void displayLocation()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null)
        {
            //Presense System
            driversAvailabale = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
            driversAvailabale.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    loadAllAvailabaleDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

                final double latitude = mLastLocation.getLatitude();
                final double longitude = mLastLocation.getLongitude();

                loadAllAvailabaleDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

            }
            else
            {
                Log.d("ERROR","Can't Get Your Location");
            }

    }

    private void loadAllAvailabaleDriver(final LatLng location) {

       mMap.clear();
        mUserMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(location)
                .title("You"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15.0f));


        //Load all available drivers in distance 3km
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
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
                                //Rider and User has same properties so using rider model
                                Rider rider = dataSnapshot.getValue(Rider.class);

                                //On Map
                                mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(location.latitude,location.longitude))
                                                .flat(true)
                                                .title(rider.getName())
                                                .snippet("Phone : "+rider.getPhone())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
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
        } else {
            super.onBackPressed();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sign_out) {
            signOut();
        } else if (id == R.id.nav_update_info) {
            showDialogUpdateInfo();
        } else if (id == R.id.nav_change_password) {
            showDialogChangePwd();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        edtPhone.setText(Common.currentUser.getEmail());
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
                                mDialog.setMessage("Uploading "+progress+"%");
                            }
                        });
            }
        }

    }


    private void signOut() {

        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Home.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showDialogChangePwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please enter details");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_pwd = inflater.inflate(R.layout.layout_change_password,null);

        final MaterialEditText edtPassword = layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtConfPassword = layout_pwd.findViewById(R.id.edtConfNewPassword);

        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                if(TextUtils.isEmpty(edtPassword.getText().toString()))
                {
                    Toast.makeText(Home.this, "Please Enter Current Password..", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(edtNewPassword.getText().toString()))
                {
                    Toast.makeText(Home.this, "Please Enter New Password..", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edtConfPassword.getText().toString().length()<6)
                {
                    Toast.makeText(Home.this, "Please Confirm New Password..", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(edtNewPassword.getText().toString().equals(edtConfPassword.getText().toString())) {

                    final SpotsDialog waitingDialog = new SpotsDialog(Home.this);
                    waitingDialog.show();

                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());

                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtNewPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Map<String, Object> password = new HashMap<>();
                                                            password.put("password", edtNewPassword.getText().toString());

                                                            DatabaseReference riderInfomation = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);

                                                            riderInfomation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                waitingDialog.dismiss();
                                                                                Toast.makeText(Home.this, "Your Password Changed Successfully..", Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                waitingDialog.dismiss();
                                                                                Toast.makeText(Home.this, "Password Changed But Couldn't Update Driver Information..", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        } else {
                                                            waitingDialog.dismiss();
                                                            Toast.makeText(Home.this, "Error Occured While Changing Password..", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Home.this, "Current Password Not Correct", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                }
            }


        });

        alertDialog.setNegativeButton("CANCEL",   new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();


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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15.0f));

                BottomSheetDialogFragment mBottomSheet = BottomSheetRiderFragment.newInstance(String.format("%f,%f",mLastLocation.getLatitude(),mLastLocation.getLongitude())
                                                            ,String.format("%f,%f",latLng.latitude,latLng.longitude),true);
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }
}

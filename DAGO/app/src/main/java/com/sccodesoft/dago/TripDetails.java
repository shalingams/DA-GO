package com.sccodesoft.dago;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sccodesoft.dago.Common.Common;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class TripDetails extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private TextView txtDate, txtFee, txtBaseFare, txtTime, txtDistance, txtEstimatedPayout, txtFrom, txtTo;

    private Button doneTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //InitView
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtFee = (TextView)findViewById(R.id.txtFee);
        txtBaseFare = (TextView)findViewById(R.id.txtBaseFare);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtEstimatedPayout = (TextView)findViewById(R.id.txtEstimatedPayout);
        txtFrom = (TextView)findViewById(R.id.txtFrom);
        txtTo = (TextView)findViewById(R.id.txtTo);

        doneTrip = (Button)findViewById(R.id.btnDone);

        FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        txtFee.setText("Rs."+dataSnapshot.child("total").getValue().toString());
                        txtEstimatedPayout.setText("Rs."+dataSnapshot.child("total").getValue().toString());
                        if(Common.currentDriver.getCarType().equals("DAGO X"))
                            txtTime.setText(String.valueOf(Double.valueOf(dataSnapshot.child("waitingfare").getValue().toString())/Common.time_ratex)+" mins");
                        else if(Common.currentDriver.getCarType().equals("DAGO Black"))
                            txtTime.setText(String.valueOf(Double.valueOf(dataSnapshot.child("waitingfare").getValue().toString())/Common.time_rateb)+" mins");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        doneTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                HashMap driverHis = new HashMap();
                driverHis.put("date", txtDate.getText());
                driverHis.put("from", txtFrom.getText());
                driverHis.put("to", txtTo.getText());
                driverHis.put("TotFare", txtFee.getText());
                driverHis.put("duration", txtTime.getText());
                driverHis.put("distance", txtDistance.getText());

                FirebaseDatabase.getInstance().getReference().child("DriverTripHistory")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(Calendar.getInstance().getTime().toString())
                        .setValue(driverHis)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(TripDetails.this,DriverHome.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TripDetails.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        settingInformation();
    }

    private void settingInformation() {
        if(getIntent() != null)
        {
            Calendar calendar = Calendar.getInstance();
            String date = String.format("%s, %d/%d",convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH));
            txtDate.setText(date);

           // txtFee.setText(String.format("Rs %.2f",getIntent().getDoubleExtra("total",0.0)));
           // txtEstimatedPayout.setText(String.format("Rs %.2f",getIntent().getDoubleExtra("total",0.0)));
            if(Common.currentDriver.getCarType().equals("DAGO X"))
                txtBaseFare.setText(String.format("Rs %.2f",Common.base_farex));
            else if(Common.currentDriver.getCarType().equals("DAGO Black"))
                txtBaseFare.setText(String.format("Rs %.2f",Common.base_farex));
           // txtTime.setText(String.format("%s min",getIntent().getStringExtra("time")));
            txtDistance.setText(String.format("%s km",getIntent().getStringExtra("distance")));
            txtFrom.setText(getIntent().getStringExtra("start_address"));
            txtTo.setText(getIntent().getStringExtra("end_address"));

            String[] location_end = getIntent().getStringExtra("location_end").split(",");
            LatLng dropOff = new LatLng(Double.parseDouble(location_end[0]),Double.parseDouble(location_end[1]));

            mMap.addMarker(new MarkerOptions().position(dropOff)
                .title("Drop Off Here")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dropOff,12.0f));
        }
    }

    private String convertToDayOfWeek(int day) {
        switch (day)
        {
            case Calendar.SUNDAY:
                return "SUNDAY";
            case Calendar.MONDAY:
                return "MONDAY";
            case Calendar.TUESDAY:
                return "TUESDAY";
            case Calendar.WEDNESDAY:
                return "WEDNESDAY";
            case Calendar.THURSDAY:
                return "THURSDAY";
            case Calendar.FRIDAY:
                return "FRIDAY";
            case Calendar.SATURDAY:
                return "SATURDAY";
                default:
                    return "UNK";
        }
    }
}

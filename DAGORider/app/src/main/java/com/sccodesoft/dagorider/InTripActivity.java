package com.sccodesoft.dagorider;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sccodesoft.dagorider.Common.Common;

public class InTripActivity extends AppCompatActivity {

    TextView txtArrived;

    LinearLayout ll1,ll2,ll3;

    TextView Ride,Waiting,Total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_trip);

        txtArrived = (TextView)findViewById(R.id.txtArrived);
        ll1 = (LinearLayout)findViewById(R.id.ll1);
        ll2 = (LinearLayout)findViewById(R.id.ll2);
        ll3 = (LinearLayout)findViewById(R.id.ll3);
        Ride = (TextView)findViewById(R.id.txtRiderC);
        Waiting = (TextView)findViewById(R.id.txtWait);
        Total = (TextView)findViewById(R.id.txtTot);

        if(getIntent()!=null && getIntent().getBooleanExtra("arrived",false)==true)
        {
            txtArrived.setText("You are in Trip..");
            ll1.setVisibility(View.VISIBLE);
            ll2.setVisibility(View.VISIBLE);
            ll3.setVisibility(View.VISIBLE);
            displayFees(getIntent().getStringExtra("driverId"));
        }

    }

    private void displayFees(String driverId) {

        FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(driverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Ride.setText(": Rs."+dataSnapshot.child("distancefare").getValue().toString());
                Waiting.setText(": Rs."+dataSnapshot.child("waitingfare").getValue().toString());
                Total.setText(": Rs."+dataSnapshot.child("total").getValue().toString());
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
}

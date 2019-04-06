package com.sccodesoft.dagorider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Model.Rider;
import com.sccodesoft.dagorider.Remote.IFCMServices;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallDriver extends AppCompatActivity {

    CircleImageView avatar_image;
    TextView txt_name, txt_phone, txt_rate;
    Button btnCallDriver, btnCallDriverPhone;

    String driverId;
    Location mLastLocation;

    IFCMServices mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_driver);

        if(getIntent()!=null)
        {
            driverId = getIntent().getStringExtra("driverId");
            double lat = getIntent().getDoubleExtra("lat",-1.0);
            double lng = getIntent().getDoubleExtra("lng",-1.0);

            mLastLocation = new Location("");
            mLastLocation.setLatitude(lat);
            mLastLocation.setLongitude(lng);

            loadDriverInfo(driverId);
        }

        mService = Common.getFCMService();
        avatar_image = (CircleImageView) findViewById(R.id.avatar_image);
        txt_name = (TextView) findViewById(R.id.txt_name);
        txt_phone = (TextView) findViewById(R.id.txt_phone);
        txt_rate = (TextView) findViewById(R.id.txt_rate);
        btnCallDriver = (Button) findViewById(R.id.btnCallDriver);
        btnCallDriverPhone = (Button) findViewById(R.id.btnCallDriverPhone);

        btnCallDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverId != null && !driverId.isEmpty())
                    Common.sendRequestToDriver(driverId, mService, getBaseContext(), mLastLocation,Common.mDestination);
            }
        });

        btnCallDriverPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + txt_phone.getText().toString()));
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                startActivity(intent);
            }
        });

    }

    private void loadDriverInfo(final String driverId) {
        FirebaseDatabase.getInstance()
                .getReference(Common.user_driver_tbl)
                .child(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Rider driverUser = dataSnapshot.getValue(Rider.class);

                        if(!driverUser.getAvatarUrl().isEmpty())
                        {
                            Picasso.with(getBaseContext())
                                    .load(driverUser.getAvatarUrl())
                                    .into(avatar_image);
                        }
                        txt_name.setText(driverUser.getName());
                        txt_phone.setText(driverUser.getPhone());
                        txt_rate.setText(driverUser.getRates());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}

package com.sccodesoft.dagorider;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Model.Rate;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RateActivity extends AppCompatActivity {

    Button btnSubmit,btnFeeDetails;
    MaterialRatingBar ratingBar;
    MaterialEditText edtComment;
    TextView txtFees,txtaTime,txtaDistance;
    LatLng dropOff;

    String date,fee,distance,baseFare,time,from,to,cartype;

    FirebaseDatabase database;
    DatabaseReference rateDetailRef;
    DatabaseReference driverInformationRef;

    double ratingStars = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        database = FirebaseDatabase.getInstance();
        rateDetailRef = database.getReference(Common.rate_detail_tbl);
        driverInformationRef = database.getReference(Common.user_driver_tbl);

        btnSubmit = (Button)findViewById(R.id.btnSubmit);
        ratingBar = (MaterialRatingBar)findViewById(R.id.ratingBar);
        edtComment = (MaterialEditText)findViewById(R.id.edtComment);
        txtaDistance = (TextView)findViewById(R.id.txtaDistance);
        txtaTime = (TextView)findViewById(R.id.txtaTime);
        btnFeeDetails =(Button)findViewById(R.id.btnFeeDetails);
        txtFees = (TextView)findViewById(R.id.txtFees);


        if(getIntent() != null)
        {
            Common.driverId = getIntent().getStringExtra("driverId");
            Calendar calendar = Calendar.getInstance();
            date = String.format("%s, %d/%d",convertToDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)),calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.MONTH));

            cartype = getIntent().getStringExtra("cartype");

            if(cartype.equals("DAGO X"))
                baseFare = String.format("Rs %.2f",Common.base_farex);
            else if(cartype.equals("DAGO Black"))
                baseFare = String.format("Rs %.2f",Common.base_fareb);

            distance = String.format("%s km",getIntent().getStringExtra("distance"));
            from = getIntent().getStringExtra("start_address");
            to = getIntent().getStringExtra("end_address");
        }

        FirebaseDatabase.getInstance().getReference(Common.ongoing_tbl).child(Common.driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        txtFees.setText("Rs."+dataSnapshot.child("total").getValue().toString());
                        if(cartype.equals("DAGO X"))
                            txtaTime.setText(String.valueOf(Double.valueOf(dataSnapshot.child("waitingfare").getValue().toString())/Common.time_ratex)+" mins");
                        else if(cartype.equals("DAGO Black"))
                            txtaTime.setText(String.valueOf(Double.valueOf(dataSnapshot.child("waitingfare").getValue().toString())/Common.time_rateb)+" mins");
                        time = txtaTime.getText().toString();
                        fee = txtFees.getText().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



        txtaDistance.setText(distance);

        btnFeeDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeeDetails();
            }
        });

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars = rating;
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRateDetails(Common.driverId);
            }
        });
    }

    private void showFeeDetails() {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(this);
        alertDialog.setTitle("DETAILED REPORT");

        LayoutInflater inflater = LayoutInflater.from(this);
        View details = inflater.inflate(R.layout.layout_reciept,null);

        TextView txtDate = (TextView)details.findViewById(R.id.txtDate);
        TextView txtFee = (TextView)details.findViewById(R.id.txtFee);
        TextView txtBaseFare = (TextView)details.findViewById(R.id.txtBaseFare);
        TextView txtTime = (TextView)details.findViewById(R.id.txtTime);
        TextView txtDistance = (TextView)details.findViewById(R.id.txtDistance);
        TextView txtEstimatedPayout = (TextView)details.findViewById(R.id.txtEstimatedPayout);
        TextView txtFrom = (TextView)details.findViewById(R.id.txtFrom);
        TextView txtTo = (TextView)details.findViewById(R.id.txtTo);

        txtDate.setText(date);

        txtFee.setText(fee);
        txtEstimatedPayout.setText(fee);
        txtBaseFare.setText(baseFare);
        txtTime.setText(time);
        txtDistance.setText(distance);
        txtFrom.setText(from);
        txtTo.setText(to);

        alertDialog.setView(details);

        alertDialog.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

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

    private void submitRateDetails(final String driverId) {
        final AlertDialog alertDialog = new SpotsDialog(this);
        alertDialog.show();

        Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComments(edtComment.getText().toString());
        rate.setRaterid(FirebaseAuth.getInstance().getCurrentUser().getUid());

        rateDetailRef.child(driverId)
                .push()
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        rateDetailRef.child(driverId)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        double averageStars = 0.0;
                                        int count =0;
                                        for(DataSnapshot postSnapShot : dataSnapshot.getChildren())
                                        {
                                            Rate rate = postSnapShot.getValue(Rate.class);
                                            averageStars+=Double.parseDouble(rate.getRates());
                                            count++;
                                        }
                                        double finalAverage = averageStars/count;
                                        DecimalFormat df = new DecimalFormat("#.#");
                                        String valueUpdate = df.format(finalAverage);

                                        Map<String,Object> driverUpdateRate = new HashMap<>();
                                        driverUpdateRate.put("rates",valueUpdate);

                                        driverInformationRef.child(driverId)
                                                .updateChildren(driverUpdateRate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        alertDialog.dismiss();

                                                        HashMap riderHis = new HashMap();
                                                        riderHis.put("date", date);
                                                        riderHis.put("from", from);
                                                        riderHis.put("to", to);
                                                        riderHis.put("fare", fee);
                                                        riderHis.put("duration", time);
                                                        riderHis.put("distance", distance);

                                                        FirebaseDatabase.getInstance().getReference().child("RiderTripHistory")
                                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                .child(Calendar.getInstance().getTime().toString())
                                                                .setValue(riderHis)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(RateActivity.this, "Thank You For Rating..", Toast.LENGTH_SHORT).show();
                                                                        Intent intent = new Intent(RateActivity.this,Home.class);
                                                                        intent.putExtra("rated","rated");
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(RateActivity.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        alertDialog.dismiss();
                                                        Toast.makeText(RateActivity.this, "Couldn't add Your Ratings To the Driver..", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.dismiss();
                        Toast.makeText(RateActivity.this, "Rate Failed.. !", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.sccodesoft.dago;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.sccodesoft.dago.Common.Common;
import com.sccodesoft.dago.Model.DataMessage;
import com.sccodesoft.dago.Model.FCMResponse;
import com.sccodesoft.dago.Model.Token;
import com.sccodesoft.dago.Remote.IFCMServices;
import com.sccodesoft.dago.Remote.IGoogleApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance,txtCountDown;
    Button btnCancel,btnAccpet;

    MediaPlayer mediaPlayer;

    IGoogleApi mService;
    IFCMServices mFCMService;

    String customerId,cusID;

    String lat;
    String lng;

    String destlng,destlat;

    int Accpet;

    boolean isKandy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        Accpet = 0;

        //Init View
        txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtCountDown = (TextView)findViewById(R.id.txt_count_down);

        btnAccpet = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
            }
        });

        btnAccpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerCall.this,DriverTracking.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("destlat",destlat);
                intent.putExtra("destlng",destlng);
                intent.putExtra("customerToken",customerId);
                intent.putExtra("cusID",cusID);
                intent.putExtra("isKandy",isKandy);

                Accpet = 1;

                FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).
                        child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("reserved").setValue("1");

                if(!TextUtils.isEmpty(customerId))
                    acceptBooking(customerId);

                startActivity(intent);
                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent() != null)
        {
            lat = getIntent().getStringExtra("lat");
            lng = getIntent().getStringExtra("lng");
            destlat = getIntent().getStringExtra("destlat");
            destlng = getIntent().getStringExtra("destlng");
            customerId = getIntent().getStringExtra("customer");
            cusID = getIntent().getStringExtra("cusid");
            if(getIntent().getStringExtra("isKandy").equals("true"))
            {
                isKandy=true;
            }
            else
            {
                isKandy=false;
            }
            getDirection(lat,lng);
        }

        startTimer();
    }

    private void acceptBooking(String customerId) {
        Token token = new Token(customerId);

       /* Notification notification = new Notification("Request Canceled!","Driver has cancelled your request!");
        Sender sender = new Sender(token.getToken(),notification);*/

        Map<String,String> content = new HashMap<>();
        content.put("title","Request Accepted!");
        content.put("driverid",FirebaseAuth.getInstance().getCurrentUser().getUid());
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success==1)
                        {
                            Toast.makeText(CustomerCall.this, "Request Accepted.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void startTimer() {
        CountDownTimer countDownTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtCountDown.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                if(!TextUtils.isEmpty(customerId) && Accpet != 1)
                    cancelBooking(customerId);
            }
        }.start();
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

       /* Notification notification = new Notification("Request Canceled!","Driver has cancelled your request!");
        Sender sender = new Sender(token.getToken(),notification);*/

       Map<String,String> content = new HashMap<>();
       content.put("title","Request Canceled!");
       content.put("message","Driver has cancelled your request!");
       DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success==1)
                        {
                            Toast.makeText(CustomerCall.this, "Request Cancelled.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }


    private void getDirection(String lat, String lng) {

        String requestApi = null;

        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.i("REQUEST_APIXXX",requestApi);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                Log.i("JSONOBJSC",response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //Get first element of routes
                                JSONObject object = routes.getJSONObject(0);

                                // Get Array named legs
                                JSONArray legs = object.getJSONArray("legs");

                                //Get first element of legs
                                JSONObject legsObject = legs.getJSONObject(0);

                                //Get Distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));

                                //Get Distance
                                JSONObject time = legsObject.getJSONObject("duration");
                                txtTime.setText(time.getString("text"));

                                //Get Address
                                String address = legsObject.getString("end_address");
                                txtAddress.setText(address);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(CustomerCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
}

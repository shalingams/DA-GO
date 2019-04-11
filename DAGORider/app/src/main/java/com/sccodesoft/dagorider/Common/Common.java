package com.sccodesoft.dagorider.Common;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sccodesoft.dagorider.Home;
import com.sccodesoft.dagorider.Model.DataMessage;
import com.sccodesoft.dagorider.Model.FCMResponse;
import com.sccodesoft.dagorider.Model.Rider;
import com.sccodesoft.dagorider.Model.Token;
import com.sccodesoft.dagorider.Remote.FCMClient;
import com.sccodesoft.dagorider.Remote.GoogleMapAPI;
import com.sccodesoft.dagorider.Remote.IFCMServices;
import com.sccodesoft.dagorider.Remote.IGoogleAPI;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Common {


    public static boolean isDriverFound=false;
    public static String driverId="";

    public static Rider currentUser = new Rider();

    public static final String CANCEL_BROADCAST_STRING = "cancel_pickup";
    public static final String DROPOFF_BROADCAST_STRING = "drop_off";

    public static final int PICK_IMAGE_REQUEST = 9999;

    public static Location mLastLocation;
    public static LatLng mDestination;

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String rate_detail_tbl = "RateDetails";
    public static final String ongoing_tbl = "OnGoingTrip";

    public static final String fcmURL = "https://fcm.googleapis.com";
    public static final String googleAPIUrl = "https://maps.googleapis.com";

    public static final String user_field = "rider_usr";
    public static final String pwd_field = "rider_pwd";

    public static double base_farex = 50;
    public static double time_ratex = 2;
    public static double distance_ratex = 40;

    public static double base_fareb = 140;
    public static double time_rateb = 4;
    public static double distance_rateb = 42;

    public static double getPrice(double km,int min,boolean isDAGOX)
    {
        if(isDAGOX)
            return(base_farex+(time_ratex*min)+(distance_ratex*(km-1)));
        else
            return(base_fareb+(time_rateb*min)+(distance_rateb*(km-2)));
    }

    public static IFCMServices getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMServices.class);
    }

    public static IGoogleAPI getGoogleService()
    {
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }

    public static void sendRequestToDriver(String driverId, final IFCMServices mService, final Context context, final Location currentLocation, final LatLng destination) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);

        tokens.orderByKey().equalTo(driverId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            //Get token obj from db with key
                            Token token = postSnapShot.getValue(Token.class);


                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            /*Notification data = new Notification(riderToken,json_lat_lng);
                            Sender content = new Sender(token.getToken(),data);*/

                            Map<String,String> content = new HashMap<>();
                            content.put("customer",riderToken);
                            content.put("customerid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            content.put("lat",String.valueOf(currentLocation.getLatitude()));
                            content.put("lng",String.valueOf(currentLocation.getLongitude()));
                            content.put("destlat", String.valueOf(destination.latitude));
                            content.put("destlng", String.valueOf(destination.longitude));
                            DataMessage dataMessage = new DataMessage(token.getToken(),content);


                            mService.sendMessage(dataMessage)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if(response.body().success==1)
                                                Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show();
                                            else
                                                Toast.makeText(context, "Failed !", Toast.LENGTH_SHORT).show();
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
}

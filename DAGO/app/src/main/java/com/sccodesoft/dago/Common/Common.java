package com.sccodesoft.dago.Common;

import android.location.Location;

import com.sccodesoft.dago.Model.Driver;
import com.sccodesoft.dago.Remote.FCMClient;
import com.sccodesoft.dago.Remote.IFCMServices;
import com.sccodesoft.dago.Remote.IGoogleApi;
import com.sccodesoft.dago.Remote.RetrofitClient;

public class Common
{
    public static int GPS_REQUEST=19278;
    public static boolean isGPS=false;

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String ongoing_tbl = "OnGoingTrip";

    public static double distancefare;
    public static double waitingtime;
    public static double totfare;
    public static double distance;

    public static final int PICK_IMAGE_REQUEST = 9999;

    public static Driver currentDriver;

    public static Location mLastLocation=null;
    public static Location tempLoc1,tempLoc2 = null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com";

    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";

    public static double base_farex = 50;
    public static double time_ratex = 2;
    public static double distance_ratex = 35;

    public static double base_fareb = 140;
    public static double time_rateb = 4;
    public static double distance_rateb = 42;

    public static double base_farexk = 80;
    public static double distance_ratexk = 50;

    public static double formulaPrice(double km,int min)
    {
       // return(base_fare+(time_rate*min)+(distance_rate*km));
        return 0;
    }

    public static IGoogleApi getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleApi.class);
    }

    public static IFCMServices getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMServices.class);
    }
}

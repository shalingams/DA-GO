package com.sccodesoft.dago.Common;

import android.location.Location;

import com.sccodesoft.dago.Model.User;
import com.sccodesoft.dago.Remote.FCMClient;
import com.sccodesoft.dago.Remote.IFCMServices;
import com.sccodesoft.dago.Remote.IGoogleApi;
import com.sccodesoft.dago.Remote.RetrofitClient;

public class Common
{
    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";

    public static final int PICK_IMAGE_REQUEST = 9999;

    public static User currentUser;

    public static Location mLastLocation=null;

    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com";

    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";

    public static double base_fare = 50;
    private static double time_rate = 2;
    private static double distance_rate = 40;

    public static double formulaPrice(double km,int min)
    {
        return(base_fare+(time_rate*min)+(distance_rate*km));
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

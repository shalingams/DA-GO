package com.sccodesoft.dagorider.Common;

import com.sccodesoft.dagorider.Model.Rider;
import com.sccodesoft.dagorider.Remote.FCMClient;
import com.sccodesoft.dagorider.Remote.GoogleMapAPI;
import com.sccodesoft.dagorider.Remote.IFCMServices;
import com.sccodesoft.dagorider.Remote.IGoogleAPI;

public class Common {


    public static boolean isDriverFound=false;
    public static String driverId="";

    public static Rider currentUser = new Rider();

    public static final int PICK_IMAGE_REQUEST = 9999;

    public static final String driver_tbl = "Drivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";
    public static final String rate_detail_tbl = "RateDetails";

    public static final String fcmURL = "https://fcm.googleapis.com";
    public static final String googleAPIUrl = "https://maps.googleapis.com";

    public static final String user_field = "rider_usr";
    public static final String pwd_field = "rider_pwd";

    private static double base_fare = 50;
    private static double time_rate = 2;
    private static double distance_rate = 40;

    public static double getPrice(double km,int min)
    {
        return(base_fare+(time_rate*min)+(distance_rate*km));
    }

    public static IFCMServices getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMServices.class);
    }

    public static IGoogleAPI getGoogleService()
    {
        return GoogleMapAPI.getClient(googleAPIUrl).create(IGoogleAPI.class);
    }
}

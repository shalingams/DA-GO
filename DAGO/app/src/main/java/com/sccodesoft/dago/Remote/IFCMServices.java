package com.sccodesoft.dago.Remote;

import com.sccodesoft.dago.Model.FCMResponse;
import com.sccodesoft.dago.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMServices {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAXQ5CYRg:APA91bExnRWZtglBRj3j3aMxo1d7m14JQki-xhXw1I50eWZruB4Rn72dxAhO-6ohy6P3RBf47e0TOLzmtHKO05sgixOZefsKGxFfzAxCG5vTolh0vcf6K_oPgt5lt7HoXHmpoPEblhWC",

    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}

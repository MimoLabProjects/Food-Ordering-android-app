package com.mimolabprojects.fudy.Retrofit;



import com.mimolabprojects.fudy.Model.FCMResponse;
import com.mimolabprojects.fudy.Model.FCMSendData;


import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAn5KwSc8:APA91bFj-cSJ7btrgAdcKBdpr_WaUZl8sIoCHYidwKsrQevU5YQ3BNi7hEE40OaTEQp29U1vqw4aO-ZX54Nq_lxsyQ8KwF2LN6RNfySV8LREayak9dL36Xhy-d9fhlQcs-ocF8gjIZfU"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification (@Body FCMSendData body);
}

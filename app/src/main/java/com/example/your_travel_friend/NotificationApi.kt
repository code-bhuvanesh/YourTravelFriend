package com.example.your_travel_friend

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body

interface NotificationApi {
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}
package com.jatin.dropit.api;

import com.jatin.dropit.model.Response;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UsersService {

    @GET("users")
    Call<Response> getResponse(
            @Query("offset") int offset,
            @Query("limit") int limit
    );

}

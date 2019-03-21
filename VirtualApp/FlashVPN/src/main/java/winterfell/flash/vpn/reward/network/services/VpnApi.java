package winterfell.flash.vpn.reward.network.services;

import com.polestar.task.network.responses.SucceedResponse;

import winterfell.flash.vpn.reward.network.datamodels.VpnRequirement;
import winterfell.flash.vpn.reward.network.responses.ServersResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface VpnApi {
    @Headers("Accept: application/json")
    @GET("api/vpn/v1/getServers")
    Call<ServersResponse> getAvailableVpnServers(@Query("version_code") int versionCode,
                                            @Query("app") String pkgName,
                                            @Query("secret") String secret);

    @Headers("Accept: application/json")
    @POST("api/vpn/v1/acquireBestForGeoCityIp")
    @FormUrlEncoded
    Call<VpnRequirement> acquire(@Field("version_code") int versionCode,
                                 @Field("app") String pkgName,
                                 @Field("public_ip") String publicIp,
                                 @Field("geo") String geo,
                                 @Field("city") String city,
                                 @Field("secret") String secret);

    @Headers("Accept: application/json")
    @POST("api/vpn/v1/release")
    @FormUrlEncoded
    Call<SucceedResponse> release(@Field("version_code") int versionCode,
                                  @Field("app") String pkgName,
                                  @Field("public_ip") String publicIp,
                                  @Field("secret") String secret);
}

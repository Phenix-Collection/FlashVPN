package winterfell.flash.vpn.reward.network;

import com.polestar.ad.AdLog;
import com.polestar.task.ADErrorCode;
import winterfell.flash.vpn.reward.IVpnStatusListener;
import winterfell.flash.vpn.reward.network.datamodels.VpnRequirement;
import winterfell.flash.vpn.reward.network.responses.ServersResponse;
import winterfell.flash.vpn.reward.network.services.VpnApi;

import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.Configuration;
import com.polestar.task.network.ErrorCodeInterceptor;
import com.polestar.task.network.RetrofitServiceFactory;
import com.polestar.task.network.responses.SucceedResponse;
import com.witter.msg.Sender;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VpnApiHelper extends AdApiHelper {
    private static String KEY_GET_VPN_SERVERS = "getVpnServers";
    private static String KEY_ACQUIRE = "acquire";
    private static String KEY_RELEASE = "release";


    public static int getVpnServers(String deviceId, final IVpnStatusListener listener) {
        return getVpnServers(deviceId, listener, false);
    }

    public static int getVpnServers(String deviceId, final IVpnStatusListener listener, boolean force) {
        if (AdApiHelper.checkRequestTooFrequent(KEY_GET_VPN_SERVERS, listener, force) == AdApiHelper.ERR_REQUEST_TOO_FREQUENT) {
            return AdApiHelper.ERR_REQUEST_TOO_FREQUENT;
        }

        VpnApi service = RetrofitServiceFactory.createSimpleRetroFitService(VpnApi.class);
        Call<ServersResponse> call = service.getAvailableVpnServers(Configuration.APP_VERSION_CODE,
                Configuration.PKG_NAME, Sender.Send(AdApiHelper.getSecret(deviceId)));
        call.enqueue(new Callback<ServersResponse>() {
            @Override
            public void onResponse(Call<ServersResponse> call, Response<ServersResponse> response) {
                AdLog.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        ServersResponse ur = response.body();
                        AdLog.i(Configuration.HTTP_TAG, "onResponse vpnServers count "+ ur.mVpnServers.size());
                        if (listener != null) {
                            listener.onGetAllServers(ur);
                        }
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(AdApiHelper.createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<ServersResponse> call, Throwable t) {
                AdLog.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onGeneralError(ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
                }
            }
        });

        return AdApiHelper.REQUEST_SUCCEED;
    }

    public static int acquireVpnServer(String deviceId, final String publicIp,
                                       final String geo, final String city,
                                       final IVpnStatusListener listener) {
        return acquireVpnServer(deviceId, publicIp, geo, city, listener, false);
    }

    public static int acquireVpnServer(String deviceId, final String publicIp,
                                       final String geo, final String city,
                                       final IVpnStatusListener listener, boolean force) {
        if (AdApiHelper.checkRequestTooFrequent(KEY_ACQUIRE, listener, force) == AdApiHelper.ERR_REQUEST_TOO_FREQUENT) {
            return AdApiHelper.ERR_REQUEST_TOO_FREQUENT;
        }

        VpnApi service = RetrofitServiceFactory.createSimpleRetroFitService(VpnApi.class);
        Call<VpnRequirement> call = service.acquire(Configuration.APP_VERSION_CODE,
                Configuration.PKG_NAME, publicIp, geo, city, Sender.Send(AdApiHelper.getSecret(deviceId)));
        call.enqueue(new Callback<VpnRequirement>() {
            @Override
            public void onResponse(Call<VpnRequirement> call, Response<VpnRequirement> response) {
                AdLog.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        VpnRequirement ur = response.body();
                        if (listener != null) {
                            listener.onAcquireSucceed(ur);
                        }
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(AdApiHelper.createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<VpnRequirement> call, Throwable t) {
                AdLog.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onAcquireFailed(publicIp, ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
                }
            }
        });

        return AdApiHelper.REQUEST_SUCCEED;
    }

    public static int releaseVpnServer(String deviceId, final String publicIp, final IVpnStatusListener listener) {
        return releaseVpnServer(deviceId, publicIp, listener, false);
    }

    public static int releaseVpnServer(String deviceId, final String publicIp, final IVpnStatusListener listener, boolean force) {
        if (AdApiHelper.checkRequestTooFrequent(KEY_RELEASE, listener, force) == AdApiHelper.ERR_REQUEST_TOO_FREQUENT) {
            return AdApiHelper.ERR_REQUEST_TOO_FREQUENT;
        }

        VpnApi service = RetrofitServiceFactory.createSimpleRetroFitService(VpnApi.class);
        Call<SucceedResponse> call = service.release(Configuration.APP_VERSION_CODE,
                Configuration.PKG_NAME, publicIp, Sender.Send(AdApiHelper.getSecret(deviceId)));
        call.enqueue(new Callback<SucceedResponse>() {
            @Override
            public void onResponse(Call<SucceedResponse> call, Response<SucceedResponse> response) {
                AdLog.i(Configuration.HTTP_TAG, "onResponse: "+ response.toString());

                switch(response.code()){
                    case 200:
                        SucceedResponse ur = response.body();
                        if (listener != null) {
                            listener.onReleaseSucceed(publicIp);
                        }
                        break;
                    default:
                        if (listener != null) {
                            listener.onGeneralError(AdApiHelper.createADErrorFromResponse(response));
                        }
                        break;
                }
            }

            @Override
            public void onFailure(Call<SucceedResponse> call, Throwable t) {
                AdLog.e(Configuration.HTTP_TAG, "onFailure: " + t.getMessage());
                if (listener != null) {
                    if (ErrorCodeInterceptor.isAdErrorMsg(t.getMessage())) {
                        listener.onReleaseFailed(publicIp, ADErrorCode.createFromAdErrMsg(t.getMessage()));
                    } else {
                        listener.onGeneralError(ADErrorCode.createServerDown());
                    }
                }
            }
        });

        return AdApiHelper.REQUEST_SUCCEED;
    }
}

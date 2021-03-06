/*
 ThetaBatteryProfile
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;

import com.theta360.lib.ThetaException;

import org.deviceconnect.android.deviceplugin.theta.ThetaApi;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiClient;
import org.deviceconnect.android.deviceplugin.theta.ThetaApiTask;
import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.BatteryProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.IOException;

/**
 * Theta Battery Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaBatteryProfile extends BatteryProfile {

    private final ThetaApiClient mClient;

    /**
     * Constructor.
     *
     * @param client an instance of {@link ThetaApiClient}
     */
    public ThetaBatteryProfile(final ThetaApiClient client) {
        mClient = client;
    }

    @Override
    protected boolean onGetAll(final Intent request, final Intent response, final String serviceId) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    setLevel(response, api.getBatteryLevel());
                    setCharging(response, false);
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    @Override
    protected boolean onGetLevel(final Intent request, final Intent response, final String serviceId) {
        if (!mClient.hasDevice(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        mClient.execute(new ThetaApiTask() {
            @Override
            public void run(final ThetaApi api) {
                try {
                    setLevel(response, api.getBatteryLevel());
                    setResult(response, DConnectMessage.RESULT_OK);
                } catch (ThetaException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                getService().sendResponse(response);
            }
        });
        return false;
    }

    private ThetaDeviceService getService() {
        return ((ThetaDeviceService) getContext());
    }

}

/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.polestar.billing;

import com.android.billingclient.api.BillingClient.SkuType;
import com.polestar.superclone.utils.RemoteConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static fields and methods useful for billing
 */
public final class BillingConstants {
    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    //public static final String SKU_AD_FREE = "test_not_buy";

    public static final String VIP_1_MONTH = "premium_service_one_month";
    public static final String VIP_3_MONTH = "premium_service_3_month";
    public static final String VIP_1_YEAR = "premium_service_1_year";

    private static String[] IN_APP_SUBS = new String[] {
            VIP_1_MONTH,VIP_3_MONTH,VIP_1_YEAR
    };

    public static final String BILL_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxQakTO2v3EWnAKAuCT/KlbFPN4FgGQrzbz1lW85TnzbUKB4faOhg8yG5lIOY3n3m94v11Grt5Guq/BIx2dl4EHT8rESfjNCWwokL6HvRZ+FPxAAntOSHf810RXmpVhN1PmdhV2ZRWzvPKBI9xBrL2PDn3RQOnTnbcV1NGmojJA5AMChRA/5Ntkfh0qEUCYiM7rMd94RAslM2gjG9KGeC5H87mRasutehOyFLYz3oM7dLQCdPWiZ8lPCDNHFDae/U3nxmD7p3c5IYAbh0lyoshaF/pRPIE5CAPVx2M5muA4y9dgbyyPBtAfI6atogROArNVshLcS+paDJ1C3zSADyTwIDAQAB";

    private BillingConstants(){}

    /**
     * Returns the list of all SKUs for the billing type specified
     */
    public static final List<String> getSkuList(@SkuType String billingType) {
        String list = RemoteConfig.getString("active_sub_sku_items");
        if (IN_APP_SUBS == null) {
            IN_APP_SUBS = list.split(";");
        }
        return (billingType == SkuType.INAPP) ? new ArrayList<String>(0)
                : Arrays.asList(IN_APP_SUBS);
    }
}


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

import com.android.billingclient.api.BillingClient;
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

    private static String[] IN_APP_SUBS;

    public static final String BILL_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoFJpEUoPNkUa5vhq7wakFXaIAG/YW+04U0P+/gMg4fGP9uRg9N5kAvXRAe+FcEeHbXHtSlavjQs7yNzgoatGtCdsYlMGwKG9UAyAIItO6i1FABwYNCY1Nr69mObx5aZMMTv50aWif4UPQ3kjS0mtOnodnsqMS87JvA1HdueNz0pEamB7vJFIn9lrzJG1kuTUVWYopA7oD5NL7gbwsbHd6fz3q0Wg7aS233Sk2qVVMtBCmZ/aB0plnziWl+qwAyf4IMbT7R2l42Cj4JtLrcp4IIFXFISEjP8rpYCrC9AIPewxNO5LMQKsq7Rx6ve0cqxAlPcEF4HHNwGfhKJYrQ9iJQIDAQAB";

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


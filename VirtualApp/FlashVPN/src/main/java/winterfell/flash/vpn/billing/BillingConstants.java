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
package winterfell.flash.vpn.billing;

import com.android.billingclient.api.BillingClient.SkuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import winterfell.flash.vpn.utils.RemoteConfig;

/**
 * Static fields and methods useful for billing
 */
public final class BillingConstants {
    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    //public static final String SKU_AD_FREE = "test_not_buy";

    private static String[] IN_APP_SUBS;

    public static final String BILL_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjNYxtmLDqe9bD9/tQOJa8K05X+Rcna8GjO7R7vtHvGKTU+FnbmOojCww9zczWIeL9rpguz2a/FHlBqeHHx+8r2tfn3oN9DcLyi5Q0b4apxBInVH+UwvAboP5GYzuF42mvWH4jEcsNhzRiUImBx6gjCOWZabaD+gAV4sKkNK2MbfUOMPOqrKD/IAgMkalkIRMcPD6lF5hf4wOpgiFtA+zWoHk9NRxw9Ak7U61ge0cnZUHId2dZ6zl2uiXy5iPwI5+IzKtETPzjoNZkQUKS5avDSu+UWyBgI9e0RgYlHQJJAB6n1/4BKba4ZXy41pYtpAZHgfnboX5ZAYC5rsenTCJOwIDAQAB";

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


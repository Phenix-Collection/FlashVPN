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
package nova.fast.free.vpn.billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.SkuType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nova.fast.free.vpn.utils.RemoteConfig;

/**
 * Static fields and methods useful for billing
 */
public final class BillingConstants {
    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    //public static final String SKU_AD_FREE = "test_not_buy";

    private static String[] IN_APP_SUBS;

    public static final String BILL_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsEcgyBJZFOYH3ZkpktIOEE9WlnmNFjf4mDK0zdt4GghOxdh6U1Z0sLNEJc/RjhaCl7XhCLVgiSRGZCwvoDEKqh790srBK7UWipJkR1ydrMpjPn8ouJBhqOslJAigiTB6Ib+2KD98q98YSC8QRhtDLfLHL1LTYY/sUjeMJEU6/OtayPs99MwwoSRxEHuO3NrAc1cFWh8N/wNK5i4uZmi4eubREBQZeqg6C+/2qYrAtXAiQnwZpBN8o84XxFjH2sA+5YFf56ZBIIrLxLUQV1lflaOI9NnKwy/33p+I43zGi3lJ74j4YA+pydQmBha7slvIZ14haee5uatLko0cvRWzBwIDAQAB";

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


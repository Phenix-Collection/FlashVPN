package in.dualspace.cloner.billing;

/**
 * Created by DualApp on 2017/6/25.
 */


import android.app.Activity;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.FeatureType;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.Purchase.PurchasesResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails.SkuDetailsResult;
import com.android.billingclient.api.SkuDetailsResponseListener;
import in.dualspace.cloner.BuildConfig;
import in.dualspace.cloner.utils.MLogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles all the interactions with Play Store (via Billing library), maintains connection to
 * it through BillingClient and caches temporary states/data if needed
 */
public class BillingManager implements PurchasesUpdatedListener {
    // Default value of mBillingClientResponseCode until BillingManager was not yeat initialized
    public static final int BILLING_MANAGER_NOT_INITIALIZED  = -1;

    private static final String TAG = "BillingManager";

    /** A reference to BillingClient **/
    private BillingClient mBillingClient;

    /**
     * True if billing service is connected now.
     */
    private boolean mIsServiceConnected;

    private final BillingUpdatesListener mBillingUpdatesListener;

    private final Context mContext;

    private final List<Purchase> mPurchases = new ArrayList<>();

    private Set<String> mTokensToBeConsumed;

    private int mBillingClientResponseCode = BILLING_MANAGER_NOT_INITIALIZED;

    /* BASE_64_ENCODED_PUBLIC_KEY should be YOUR APPLICATION'S PUBLIC KEY
     * (that you got from the Google Play developer console). This is not your
     * developer public key, it's the *app-specific* public key.
     *
     * Instead of just storing the entire literal string here embedded in the
     * program,  construct the key at runtime from pieces or
     * use bit manipulation (for example, XOR with some other string) to hide
     * the actual key.  The key itself is not secret information, but we don't
     * want to make it easy for an attacker to replace the public key with one
     * of their own and then fake messages from the server.
     */
    private static final String BASE_64_ENCODED_PUBLIC_KEY = "ArtdUkbxgaPFIg68T4jLZIcamls+0ShzawmOX7+JEd9yjzKRfnmQoCdr2P7ZX2HVXpjIRdxQIDAQAB";

    /**
     * Listener to the updates that happen when purchases list was updated or consumption of the
     * item was finished
     */
    public interface BillingUpdatesListener {
        void onBillingClientSetupFinished();
        void onConsumeFinished(String token, @BillingResponse int result);
        void onPurchasesUpdated(List<Purchase> purchases);
    }

//    public String PUB_KEY ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvmuCaE7SGRpShOzw8zlZdQvDqDRyhOuWVHSKlWX5bJSSqYNmlZb3vKYDIp3SGXaZvnWbK3DY12rC6hMGrkgKmKcuERycgAyYOmRilbZOSIBRboo+tdssfTrSXprWaDAE51rbvb4zSJkgNeU1yS47n47sUSQd9qMZVLT70h/aJWteATq20TO9XFiqgsk6E1kQSjusSDWL98pPr6AJLSGa5SWtZwv3EMzrroiiRYQZTiRoD4UqJC5RP4rbKh7DmIWL4M9QtqR0TqeFGUxyPfgzh4j/EZMulN3jCEDrQ0oj8vmL4ObP3d3SMTQm2yqU2GYlhNj3MGMmZkjCs2RRXDwS4wIDAQAB"
//    public static String PUB_KEY_dom = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvmuCaE7SGRpShOzw8zlZdQvDqDRyhOuWVHSKlWX5bJSSqYNmlZb3vKYDIp3SGXaZvnWbK3DY12rC6hMGrkgKmKcuERycgAyYOmRilbZOSIBRboo+tdssfTrSXprWaDAE51rbvb4zSJkgNeU1yS47n47sUSQd9qMZVLT70h/aJWteATq20TO9XFiqgsk6E1kQSjusSDWL98pPr6AJLSGa5SWtZwv3EMzrroiiRYQZTiRoD4UqJC5RP4rbKh7DmIWL4M9QtqR0TqeFGUxyPfgzh4j/EZMulN3jCEDrQ0oj8vmL4ObP3d3SMTQm2yqU2GYlhNj3MGMmZkjCs2RRXDwS4wIDAQAB";
    private static final String KEY = "RgRDo~YcA@^E]zAN#t\\EEgOX{zLt%`]OR_f%EQNwL`xAt]%RO'$dU ~[Qd}q]{]ucSDo";
    private static final String KEY2 = "gqe} S'}GE|ceERAZ/.fFd W\\ZEQw#EAbLa`%S[lddy\u007F\u007FDOGLB\u007FDyR\"Cg\\U#DF\"dt]~!R{_AZ\"[/GbgD&BgsPQCnoFpql~\"|9SL[czX%|USRdG&y|.`{Z\"YtF%r%E[BG{$ogC$QOz~X|%[Q[{L}|Ue$DDNRaE\"a_RWGWT";
    private static String hash = null;
    private static String getKey() {
//        if (!BuildConfig.DEBUG) {
//            throw new NullPointerException();
//        }
        if (hash == null) {
            StringBuffer str3 = new StringBuffer();  //存储解密后的字符串
            for (int i = 0; i < BillingConstants.BILL_KEY.length(); i++) {
                char c = (char) (BillingConstants.BILL_KEY.charAt(i) ^ 22);
                str3.append(c);
            }
            for (int i = 0; i < KEY.length(); i++) {
                char c = (char) (KEY.charAt(i) ^ 22);
                str3.append(c);
            }
            for (int i = 0; i < BillingProvider.KEY.length(); i++) {
                char c = (char) (BillingProvider.KEY.charAt(i) ^ 22);
                str3.append(c);
            }
            for (int i = 0; i < KEY2.length(); i++) {
                char c = (char) (KEY2.charAt(i) ^ 22);
                str3.append(c);
            }
            hash = str3.toString();
        }
        return hash;
    }
    /**
     * Listener for the Billing client state to become connected
     */
    public interface ServiceConnectedListener {
        void onServiceConnected(@BillingResponse int resultCode);
    }

    public BillingManager(Context ctx, final BillingUpdatesListener updatesListener) {
        MLogs.d(TAG, "Creating Billing client.");
        mContext = ctx;
        mBillingUpdatesListener = updatesListener;
        mBillingClient = new BillingClient.Builder(mContext).setListener(this).build();

        MLogs.d(TAG, "Starting setup.");
        getKey();

        // Start setup. This is asynchronous and the specified listener will be called
        // once setup completes.
        // It also starts to report all the new purchases through onPurchasesUpdated() callback.
        startServiceConnection(new Runnable() {
            @Override
            public void run() {
                // Notifying the listener that billing client is ready
                mBillingUpdatesListener.onBillingClientSetupFinished();
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                MLogs.d(TAG, "Setup successful. Querying inventory.");
                queryPurchases();
            }
        });
    }

    /**
     * Handle a callback that purchases were updated from the Billing library
     */
    @Override
    public void onPurchasesUpdated(int resultCode, List<Purchase> purchases) {
        if (resultCode == BillingResponse.OK) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
            mBillingUpdatesListener.onPurchasesUpdated(mPurchases);
        } else if (resultCode == BillingResponse.USER_CANCELED) {
            MLogs.i(TAG, "onPurchasesUpdated() - user cancelled the purchase flow - skipping");
        } else {
            MLogs.e(TAG, "onPurchasesUpdated() got unknown resultCode: " + resultCode);
        }
    }

    /**
     * Start a purchase flow
     */
    public void initiatePurchaseFlow(final Activity activity, final String skuId, final @SkuType String billingType) {
        initiatePurchaseFlow(activity, skuId, null, billingType);
    }

    /**
     * Start a purchase or subscription replace flow
     */
    public void initiatePurchaseFlow(final Activity activity, final String skuId, final ArrayList<String> oldSkus,
                                     final @SkuType String billingType) {
        Runnable purchaseFlowRequest = new Runnable() {
            @Override
            public void run() {
                MLogs.d(TAG, "Launching in-app purchase flow. Replace old SKU? " + (oldSkus != null));
                BillingFlowParams purchaseParams = new BillingFlowParams.Builder()
                        .setSku(skuId).setType(billingType).setOldSkus(oldSkus).build();
                mBillingClient.launchBillingFlow(activity, purchaseParams);
            }
        };

        executeServiceRequest(purchaseFlowRequest);
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Clear the resources
     */
    public void destroy() {
        MLogs.d(TAG, "Destroying the manager.");

        if (mBillingClient != null && mBillingClient.isReady()) {
            mBillingClient.endConnection();
            mBillingClient = null;
        }
    }

    public void querySkuDetailsAsync(@SkuType final String itemType, final List<String> skuList,
                                     final SkuDetailsResponseListener listener) {
        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable queryRequest = new Runnable() {
            @Override
            public void run() {
                // Query the purchase async
                mBillingClient.querySkuDetailsAsync(itemType, skuList,
                        new SkuDetailsResponseListener() {
                            @Override
                            public void onSkuDetailsResponse(SkuDetailsResult result) {
                                listener.onSkuDetailsResponse(result);
                            }
                        });
            }
        };

        executeServiceRequest(queryRequest);
    }

    public void consumeAsync(final String purchaseToken) {
        // If we've already scheduled to consume this token - no action is needed (this could happen
        // if you received the token when querying purchases inside onReceive() and later from
        // onActivityResult()
        if (mTokensToBeConsumed == null) {
            mTokensToBeConsumed = new HashSet<>();
        } else if (mTokensToBeConsumed.contains(purchaseToken)) {
            MLogs.i(TAG, "Token was already scheduled to be consumed - skipping...");
            return;
        }
        mTokensToBeConsumed.add(purchaseToken);

        // Generating Consume Response listener
        final ConsumeResponseListener onConsumeListener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(String outToken, @BillingResponse int billingResult) {
                // If billing service was disconnected, we try to reconnect 1 time
                // (feel free to introduce your retry policy here).
                mBillingUpdatesListener.onConsumeFinished(outToken, billingResult);
            }
        };

        // Creating a runnable from the request to use it inside our connection retry policy below
        Runnable consumeRequest = new Runnable() {
            @Override
            public void run() {
                // Consume the purchase async
                mBillingClient.consumeAsync(purchaseToken, onConsumeListener);
            }
        };

        executeServiceRequest(consumeRequest);
    }

    /**
     * Returns the value Billing client response code or BILLING_MANAGER_NOT_INITIALIZED if the
     * clien connection response was not received yet.
     */
    public int getBillingClientResponseCode() {
        return mBillingClientResponseCode;
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * See {@link Security#verifyPurchase(String, String, String)}
     * </p>
     * @param purchase Purchase to be handled
     */
    private void handlePurchase(Purchase purchase) {
        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
            MLogs.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
            return;
        }

        MLogs.d(TAG, "Got a verified purchase: " + purchase);

        mPurchases.add(purchase);
    }

    /**
     * Handle a result from querying of purchases and report an updated list to the listener
     */
    private void onQueryPurchasesFinished(Purchase.PurchasesResult result) {
        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (mBillingClient == null || result.getResponseCode() != BillingResponse.OK) {
            MLogs.e(TAG, "Billing client was null or result code (" + result.getResponseCode()
                    + ") was bad - quitting");
            return;
        }

        MLogs.d(TAG, "Query inventory was successful.");

        // Update the UI and purchases inventory with new list of purchases
        mPurchases.clear();
        onPurchasesUpdated(BillingResponse.OK, result.getPurchasesList());
    }

    /**
     * Checks if subscriptions are supported for current client
     * <p>Note: This method does not automatically retry for RESULT_SERVICE_DISCONNECTED.
     * It is only used in unit tests and after queryPurchases execution, which already has
     * a retry-mechanism implemented.
     * </p>
     */
    public boolean areSubscriptionsSupported() {
        int responseCode = mBillingClient.isFeatureSupported(FeatureType.SUBSCRIPTIONS);
        if (responseCode != BillingResponse.OK) {
            MLogs.e(TAG, "areSubscriptionsSupported() got an error response: " + responseCode);
        }
        return responseCode == BillingResponse.OK;
    }

    /**
     * Query purchases across various use cases and deliver the result in a formalized way through
     * a listener
     */
    public void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis();
                PurchasesResult purchasesResult = null;
                try {
                    purchasesResult = mBillingClient.queryPurchases(SkuType.INAPP);
                    MLogs.i(TAG, "Querying purchases elapsed time: " + (System.currentTimeMillis() - time)
                            + "ms");
                    // If there are subscriptions supported, we add subscription rows as well
                    if (areSubscriptionsSupported()) {
                        PurchasesResult subscriptionResult
                                = mBillingClient.queryPurchases(SkuType.SUBS);
                        MLogs.i(TAG, "Querying purchases and subscriptions elapsed time: "
                                + (System.currentTimeMillis() - time) + "ms");
                        MLogs.i(TAG, "Querying subscriptions result code: "
                                + subscriptionResult.getResponseCode());
                        if (subscriptionResult.getPurchasesList() != null) {
                            MLogs.i(TAG, " res: " + subscriptionResult.getPurchasesList().size());
                        }

                        if (subscriptionResult.getResponseCode() == BillingResponse.OK) {
                            purchasesResult.getPurchasesList().addAll(
                                    subscriptionResult.getPurchasesList());
                        } else {
                            MLogs.logBug(TAG, "Got an error response trying to query subscription purchases");
                        }
                    } else if (purchasesResult.getResponseCode() == BillingResponse.OK) {
                        MLogs.i(TAG, "Skipped subscription purchases query since they are not supported");
                    } else {
                        MLogs.e(TAG, "queryPurchases() got an error response code: "
                                + purchasesResult.getResponseCode());
                    }
                } catch (Exception e) {
                    MLogs.logBug(TAG, MLogs.getStackTraceString(e));
                }
                if (purchasesResult == null) {
                    purchasesResult = new PurchasesResult(null, BillingResponse.SERVICE_DISCONNECTED);
                }
                onQueryPurchasesFinished(purchasesResult);
            }
        };

        executeServiceRequest(queryToExecute);
    }

    public void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
                MLogs.d(TAG, "Setup finished. Response code: " + billingResponseCode);

                if (billingResponseCode == BillingResponse.OK) {
                    mIsServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
                mBillingClientResponseCode = billingResponseCode;
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
            }
        });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (mIsServiceConnected) {
            runnable.run();
        } else {
            // If billing service was disconnected, we try to reconnect 1 time.
            // (feel free to introduce your retry policy here).
            startServiceConnection(runnable);
        }
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)

        try {
            return Security.verifyPurchase(getKey(), signedData, signature);
        } catch (IOException e) {
            MLogs.logBug(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }
}

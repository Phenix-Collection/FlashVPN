package nova.fast.free.vpn.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.polestar.task.network.Configuration;
import com.polestar.task.network.RetrofitServiceFactory;
import com.polestar.task.network.datamodels.UserProduct;
import com.polestar.task.network.datamodels.UserTask;
import com.polestar.task.network.responses.ProductsResponse;
import com.polestar.task.network.responses.TasksResponse;
import com.polestar.task.network.responses.UserResponse;
import com.polestar.task.network.services.AuthApi;
import com.polestar.task.network.services.ProductsApi;
import com.polestar.task.network.services.TasksApi;

import nova.fast.free.vpn.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        testRegister("fakedeviceId");
        testGetAvailableProducts();
        testGetAvailableTasks();
        consumeProduct("fakedeviceId",1);
        finishTask("fakedeviceId",1);
    }

    private void testRegister(String deviceId) {
        AuthApi service = RetrofitServiceFactory.createSimpleRetroFitService(AuthApi.class);
        Call<UserResponse> call = service.registerAnonymous(deviceId);
        call.enqueue(new Callback<UserResponse>(){
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response){
                Log.e(Configuration.HTTP_TAG, "onResponse: "+ response.toString() + " response.message()" +response.message()
                    );

                switch(response.code()){
                    case 200:
                        UserResponse ur = response.body();
                        Log.e(Configuration.HTTP_TAG, "onResponse: "+ ur.mReferralCode);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t){
                Log.e(Configuration.HTTP_TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    private void testGetAvailableProducts() {
        ProductsApi service = RetrofitServiceFactory.createSimpleRetroFitService(ProductsApi.class);
        Call<ProductsResponse> call = service.getAvailableProducts();
        call.enqueue(new Callback<ProductsResponse>() {
            @Override
            public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                Log.e(Configuration.HTTP_TAG, "onResponse: "+ response.toString() + " response.message()" +response.message()
                );

                switch(response.code()){
                    case 200:
                        ProductsResponse ur = response.body();
                        Log.e(Configuration.HTTP_TAG, "onResponse product count: "+ ur.mProducts.size());

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<ProductsResponse> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    private void consumeProduct(String deviceId, long id) {
        ProductsApi service = RetrofitServiceFactory.createSimpleRetroFitService(ProductsApi.class);
        Call<UserProduct> call = service.consumeProduct(deviceId, id);
        call.enqueue(new Callback<UserProduct>() {
            @Override
            public void onResponse(Call<UserProduct> call, Response<UserProduct> response) {
                Log.e(Configuration.HTTP_TAG, "onResponse: "+ response.toString() + " response.message()" +response.message()
                );

                switch(response.code()){
                    case 200:
                        UserProduct ur = response.body();
                        Log.e(Configuration.HTTP_TAG, "onResponse cost: "+ ur.mCost);

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserProduct> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    private void testGetAvailableTasks() {
        TasksApi service = RetrofitServiceFactory.createSimpleRetroFitService(TasksApi.class);
        Call<TasksResponse> call = service.getAvailableTasks();
        call.enqueue(new Callback<TasksResponse>() {
            @Override
            public void onResponse(Call<TasksResponse> call, Response<TasksResponse> response) {
                Log.e(Configuration.HTTP_TAG, "onResponse: "+ response.toString() + " response.message()" +response.message()
                );

                switch(response.code()){
                    case 200:
                        TasksResponse ur = response.body();
                        Log.e(Configuration.HTTP_TAG, "onResponse task count "+ ur.mTasks.size());

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<TasksResponse> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: "+t.getMessage());
            }
        });
    }

    private void finishTask(String deviceId, long id) {
        TasksApi service = RetrofitServiceFactory.createSimpleRetroFitService(TasksApi.class);
        Call<UserTask> call = service.finishTask(deviceId, id);
        call.enqueue(new Callback<UserTask>() {
            @Override
            public void onResponse(Call<UserTask> call, Response<UserTask> response) {
                Log.e(Configuration.HTTP_TAG, "onResponse: "+ response.toString() + " response.message()" +response.message()
                );

                switch(response.code()){
                    case 200:
                        UserTask ur = response.body();
                        Log.e(Configuration.HTTP_TAG, "onResponse paid: "+ ur.mPayout);

                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(Call<UserTask> call, Throwable t) {
                Log.e(Configuration.HTTP_TAG, "onFailure: "+t.getMessage());
            }
        });
    }

}

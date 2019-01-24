package nova.fast.free.vpn.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.polestar.task.ADErrorCode;
import com.polestar.task.ITaskStatusListener;
import com.polestar.task.database.DatabaseImplFactory;
import com.polestar.task.network.AdApiHelper;
import com.polestar.task.network.Configuration;
import com.polestar.task.network.datamodels.Task;

import java.util.ArrayList;

import nova.fast.free.vpn.R;

import static com.polestar.task.network.AdApiHelper.consumeProduct;
import static com.polestar.task.network.AdApiHelper.finishTask;
import static com.polestar.task.network.AdApiHelper.getAvailableProducts;
import static com.polestar.task.network.AdApiHelper.getAvailableTasks;

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

        DatabaseImplFactory.getDatabaseApi(AdTestActivity.this);

        AdApiHelper.register("fakedeviceId", null);

       /* getAvailableProducts(null);
        getAvailableTasks(new ITaskStatusListener() {
            @Override
            public void onTaskSuccess(long taskId, float payment, float balance) {
            }

            @Override
            public void onTaskFail(long taskId, ADErrorCode code) {
            }

            @Override
            public void onGetAllAvailableTasks(ArrayList<Task> tasks) {
                DatabaseImplFactory.getDatabaseApi(AdTestActivity.this).setActiveTasks(tasks);
            }

            @Override
            public void onGeneralError(ADErrorCode code) {
                Log.i(Configuration.HTTP_TAG, code.toString());
            }
        });
        consumeProduct("fakedeviceId",1, 2, null);
        finishTask("fakedeviceId",1, null);*/
    }



}

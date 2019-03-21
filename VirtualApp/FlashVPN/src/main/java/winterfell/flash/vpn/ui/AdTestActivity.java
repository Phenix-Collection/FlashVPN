package winterfell.flash.vpn.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.polestar.task.database.DatabaseImplFactory;

import winterfell.flash.vpn.R;

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

package nova.fast.free.vpn.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.polestar.task.network.AdApiHelper;

import nova.fast.free.vpn.R;

import static com.polestar.task.network.AdApiHelper.consumeProduct;
import static com.polestar.task.network.AdApiHelper.finishTask;
import static com.polestar.task.network.AdApiHelper.testGetAvailableProducts;
import static com.polestar.task.network.AdApiHelper.testGetAvailableTasks;

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

        AdApiHelper.testRegister("fakedeviceId");
        testGetAvailableProducts();
        testGetAvailableTasks();
        consumeProduct("fakedeviceId",1);
        finishTask("fakedeviceId",1);
    }



}

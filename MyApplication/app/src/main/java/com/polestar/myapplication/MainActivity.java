package com.polestar.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.UserManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap btm = BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher);
                Intent intent = new Intent(MainActivity.this,
                        MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        MainActivity.this, 0, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
                Notification noti = new NotificationCompat.Builder(
                        MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(btm)
                        .setNumber(13)
                        .setContentIntent(pendingIntent)
                        .setStyle(
                                new NotificationCompat.InboxStyle()
                                        .addLine(
                                                "M.Twain (Google+) Haiku is more than a cert...")
                                        .addLine("M.Twain Reminder")
                                        .addLine("M.Twain Lunch?")
                                        .addLine(
                                                "Google Play Celebrate 25 billion apps with Goo..")
                                        .addLine(
                                                "Stack Exchange StackOverflow weekly Newsl...")
                                        .setBigContentTitle("6 new message")
                                        .setSummaryText("mtwain@android.com"))
                        .build();
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(0, noti);
                mNotificationManager.notify(1, noti);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Intent test = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        Log.e("SPC", "ACTION_BATTERY_CHANGED " + test);

//        UserManager  um = (UserManager) getSystemService(Context.USER_SERVICE);
//        String name = um.getUserName();
//        Log.e("SPC", "UserName " + name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

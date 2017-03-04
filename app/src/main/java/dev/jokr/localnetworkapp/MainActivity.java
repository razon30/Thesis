package dev.jokr.localnetworkapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import dev.jokr.localnet.LocalServer;
import dev.jokr.localnetworkapp.BroadCastReceiver.BatteryChargeReceiver;
import dev.jokr.localnetworkapp.discovery.DiscoveryFragment;
import dev.jokr.localnetworkapp.session.SessionFragment;

public class MainActivity extends AppCompatActivity implements DiscoveryFragment.FragmentInteractionListener, BatteryChargeReceiver.GetChargeInterface {

    Button btnJoin;
    Button btnCreate;
    FrameLayout layoutMain;

    private LocalServer localServer;
    private boolean isServer;
    IntentFilter iFilter;
    BatteryChargeReceiver receiver;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCreate = (Button) findViewById(R.id.btn_create);
        btnJoin = (Button) findViewById(R.id.btn_join);
        layoutMain = (FrameLayout) findViewById(R.id.layout_main);

        showDiscoveryFragment();
        receiver = new BatteryChargeReceiver();

        textView = (TextView) findViewById(R.id.tv_percentage);
        iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver,iFilter);
        receiver.setListener(this);





    }


    private void showDiscoveryFragment() {
        DiscoveryFragment fragment = new DiscoveryFragment();
        fragment.setListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main, fragment)
                .commit();
    }

    @Override
    public void onStartSession() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main, SessionFragment.newInstance(SessionFragment.SERVER))
                .commit();
    }

    @Override
    public void onStartClientSession() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.layout_main, SessionFragment.newInstance(SessionFragment.CLIENT))
                .commit();
    }

    @Override
    public void BatteryCharge(String charge) {
        textView.setText(charge);
    }
}

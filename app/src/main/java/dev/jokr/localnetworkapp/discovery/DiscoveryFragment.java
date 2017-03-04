package dev.jokr.localnetworkapp.discovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dev.jokr.localnet.LocalClient;
import dev.jokr.localnet.LocalServer;
import dev.jokr.localnet.models.Payload;
import dev.jokr.localnetworkapp.GameSession;
import dev.jokr.localnetworkapp.R;
import dev.jokr.localnetworkapp.models.MyProfile;
import dev.jokr.localnetworkapp.session.MessagesAdapter;

/**
 * Created by JoKr on 9/3/2016.
 */
public class DiscoveryFragment extends Fragment implements LocalServer.OnUiEventReceiver {

    Button btnJoin;
    Button btnCreate;
    Button btnStartSession;
    RecyclerView connectedClients;

    private LocalServer localServer;
    private boolean isServer;
    private MessagesAdapter adapter;
    RecyclerView recyclerView;
    private FragmentInteractionListener listener;


    Button stopWifi;
    WifiReceiver receiverWifi;
    WifiManager wifimanager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);

        btnCreate = (Button) view.findViewById(R.id.btn_create);
        btnJoin = (Button) view.findViewById(R.id.btn_join);
        btnStartSession = (Button) view.findViewById(R.id.btn_start_session);
        connectedClients = (RecyclerView) view.findViewById(R.id.list_clients);
        connectedClients.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessagesAdapter(LayoutInflater.from(getContext()));
        connectedClients.setAdapter(adapter);

        stopWifi = (Button) view.findViewById(R.id.stop);
        wifimanager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        final boolean[] b = {true};

        stopWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (b[0]) {wifimanager.setWifiEnabled(false); b[0] = false;}
                else {

                    wifimanager.setWifiEnabled(true); b[0] = true;
                    receiverWifi = new WifiReceiver();
                    getActivity().registerReceiver(receiverWifi, new IntentFilter(
                            WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    doInback();

                }
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createSession();
            }
        });

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinSession();
            }
        });

        btnStartSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSession();
            }
        });

        return view;
    }

    // Method for starting the server
    public void createSession() {
        btnCreate.setEnabled(false);
        btnJoin.setEnabled(false);
        btnStartSession.setVisibility(View.VISIBLE);
        isServer = true;

        localServer = new LocalServer(getContext());
        localServer.setReceiver(this);
        localServer.init();
    }

    // Method for starting the client
    public void joinSession() {
        btnCreate.setEnabled(false);
        isServer = false;

        LocalClient localClient = new LocalClient(getContext());

        String name = Build.USER;
        String device = Build.DEVICE;

        localClient.connect(new Payload<MyProfile>(new MyProfile(name, device)));
        localClient.setDiscoveryReceiver(new LocalClient.DiscoveryStatusReceiver() {
            @Override
            public void onDiscoveryTimeout() {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Discovery timeout", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onServerDiscovered() {
                Toast.makeText(getContext(), "Server discovered!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSessionStart() {
                Log.d("USER", "Start client session!");
                if (listener != null) listener.onStartClientSession();
            }
        });
        localClient.setReceiver(new LocalClient.MessageReceiver() {
            @Override
            public void onMessageReceived(Payload<?> payload) {
                String toShow = "" + payload.getPayload();
                if (getContext() != null) {
                    adapter.addMessage(toShow);
                    Toast.makeText(getContext(), toShow, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Server should start session, after discovery is over.
    public void startSession() {
        btnCreate.setEnabled(false);
        btnJoin.setEnabled(false);

        localServer.setSession(GameSession.class);
        if (listener != null) {
            listener.onStartSession();
        }
    }


    @Override
    public void onUiEvent(Payload<?> payload) {
        // UiEvent shouldn't happen during discovery because Session is not yet created
    }

    @Override
    public void onClientConnected(Payload<?> payload) {
        adapter.addMessage("" + payload.getPayload());
    }

    public void setListener(FragmentInteractionListener listener) {
        this.listener = listener;
    }

    public interface FragmentInteractionListener {
        public void onStartSession();
        public void onStartClientSession();
    }






    class WifiReceiver extends BroadcastReceiver
    {
        public void onReceive(Context c, Intent intent)
        {

            ArrayList<String> connections=new ArrayList<String>();
            ArrayList<Float> Signal_Strenth= new ArrayList<Float>();

          //  sb = new StringBuilder();
            List<ScanResult> wifiList;
            wifiList = wifimanager.getScanResults();
            for(int i = 0; i < wifiList.size(); i++)
            {

                connections.add(wifiList.get(i).SSID);
                adapter.addMessage(wifiList.get(i).SSID);
                Toast.makeText(getContext(), wifiList.get(i).SSID, Toast.LENGTH_LONG).show();

            }


        }
    }

    public void doInback()
    {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                wifimanager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

                receiverWifi = new WifiReceiver();
                getActivity().registerReceiver(receiverWifi, new IntentFilter(
                        WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifimanager.startScan();
                doInback();
            }
        }, 1000);

    }



}

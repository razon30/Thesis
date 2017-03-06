package dev.jokr.localnetworkapp.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import dev.jokr.localnet.LocalClient;
import dev.jokr.localnet.LocalServer;
import dev.jokr.localnet.models.Payload;
import dev.jokr.localnetworkapp.R;
import dev.jokr.localnetworkapp.models.MyMessage;

/**
 * Created by JoKr on 9/3/2016.
 */
public class SessionFragment extends Fragment {


    public static final String ROLE = "role";
    public static final int SERVER = 1;
    public static final int CLIENT = 2;

    private int role;
    private RecyclerView recyclerView;
    private MessagesAdapter adapter;
    private LocalClient client;
    private LocalServer server;

    int i = 0;

    CountDownTimer countDownTimer;
    TextView textview;
    NetworkInfo wifiCheck;
    ConnectivityManager connectivityManager;
    Context context;
    WifiManager wifimanager;


    String ADDITION = "add";
    String SUBSTRACTION = "sub";
    String MULTIPLICATION = "mul";
    String DIVISION = "div";

    long startTime, endTime;
    TextView time, bitswithoutFunc, bitsWithFUnc;


    //for client
    Button addition;
    String[] OperationList = {"Addition", "Substraction", "Multiplication", "Division"};
    LinearLayout calcualtorLayout;
    Button add, sub, mul, div;
    long withFunc = 0, withoutFunc = 0;

    //for server
    SharedPreferences preferences;


    public static SessionFragment newInstance(int role) {

        Bundle args = new Bundle();
        args.putInt(ROLE, role);
        SessionFragment fragment = new SessionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        textview = (TextView) view.findViewById(R.id.textview);
        addition = (Button) view.findViewById(R.id.btnAddition);
        calcualtorLayout = (LinearLayout) view.findViewById(R.id.calculate_layout);
        add = (Button) view.findViewById(R.id.add);
        sub = (Button) view.findViewById(R.id.sub);
        mul = (Button) view.findViewById(R.id.multi);
        div = (Button) view.findViewById(R.id.div);
        context = getActivity();

        time = (TextView) view.findViewById(R.id.timeTosend);
        bitswithoutFunc = (TextView) view.findViewById(R.id.numberOfBitswithoutFunc);
        bitsWithFUnc = (TextView) view.findViewById(R.id.numberOfBitswithFUnc);

        this.role = getArguments().getInt(ROLE);
        recyclerView = (RecyclerView) view.findViewById(R.id.list_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessagesAdapter(LayoutInflater.from(getContext()));
        recyclerView.setAdapter(adapter);

        Button btnSend = (Button) view.findViewById(R.id.btn_send_message);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        Button btnStop = (Button) view.findViewById(R.id.btn_stop_session);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSession();
            }
        });

        if (role == SERVER)
            setupServer();
        else if (role == CLIENT)
            setupClient();

        return view;
    }

    private void setupClient() {
        client = new LocalClient(getContext());

        //  addition.setVisibility(View.VISIBLE);
        calcualtorLayout.setVisibility(View.VISIBLE);
        addition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateSendValues("add");
            }
        });

        bitswithoutFunc.setVisibility(View.VISIBLE);
        bitsWithFUnc.setVisibility(View.VISIBLE);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateSendValues(ADDITION);
            }
        });
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateSendValues(SUBSTRACTION);
            }
        });
        mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateSendValues(MULTIPLICATION);
            }
        });
        div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inflateSendValues(DIVISION);
            }
        });

        client.setReceiver(new LocalClient.MessageReceiver() {
            @Override
            public void onMessageReceived(Payload<?> payload) {
                if (getContext() != null) {
                    String data = payload.getPayload().toString();
                    Toast.makeText(getContext(), "" + payload.getPayload(), Toast.LENGTH_LONG).show();
                    adapter.addMessage(payload.getPayload().toString());

                    if (data.equals("Stop")) {
                        wifimanager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifimanager.setWifiEnabled(false);
                    }

                    if (data.equals("op")) {
                        new MaterialDialog.Builder(getContext())
                                .title("Choose An Operation")
                                .items(OperationList)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        String msg = "act:" + which;
                                        client.sendSessionMessage(new Payload<MyMessage>
                                                (new MyMessage(msg)));

                                        withFunc = withoutFunc+ msg.length() * 8;  //(msg.getBytes().toString().length());
                                        bitsWithFUnc.setText("Biths with Function "+ withFunc); //according to UTF unicode, each character contains 8bits

                                        dialog.dismiss();

                                    }
                                })
                                .show();
                    }

                    if (data.contains("re:")) {
                        double result = Double.parseDouble(data.split(":")[1]);

                        Toast.makeText(getActivity(), "Result is " + result, Toast.LENGTH_LONG).show();


                    }

                } else
                    Log.e("USER", "Received but context is null: " + payload.getPayload());
            }
        });
    }

    private void inflateSendValues(final String funcName) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.sendvalues, null);
        final EditText value1 = (EditText) view.findViewById(R.id.value1);
        final EditText value2 = (EditText) view.findViewById(R.id.value2);
        Button sendArgs = (Button) view.findViewById(R.id.sendArgs);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).customView(view, false);

        final MaterialDialog dialog = builder.build();
        dialog.show();



        sendArgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double val1;
                double val2;

                if (value1.getText().toString() == null) val1 = 0;
                else val1 = Double.parseDouble(value1.getText().toString());
                if (value2.getText().toString() == null) val2 = 0;
                else val2 = Double.parseDouble(value2.getText().toString());


                String msg = funcName + ":" + val1 + ":" + val2;

                withoutFunc = msg.length() * 8; //msg.getBytes().toString().length();
                bitswithoutFunc.setText( "Bits without Function"+ (msg.length() * 8)); //according to UTF unicode, each character contains 8bits

                final long startTime = System.nanoTime();

                client.sendSessionMessage(new Payload<MyMessage>(new MyMessage(msg)));

                dialog.dismiss();
                long endTime = System.nanoTime();

                long totalTime = endTime - startTime;

                time.setText("Required Time for client: " + totalTime);

            }
        });


    }

    private void setupServer() {
        addition.setVisibility(View.GONE);
        server = new LocalServer(getContext());
        preferences = getActivity().getSharedPreferences("opt", Context.MODE_PRIVATE);
        server.setReceiver(new LocalServer.OnUiEventReceiver() {
            @Override
            public void onUiEvent(Payload<?> payload) {
                Toast.makeText(getContext(), "EVENT: " + payload.getPayload(), Toast.LENGTH_LONG).show();
                adapter.addMessage(payload.getPayload().toString());
                server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("Message is Received" + i)));
                String msg = payload.getPayload().toString();
                operationOnmsg(msg);
                textview.setText("");
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = new CountDownTimerClass(60000, 1000);
                    countDownTimer.start();
                } else {
                    countDownTimer = new CountDownTimerClass(60000, 1000);
                    countDownTimer.start();
                }
            }

            @Override
            public void onClientConnected(Payload<?> payload) {
                // not interested (not in this version)
            }
        });
    }

    private void operationOnmsg(String msg) {

        String prefMsg = preferences.getString("operation", "0:0:0:0");
        String[] prefMsgList = prefMsg.split(":");

        String[] optList = msg.split(":");
        String funcName = optList[0];

        String funcPref = preferences.getString("func", "f");
        double value1Pref = Double.parseDouble(preferences.getString("x", "0"));
        double value2Pref = Double.parseDouble(preferences.getString("y", "0"));


        if (msg.contains("act")) {

            int optSl = Integer.parseInt(optList[1]);

            if (optSl == 0) {
                //  funcPref = ADDITION;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("operation", funcPref + ":" + prefMsgList[1] + ":" + prefMsgList[2] + ":" + prefMsgList[3]);
                editor.putString("func", "f");
                editor.putString("x", "0");
                editor.putString("y", "0");
                editor.apply();
                funcPref = ADDITION;
            } else if (optSl == 1) {
                //   funcPref = SUBSTRACTION;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("operation", prefMsgList[0] + ":" + funcPref + ":" + prefMsgList[2] + ":" + prefMsgList[3]);
                editor.putString("func", "f");
                editor.putString("x", "0");
                editor.putString("y", "0");
                editor.apply();
                funcPref = SUBSTRACTION;
            } else if (optSl == 2) {
                //    funcPref = MULTIPLICATION;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("operation", prefMsgList[0] + ":" + prefMsgList[1] + ":" + funcPref + ":" + prefMsgList[3]);
                editor.putString("func", "f");
                editor.putString("x", "0");
                editor.putString("y", "0");
                editor.apply();
                funcPref = MULTIPLICATION;
            } else if (optSl == 3) {
                //    funcPref = DIVISION;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("operation", prefMsgList[0] + ":" + prefMsgList[1] + ":" + prefMsgList[2] + ":" + funcPref);
                editor.putString("func", "f");
                editor.putString("x", "0");
                editor.putString("y", "0");
                editor.apply();
                funcPref = DIVISION;
            }

            double result = getResult(funcPref, value1Pref, value2Pref);
            server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("re:" + result)));

        } else {

            double val1 = Double.parseDouble(optList[1]);
            double val2 = Double.parseDouble(optList[2]);

            if (prefMsg.contains(funcName)) {
                if (prefMsg.split(":")[0].equals(funcName)) funcName = ADDITION;
                else if (prefMsg.split(":")[1].equals(funcName)) funcName = SUBSTRACTION;
                else if (prefMsg.split(":")[2].equals(funcName)) funcName = MULTIPLICATION;
                else if (prefMsg.split(":")[3].equals(funcName)) funcName = DIVISION;

                double result = getResult(funcName, val1, val2);
                server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("re:" + result)));
            } else {
                server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("op")));

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("func", funcName);
                editor.putString("x", val1 + "");
                editor.putString("y", val2 + "");
                editor.apply();

            }

        }


    }

    private double getResult(String funcName, double val1, double val2) {

        double result = 0;

        long startTime = System.nanoTime();

        if (funcName.equals(ADDITION)){
            result = val1 + val2;
            long endTime = System.nanoTime();

            long totalTime = endTime - startTime;

            time.setText("Required Time for A Mobile Device: " + totalTime);

        }
        else if (funcName.equals(SUBSTRACTION)){
            result = val1 - val2;
            long endTime = System.nanoTime();

            long totalTime = endTime - startTime;

            time.setText("Required Time for A Mobile Device: " + totalTime);

        }
        else if (funcName.equals(MULTIPLICATION)){
            result = val1 * val2;
            long endTime = System.nanoTime();

            long totalTime = endTime - startTime;

            time.setText("Required Time for A Mobile Device: " + totalTime);

        }
        else if (funcName.equals(DIVISION)) {
            result = val1 / val2;
            long endTime = System.nanoTime();

            long totalTime = endTime - startTime;

            time.setText("Required Time for A Mobile Device: " + totalTime);

        }


        return result;

    }

    private void sendMessage() {

        if (role == SERVER) {
            server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("This is something from server!" + i)));
            i++;
        } else if (role == CLIENT) {
            client.sendSessionMessage(new Payload<MyMessage>(new MyMessage("This is something from client!" + i)));
            i++;
        }
    }


    private void stopSession() {
        if (role == SERVER)
            server.shutdown();
        else if (role == CLIENT)
            client.shutdown();
    }

    public class CountDownTimerClass extends CountDownTimer {

        public CountDownTimerClass(long millisInFuture, long countDownInterval) {

            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {

            int progress = (int) (millisUntilFinished / 1000);

            textview.setText(Integer.toString(progress));

//            if (progress==20){
//                server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("Its twenty")));
//            }

        }

        @Override
        public void onFinish() {

            server.sendLocalSessionEvent(new Payload<MyMessage>(new MyMessage("Stop")));

//            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//            if (activeNetwork != null) { // connected to the internet
//                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
//                    // connected to wifi
//                    Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
//
//                } else {
//                    // not connected to the internet
//                }
//
//
//            }
        }

    }

}

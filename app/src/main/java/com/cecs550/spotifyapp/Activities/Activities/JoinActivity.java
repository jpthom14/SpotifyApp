package com.cecs550.spotifyapp.Activities.Activities;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cecs550.spotifyapp.Activities.Classes.NsdHelper;
import com.cecs550.spotifyapp.Activities.Classes.PlaylistConnection;
import com.cecs550.spotifyapp.Activities.Classes.UserProfile;
import com.cecs550.spotifyapp.R;

public class JoinActivity extends AppCompatActivity {
    private String accessToken;

    NsdHelper nsdHelper;

    private Handler handler;

    PlaylistConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("token");

        //connect to host

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
            }
        };

        Button cancelButton = (Button) findViewById(R.id.cancel_join_playlist);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                GoToMenu(accessToken);
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting.");
        connection = new PlaylistConnection(handler);

        initializeNsd();

        discoverServices();

        super.onStart();
    }
    @Override
    protected void onPause() {
        Log.d(TAG, "Pausing.");
        if (nsdHelper != null) {
            nsdHelper.stopDiscovery();
        }
        super.onPause();
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "Resuming.");
        super.onResume();
        if (nsdHelper != null) {
            nsdHelper.discoverServices();
        }
    }

    private void GoToMenu(String token)
    {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("token", token);
        startActivity(intent);
    }

    Context context;

    NsdManager manager;
    NsdManager.ResolveListener resolveListener;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.RegistrationListener registrationListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String TAG = "NsdHelper";
    public String serviceName = "grapeVinePlaylist";

    public NsdServiceInfo nsdServiceInfo;

    public void initializeNsd() {
        context = this;
        manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                System.out.println(errorCode);
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                if (serviceInfo.getServiceName().equals(serviceName)) {
                    return;
                }
                nsdServiceInfo = serviceInfo;
                Log.d(TAG, "Connecting.");
                connection.connectToServer(serviceInfo.getHost(), serviceInfo.getPort());

                UserProfile prof = new UserProfile(accessToken);
                connection.sendProfile(prof);

            }
        };
    }

    public void initializeDiscoveryListener() {
        System.out.println("Initializing discovery listener...");
        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                System.out.println("Service Found?");
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same machine: " + serviceName);
                } else if (service.getServiceName().contains(serviceName)) {
                    manager.resolveService(service, resolveListener);
                    Log.d(TAG, "service resolved?");


                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (nsdServiceInfo == service) {
                    nsdServiceInfo = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                serviceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Service registered: " + serviceName);
            }
            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
                Log.d(TAG, "Service registration failed: " + arg1);
            }
            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.d(TAG, "Service unregistered: " + arg0.getServiceName());
            }
            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.d(TAG, "Service unregistration failed: " + errorCode);
            }
        };
    }

    public void registerService(int port) {
        tearDown();  // Cancel any previous registration request
        initializeRegistrationListener();
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        manager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public void discoverServices() {
        System.out.println("discoverServices()");
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        manager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        System.out.println("Hi");
    }
    public void stopDiscovery() {
        if (discoveryListener != null) {
            try {
                manager.stopServiceDiscovery(discoveryListener);
            } finally {
            }
            discoveryListener = null;
        }
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return nsdServiceInfo;
    }
    public void tearDown() {
        if (registrationListener != null) {
            try {
                manager.unregisterService(registrationListener);
            } finally {
            }
            registrationListener = null;
        }
    }
}

package com.cecs550.spotifyapp.Activities.Classes;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.nfc.Tag;
import android.util.Log;

/**
 * Created by braxt on 4/4/2017.
 */

public class NsdHelper {

    Context context;

    NsdManager manager;
    NsdManager.ResolveListener resolveListener;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.RegistrationListener registrationListener;

    public static final String SERVICE_TYPE = "ipp._tcp.";
    public static final String TAG = "NsdHelper";
    public String serviceName = "grapeVinePlaylist";

    public NsdServiceInfo nsdServiceInfo;

    public NsdHelper(Context context) {
        this.context = context;
        manager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public void initializeNsd() {
        resolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                System.out.println(errorCode);
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                nsdServiceInfo = serviceInfo;
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

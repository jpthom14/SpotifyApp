package com.cecs550.spotifyapp.Activities.Classes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by braxt on 3/29/2017.
 */

public class PlaylistConnection {

    private Socket socket;
    private int localPort = -1;

    private static final String TAG = "PlaylistConnection";
    private PlaylistClient playlistClient;
    private PlaylistServer playlistServer;
    private Handler updateHandler;

    public PlaylistConnection(Handler handler) {
        updateHandler = handler;
        playlistServer = new PlaylistServer();
    }

    public void tearDown() {
        playlistServer.tearDown();
        if(playlistClient != null) {
            playlistClient.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        playlistClient = new PlaylistClient(address, port);
    }

    public void sendProfile (UserProfile profile) {
        if (playlistClient != null) {
            playlistClient.sendProfile(profile);
        }
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int port) {
        localPort = port;
    }

    public synchronized void updateProfiles(UserProfile prof, boolean local) {
        Message message = new Message();
        message.obj = prof;
        updateHandler.sendMessage(message);
    }

    private synchronized void setSocket(Socket socket) {
        if (socket != null) {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.socket = socket;
    }

    private Socket getSocket() {
        return socket;
    }

    private class PlaylistServer {
        ServerSocket serverSocket = null;
        Thread thread = null;

        public PlaylistServer() {
            thread = new Thread(new ServerThread());
            thread.start();
        }

        public void tearDown() {
            thread.interrupt();
            try {
                serverSocket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    serverSocket = new ServerSocket(0);
                    setLocalPort(serverSocket.getLocalPort());

                    while(!Thread.currentThread().isInterrupted()) {
                        setSocket(serverSocket.accept());
                        if(playlistClient == null) {
                            int port = socket.getPort();
                            InetAddress address = socket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PlaylistClient {
        private InetAddress address;
        private int port;

        private final String CLIENT_TAG = "PlaylistClient";
        private Thread mSendThread;
        private Thread mRecThread;

        public PlaylistClient(InetAddress address, int port) {
            Log.d(CLIENT_TAG, "Creating playlistClient");
            this.address = address;
            this.port = port;
            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {
            BlockingQueue<UserProfile> messageQueue;
            private int QUEUE_CAPACITY = 10;
            public SendingThread() {
                messageQueue = new ArrayBlockingQueue<UserProfile>(QUEUE_CAPACITY);
            }
            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(address, port));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }
                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();
                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }
                while (true) {
                    try {
                        UserProfile prof = messageQueue.take();
                        sendProfile(prof);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }
        class ReceivingThread implements Runnable {
            @Override
            public void run() {
                ObjectInputStream input = null;
                try {
                    input = new ObjectInputStream(socket.getInputStream());
                    while (!Thread.currentThread().isInterrupted()) {
                        UserProfile recievedProfile = null;
                        recievedProfile = (UserProfile) input.readObject();
                        if (recievedProfile != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + recievedProfile.toString());
                            updateProfiles(recievedProfile, false);
                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();
                } catch (Exception e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }
        }
        public void tearDown() {
            try {
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }
        public void sendProfile(UserProfile profile) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(getSocket().getOutputStream())), true);
                out.println(profile);
                out.flush();
                updateProfiles(profile, true);
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent profile: " + profile);
        }
    }
}

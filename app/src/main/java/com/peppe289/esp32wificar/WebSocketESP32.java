package com.peppe289.esp32wificar;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Random;

public class WebSocketESP32 {
    private int steering = 0;
    private int[] forword;
    private String URL = "ws://192.168.4.1:81";

    public OnPingCallBack callback;

    HashMap<Integer, Long> timeRegister;

    WebSocketESP32() {
        timeRegister = new HashMap<>();
    }

    public void changeSteering(int sg) {
        if (sg != steering && (sg == -1 || sg == 0 || sg == 1)) {
            steering = sg;
            //Log.d("STEERING", "Nuovo valore: " + steering);
        }
    }

    public void changeForword(int direction, int speed) {

        if (forword[0] != direction && (direction == -1 || direction == 1 || direction == 0)) {
            forword[0] = direction;
            //Log.d("FORWORD", "Nuovo valore: " + forword[0]);
        }

        if (speed == forword[1]) return;

        forword[1] = Math.min(speed, 255);
    }

    public void connectionRunner() throws URISyntaxException {
        URI uri = new URI(URL);
        forword = new int[2];
        forword[0] = 0;
        forword[1] = 0;


        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Connessione aperta!");
            }

            @Override
            public void onMessage(String message) {
                int key;
                float temperature;
                System.out.println("Messaggio ricevuto: " + message);
                long now = System.currentTimeMillis();

                String[] part = message.split("\\|");
                key = Integer.parseInt(part[0]);
                temperature = Float.parseFloat(part[1]);

                System.out.println("key: " + key + " temp: " + temperature);

                try {
                    long reqTime = timeRegister.get(key);
                    //Log.d("PING", "Request Ping: " + (now - reqTime) + " ms");
                    timeRegister.remove(key);
                    callback.update(now - reqTime, temperature);
                } catch (NullPointerException ignored) {}
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d("WebSocket", "Chiusa: code=" + code + " reason=" + reason + " remote=" + remote);
                callback.update(-1, -1);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        new Thread(() -> {
            try {
                client.connectBlocking();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                boolean canSendSteering = true;
                long lastRequestDate = 0;

                while (true) {
                    if (client.isOpen()) {

                        long timeNow = System.currentTimeMillis();
                        if (lastRequestDate == 0 || (timeNow - lastRequestDate) > 500) {
                            lastRequestDate = timeNow;
                            Random rd = new Random();
                            int code = rd.nextInt(255);
                            timeRegister.put(code, timeNow);
                            client.send("P," + code);
                        }

                        // send steering signal only if first 0 or if isn't 0
                        if (steering != 0 || canSendSteering) {
                            String motorA = "A," + ((steering == 0) ? "0," : "240,") + steering;
                            client.send(motorA);
                            canSendSteering = true;
                        }

                        if (steering == 0)
                            canSendSteering = false;

                        String motorB = "B," + forword[1] + "," + forword[0];
                        client.send(motorB);

                        //Log.d("SEND", motorA);
                        //Log.d("SEND", motorB);
                    }
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public interface OnPingCallBack {
        public void update(long ms, float temp);
    }
}

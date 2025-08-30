package com.peppe289.esp32wificar;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tv = findViewById(R.id.pingText);
        TextView termalView = findViewById(R.id.termalText);
        Button left = findViewById(R.id.button);
        Button right = findViewById(R.id.button2);
        WebSocketESP32 webSocketESP32 = new WebSocketESP32();
        try {
            webSocketESP32.connectionRunner();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        webSocketESP32.callback = new WebSocketESP32.OnPingCallBack() {
            @Override
            public void update(long ms, float temp) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(String.format("%d ms", ms));
                        termalView.setText(temp + "°C");
                    }
                });
            }
        };

        left.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Quando premi il pulsante
                        Log.d("Button", "Premuto");
                        webSocketESP32.changeSteering(1);
                        return true; // consuma l'evento

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Quando rilasci il pulsante
                        Log.d("Button", "Rilasciato");
                        webSocketESP32.changeSteering(0);
                        return true;
                }
                return false;
            }
        });

        right.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Quando premi il pulsante
                        Log.d("Button", "Premuto");
                        webSocketESP32.changeSteering(-1);
                        return true; // consuma l'evento

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Quando rilasci il pulsante
                        Log.d("Button", "Rilasciato");
                        webSocketESP32.changeSteering(0);
                        return true;
                }
                return false;
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(200);
        seekBar.setProgress(100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int direction = Integer.compare(i, 100);
                if (direction == 0)
                    webSocketESP32.changeForword(0, 0);
                else {
                    int value = i - 100;
                    if (value < 0) value = -value;

                    int basePower = 130;
                    int maxPower = 235;
                    int normalized = (int) Math.round(basePower + (maxPower - basePower) * Math.pow(value / 100.0, 2.0));
                    webSocketESP32.changeForword(-direction, normalized);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setProgress(seekBar.getMax() / 2);
            }
        });
    }
}
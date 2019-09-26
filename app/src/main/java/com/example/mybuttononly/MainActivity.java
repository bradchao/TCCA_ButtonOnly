package com.example.mybuttononly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;

public class MainActivity extends AppCompatActivity {
    private Gpio gpioButton;
    private ButtonInputDriver btnDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManager peripheralManager = PeripheralManager.getInstance();

        try{
            btnDriver = new ButtonInputDriver("BCM4",
                    Button.LogicState.PRESSED_WHEN_LOW,
                    KeyEvent.KEYCODE_SPACE);
            btnDriver.register();

        }catch (Exception e){
            Log.v("brad", e.toString());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.v("brad", "KeyDown");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.v("brad", "KeyUp");
        return super.onKeyUp(keyCode, event);
    }
}

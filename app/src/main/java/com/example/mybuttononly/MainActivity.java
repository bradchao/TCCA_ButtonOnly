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
    private Gpio gpioLed;
    private ButtonInputDriver btnDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PeripheralManager peripheralManager = PeripheralManager.getInstance();

        try{
            gpioLed = peripheralManager.openGpio("BCM4");
            gpioLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpioLed.setValue(false);

            btnDriver = new ButtonInputDriver("BCM17",
                    Button.LogicState.PRESSED_WHEN_HIGH,
                    KeyEvent.KEYCODE_SPACE);
            btnDriver.register();

        }catch (Exception e){
            Log.v("brad", e.toString());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            Log.v("brad", "KeyDown");
            try {
                gpioLed.setValue(true);
            }catch (Exception e){
                Log.v("brad", e.toString());
            }
        }
        return true; //super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            Log.v("brad", "KeyUp");
            try {
                gpioLed.setValue(false);
            }catch (Exception e){
                Log.v("brad", e.toString());
            }
        }
        return true; //super.onKeyUp(keyCode, event);
    }
}

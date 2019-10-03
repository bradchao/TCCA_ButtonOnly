package com.example.mybuttononly;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.contrib.driver.button.ButtonInputDriver;
import com.google.android.things.device.TimeManager;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private Gpio gpioLed;
    private ButtonInputDriver btnDriver;

    private File sdroot;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

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

        sdroot = Environment.getExternalStorageDirectory();
        saveFile();

        //readUSB();
        setupTime();
        getID();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("myled");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.v("brad", "Value is: " + value);

                try {
                    if (value.equals("true")) {
                        gpioLed.setValue(true);
                    } else {
                        gpioLed.setValue(false);
                    }
                }catch (IOException e){
                    Log.v("brad", e.toString());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.v("brad", "Failed to read value.", error.toException());
            }
        });

        initBle();
    }

    private void readUSB(){
        File usb = new File("/mnt/usb");
        File[] files = usb.listFiles();
        if (files != null) {
            for (File file : files) {
                Log.v("brad", file.getAbsolutePath());
            }
        }else{
            Log.v("brad", "üsb null");
        }
    }

    private void setupTime(){
        TimeManager timeManager = TimeManager.getInstance();

        timeManager.setTimeFormat(TimeManager.FORMAT_24);
        timeManager.setTimeZone("Asia/Taipei");

        Calendar now = Calendar.getInstance();
        int yy = now.get(Calendar.YEAR);
        int mm = now.get(Calendar.MONTH)+1;
        int dd = now.get(Calendar.DAY_OF_MONTH);
        int hh = now.get(Calendar.HOUR_OF_DAY);
        int ii = now.get(Calendar.MINUTE);

        Log.v("brad", yy+"-"+mm+"-"+dd+" " + hh + ":"+ ii);

    }

    {


    }


    private void getID(){
        TelephonyManager tm =
                (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        //String mid = tm.getManufacturerCode();
        String did = tm.getDeviceId();
        Log.v("brad", "did = " + did);
        Log.v("brad", "sno = " + Build.SERIAL);

        Log.v("brad", "android_id = " +
                Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID));

        WifiManager wifiManager =
                (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String mac = wifiInfo.getMacAddress();
        Log.v("brad", "nac = " + mac);


    }

    private void saveFile(){
        File file = new File(sdroot, "mytest.txt");
        int rand = (int)(Math.random()*49+1);
        Log.v("brad", "rand = " + rand);
        try(FileOutputStream fout = new FileOutputStream(file)){
            fout.write(("Hello:"+rand).getBytes());
            fout.flush();
            Log.v("brad", "save ok");
        }catch (Exception e){
            Log.v("brad", e.toString());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            Log.v("brad", "KeyUp");
            try {
                gpioLed.setValue(!gpioLed.getValue());

                if (gpioLed.getValue()){
                    myRef.setValue("true");

                }else{
                    myRef.setValue("false");
                }


            }catch (Exception e){
                Log.v("brad", e.toString());
            }
        }
        readFile();
        return true; //super.onKeyUp(keyCode, event);
    }

    private void readFile(){
        File file = new File(sdroot, "mytest.txt");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(file)))){
            String line = br.readLine();
            Log.v("brad", line);
        }catch (Exception e){
            Log.v("brad", e.toString());
        }
    }

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer bluetoothGattServer;
    private Set<BluetoothDevice> devices = new HashSet<>();

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.STATE_OFF);
            if (state == BluetoothAdapter.STATE_ON){
                Log.v("brad", "ble on");

            }else if (state == BluetoothAdapter.STATE_OFF){
                Log.v("brad", "ble off");
            }
        }
    };

    private void initBle(){
        bluetoothManager  = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        IntentFilter filter = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, filter);

        if (!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }else{

        }

    }

    


}

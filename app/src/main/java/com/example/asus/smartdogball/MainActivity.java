package com.example.asus.smartdogball;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    MediaPlayer mp;
    Context context = this;
    BluetoothAdapter BTAdapter;
    public static int REQUEST_BLUETOOTH = 1;
    ArrayList<BluetoothDevice> deviceItemList;
    ArrayList<String> mDeviceList = new ArrayList<String>();
    ListView list;
    int index;
    boolean duplicate;
    int liveSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playBeep = (Button) this.findViewById(R.id.beep_detection);
        Button bluetoothButton = (Button) this.findViewById(R.id.bluetoothButton);
        TextView state = (TextView) this.findViewById(R.id.state);
        TextView atributes = (TextView) this.findViewById(R.id.atributes);
        final ListView list = (ListView) this.findViewById(R.id.list);

        //BLUETOOTH

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // If bluetooth is disabled, than open dialog to enable it.
        if (!BTAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        // Generate list and put in already paired devices.
        deviceItemList = new ArrayList<BluetoothDevice>();
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mDeviceList.add(device.getName() + "\n" + device.getAddress());
            }
        }

        // Set adapter of the list to listview and set text color to WHITE.
        list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.WHITE);

                // Generate ListView Item using TextView
                return view;
            }
        });

        // On button click add nearby found bluetooth devices to list.
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Refresh list
                list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent){
                        // Get the Item from ListView
                        View view = super.getView(position, convertView, parent);

                        // Initialize a TextView for ListView each Item
                        TextView tv = (TextView) view.findViewById(android.R.id.text1);

                        // Set the text color of TextView (ListView Item)
                        tv.setTextColor(Color.WHITE);

                        // Generate ListView Item using TextView
                        return view;
                    }
                });

                // check for new bluetooth device
                if(BTAdapter.isDiscovering()) {
                    Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
                    BTAdapter.cancelDiscovery();

                    checkBTPermissions();

                    BTAdapter.startDiscovery();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(bReciever, filter);
                }
                if(!BTAdapter.isDiscovering()) {
                    Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
                    checkBTPermissions();

                    BTAdapter.startDiscovery();
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(bReciever, filter);
                }


            }
        });

        //PREDVAJANJE ZVOKA

        mp = MediaPlayer.create(context, R.raw.beep);
        playBeep.setOnClickListener(new View.OnClickListener() {
            @Override
            //zaenkrat sem samo testiral kako bi zvok delal... nekak na tak način naj bi... slabsi kot je signal večji DELAY je med piskom (beep7.mp3 ma najvecji DELAY, bepp.mp3 pa najmanjsega)
            public void onClick(View view) {
                if(liveSignal >= -50) {
                    mp = MediaPlayer.create(context, R.raw.beep);
                } else if (liveSignal < -50 && liveSignal >= -57) {
                    mp = MediaPlayer.create(context, R.raw.beep);
                } else if (liveSignal < -58 && liveSignal >= -63) {
                    mp = MediaPlayer.create(context, R.raw.beep1);
                } else if (liveSignal < -64 && liveSignal >= -69) {
                    mp = MediaPlayer.create(context, R.raw.beep2);
                } else if (liveSignal < -70 && liveSignal >= -75) {
                    mp = MediaPlayer.create(context, R.raw.beep3);
                } else if (liveSignal < -76 && liveSignal >= -81) {
                    mp = MediaPlayer.create(context, R.raw.beep4);
                } else if (liveSignal < -82 && liveSignal >= -87) {
                    mp = MediaPlayer.create(context, R.raw.beep5);
                } else if (liveSignal < -88 && liveSignal >= -93) {
                    mp = MediaPlayer.create(context, R.raw.beep6);
                } else if (liveSignal < -93) {
                    mp = MediaPlayer.create(context, R.raw.beep7);
                }
                try {
                    if (mp.isPlaying()) {
                        mp.stop();
                        mp.release();
                        mp = MediaPlayer.create(context, R.raw.beep);
                    } else {
                        mp.start();
                        mp.setLooping(true);
                    }
                } catch(Exception e) { e.printStackTrace(); }
            }
        });
    }

    // To sem nek prebral da mora bit ta permission ce je verzija Androida večjo od LOLLPOP drugač bluetooth ne najde nove naprave.
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    // To pa je sprogramiran receiver za novo bluetooth napravo
    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            index = -1;
            duplicate = false;
            Log.d(TAG, "onReceive: ACTION FOUND.");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                //Tukej sem signal shranu v liveSignal da ga kasnej v zvoku uporabim (to getName equal Bluno je samo začasno).
                if(device.getName().equals("Bluno")) {
                    liveSignal = rssi;
                }
                // Create a new device item
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

                //Kle preverm če je ta MAC že v seznamu naprav in če je ga izbrišm in posodobim z novim (zato da se signal updejta)
                for(String dev : mDeviceList) {
                    index++;
                    if (dev.indexOf(device.getAddress()) != -1) {
                        duplicate = true;
                        mDeviceList.remove(index);
                        mDeviceList.add(device.getName() + "\n" + device.getAddress() + "\n" + rssi);
                    }
                }
                if (!duplicate) {
                    mDeviceList.add(device.getName() + "\n" + device.getAddress() + "\n" + rssi);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(bReciever);
    }
}

package com.spikytech.autosqa;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    LinearLayout AppName;
    TextView Headline, SampleResultHeading, SolutionResultHeading, Solution, pHLevel, Temperature, Moisture, Nutrition, LandArea;
    ImageView AddDevice;
    Button RunButton;
    int count=0;
    public String TAG = "-----------";

    private String macAddress = "";
    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    final int ThreadPassValue = 1;
    public static StringBuilder sb;
    ConnectedThread mConnectedThread;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothSocket bluetoothSocket;
    OutputStream outputStream;
    Handler handler;
    @SuppressLint({"Recycle", "UseCompatLoadingForDrawables", "SetTextI18n", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppName = findViewById(R.id.AppName);
        Headline = findViewById(R.id.Headline);
        ImageView gifImageView = findViewById(R.id.gifImageView);

        SampleResultHeading = findViewById(R.id.SampleResultHeading);
        SolutionResultHeading = findViewById(R.id.SolutionResultHeading);
        Solution = findViewById(R.id.Solution);
        pHLevel = findViewById(R.id.pHLevel);
        Temperature = findViewById(R.id.Temperature);
        Moisture = findViewById(R.id.Moisture);
        Nutrition = findViewById(R.id.Nutrition);
        AddDevice = findViewById(R.id.AddDevice);
        LandArea = findViewById(R.id.LandArea);

        // If a bluetooth device has been selected from SelectDeviceActivity
        String deviceName = getIntent().getStringExtra("deviceName");
        Toast.makeText(this, "deviceName- " + deviceName, Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String deviceAddress = getIntent().getStringExtra("deviceAddress");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {

            Log.d("Bluetooth", "Name->" + device.getName() + "    " + "MAC->" + device.getAddress());
            if (device.getName().equals("HC-05")) {
                macAddress = device.getAddress();
                Log.d("Bluetooth", "MAC - > " + macAddress);
            }
        }
        if (Objects.equals(macAddress, "")) {
            Toast.makeText(this, "HC-05 Not Found!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "HC-05 Found!", Toast.LENGTH_SHORT).show();

        }


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);

        // Connecting to bluetooth device in a separate thread;

        new Thread(() -> {


            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                mConnectedThread = new ConnectedThread(bluetoothSocket);
                mConnectedThread.start();
                Log.d("Message", "Connected to HC-06");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Bluetooth successfully connected", Toast.LENGTH_LONG).show());

            } catch (IOException e) {
                Log.d("Message", "Turn on bluetooth and restart the app");
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Turn on bluetooth and restart the app", Toast.LENGTH_SHORT).show());


            }

        }).start();
        handler = new Handler(){
            public void handleMessage(Message msg) {
                if (msg.what == ThreadPassValue) {
                    byte[] readBuf = (byte[]) msg.obj;
                    sb = new StringBuilder();
                    String strIncom = new String(readBuf, 0, msg.arg1);
                    sb.append(strIncom);
                    Log.d("Data","here 1");
                    int endOfLineIndex = sb.indexOf("\r\n");
                    if (endOfLineIndex > 0) {
                        Log.d("Data","here 1");
                        String sbprint = sb.substring(0, endOfLineIndex);
                        Log.e("ReceivedData", sbprint);
                        sb.delete(0, sb.length());
                        if(sbprint.contains("p") && sbprint.contains("m") && sbprint.contains("t")){
                            pHLevel.setText("Instantaneous pH level- 6.7");
                            String phText = sbprint;
                            Log.e("ReceivedData", sbprint);
                            phText = phText.substring(0,phText.indexOf("m"));

                            pHLevel.setText(phText.replace("p","Instantaneous pH level- "));
                            String MoistureText =  sbprint.substring(sbprint.indexOf("m")+1,sbprint.indexOf("t"));
                            Moisture.setText("Moisture - "+MoistureText+"%");
                            String temperatureText = sbprint.substring(sbprint.indexOf("t"));
                            Temperature.setText(temperatureText.replace("t","Temperature - "));

                        }

                    }
                    //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                }
            }};
        AddDevice.setOnClickListener(view -> {
            ObjectAnimator bounceAnimText1X = ObjectAnimator.ofFloat(AddDevice, View.SCALE_X, 1f, 1.1f, 1f);
            ObjectAnimator bounceAnimText1Y = ObjectAnimator.ofFloat(AddDevice, View.SCALE_Y, 1f, 1.1f, 1f);
            AnimatorSet animationSet = new AnimatorSet();
            animationSet.playTogether(bounceAnimText1X, bounceAnimText1Y);
            animationSet.start();
            Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
            startActivity(intent);
        });
        RunButton = findViewById(R.id.Run);
        Button MoistureButton = findViewById(R.id.MoistureTest);
        MoistureButton.setOnClickListener(view -> {
            String command;
            command = "M";
            try {
                outputStream.write(command.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("Command- >", command);
        });
        Button Mixture = findViewById(R.id.SoilMixture);
        Mixture.setOnClickListener(view -> {
            String command;
            command = "S";
            try {
                outputStream.write(command.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("Command- >", command);
        });
        RunButton.setOnClickListener(view -> {
//            Solution.setText("     Vinegar amount for making more acidic: Nan \n\n     Best crop for growing: Nan \n\n     Possible Yield(current values): Nan \n\n      Possible Yield(Problems Solved): Nan\n\n      Soil Degradation Level: Nan ");
            String command;
            if(count==0){
                command = "go!";
                count++;
            }else{
                command = "Y";
            }
            try {
                outputStream.write(command.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("Command- >", command);

        });
        // Create ObjectAnimator for bounce animation (scaleX and scaleY)
        ObjectAnimator bounceAnimText1X = ObjectAnimator.ofFloat(AppName, View.SCALE_X, 0.2f, 1.2f, 1f);
        ObjectAnimator bounceAnimText1Y = ObjectAnimator.ofFloat(AppName, View.SCALE_Y, 0.2f, 1.2f, 1f);

        ObjectAnimator bounceAnimText2X = ObjectAnimator.ofFloat(Headline, View.SCALE_X, 0.2f, 1.2f, 1f);
        ObjectAnimator bounceAnimText2Y = ObjectAnimator.ofFloat(Headline, View.SCALE_Y, 0.2f, 1.2f, 1f);

        // Set the duration for each bounce animation
        long duration = 1000;

        bounceAnimText1X.setDuration(duration);
        bounceAnimText1Y.setDuration(duration);

        bounceAnimText2X.setDuration(duration);
        bounceAnimText2Y.setDuration(duration);

        // Create AnimatorSets for AppName and Headline
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(bounceAnimText1X, bounceAnimText1Y);
        animationSet.start();

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                                    AnimatorSet animationSet = new AnimatorSet();
                                    animationSet.playTogether(bounceAnimText2X, bounceAnimText2Y);
                                    animationSet.start();
                                }

                        );

                    }
                },
                70
        );
        // Start the animations
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                                    Animation moveToTop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.topmotion);
                                    moveToTop.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            AppName.clearAnimation();
                                            AppName.setY(40f);
                                            AppName.setScaleX(0.6f);
                                            AppName.setScaleY(0.6f);
                                            SampleResultHeading.setVisibility(View.VISIBLE);
                                            Solution.setVisibility(View.VISIBLE);
                                            SolutionResultHeading.setVisibility(View.VISIBLE);
                                            Nutrition.setVisibility(View.VISIBLE);
                                            Temperature.setVisibility(View.VISIBLE);
                                            pHLevel.setVisibility(View.VISIBLE);
                                            AddDevice.setVisibility(View.VISIBLE);
                                            Moisture.setVisibility(View.VISIBLE);
                                            RunButton.setVisibility(View.VISIBLE);
                                            Mixture.setVisibility(View.VISIBLE);
                                            MoistureButton.setVisibility(View.VISIBLE);
                                            LandArea.setVisibility(View.VISIBLE);
                                            gifImageView.setVisibility(View.VISIBLE);

                                            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                            SampleResultHeading.startAnimation(fadeIn);
                                            Solution.startAnimation(fadeIn);
                                            SolutionResultHeading.startAnimation(fadeIn);

                                            Nutrition.startAnimation(fadeIn);
                                            pHLevel.startAnimation(fadeIn);
                                            AddDevice.startAnimation(fadeIn);
                                            Moisture.startAnimation(fadeIn);
                                            Temperature.startAnimation(fadeIn);
                                            RunButton.startAnimation(fadeIn);
                                            Mixture.startAnimation(fadeIn);
                                            MoistureButton.startAnimation(fadeIn);
                                            LandArea.startAnimation(fadeIn);
                                            gifImageView.startAnimation(fadeIn);

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {

                                        }
                                    });
                                    AppName.startAnimation(moveToTop);

                                    Headline.setVisibility(View.GONE);
                                }

                        );

                    }
                },
                2000
        );


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
                Log.d(TAG, "Connection closed");
            } catch (IOException e) {
                Log.d(TAG, "Error while closing the connection");
            }
        }
    }
    private class ConnectedThread extends Thread {
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    handler.obtainMessage(ThreadPassValue, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                    handler.handleMessage(new Message());
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }
    }
    
}
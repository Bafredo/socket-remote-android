package com.example.clientv2;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements Joystickview.JoystickListener {
    public Button connect,disconnect;
    public Thread con = null;
    public ImageView player;
    public int w,h;
    public EditText ip,port;
    public TextView tv;
    public String SERVER_IP = "127.0.0.1";
    public int PORT = 8080;
    public Joystickview rs;
    public Joystickview ls;
    public LinearLayout cb,cp;
    boolean SocketisAlive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        player = findViewById(R.id.player);
        connect = findViewById(R.id.conbtn);
        disconnect = findViewById(R.id.disconbtn);
        ip = findViewById(R.id.etip);
        port = findViewById(R.id.etport);
        tv = findViewById(R.id.tv);
        ls = findViewById(R.id.left);
        rs = findViewById(R.id.right);
        cb = findViewById(R.id.connectorBox);
        cp = findViewById(R.id.cp);



        connect.setOnClickListener(v -> {
            SERVER_IP = ip.getText().toString().trim();
            PORT = Integer.parseInt(port.getText().toString().trim());
            con = new Thread(new connect());
            con.start();
        });
    }

    PrintWriter out;
    InputStream in;



    class connect implements Runnable{
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            Socket socket;
            runOnUiThread(() -> tv.setText("connecting ...."));

            try {
                socket = new Socket(SERVER_IP,PORT);
                SocketisAlive = true;
                out = new PrintWriter(socket.getOutputStream());
                in = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        in = socket.getInputStream();
                        return 0;
                    }
                };
                runOnUiThread(() -> {
                    tv.setText("connected");
                    cb.setVisibility(View.GONE);
                    cp.setVisibility(View.VISIBLE);
                    connect.setVisibility(View.GONE);
                    disconnect.setVisibility(View.VISIBLE);
                disconnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            socket.close();
                            tv.setText("Disconnected");
                            cb.setVisibility(View.VISIBLE);
                            connect.setVisibility(View.VISIBLE);
                            cp.setVisibility(View.GONE);
                        } catch (IOException e) {
                            runOnUiThread(() -> tv.setText(e.getLocalizedMessage()));
                        }
                    }
                });
                });
            } catch (IOException e) {
                runOnUiThread(() -> tv.setText(e.getLocalizedMessage()));
            }

        }
    }
    public float Rxc,Ryc,Lxc,Lyc;
    @Override
    public void onJoystickMoved(float x, float y, int id) {
        System.out.println("Id "+id+"  X  "+ x+"  Y "+ y);
        if(SocketisAlive){
            Thread message = new Thread(new comm());
            message.start();
        }

    }
    class Player implements Runnable{

        @Override
        public void run() {

        }
    }
    class comm implements Runnable{

        @Override
        public void run() {
            String mess = "RX:"+Rxc+" RY:"+Ryc+"LX:"+Lxc+" LY:"+Lyc;
            out.write(mess);
            out.flush();
        }
    }
    class img implements Runnable{
        @Override
        public void run() {
            int totalPixels = w * h;
            byte[] rgbBytes = new byte[totalPixels * 3]; // 3 bytes per pixel (R, G, B)

            // Read RGB bytes from InputStream into the byte array
            try {
                int bytesRead = in.read(rgbBytes);
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(e.getLocalizedMessage());
                    }
                });
            }

            // If bytesRead is less than expected, it indicates an issue with the input stream.
//            if (bytesRead < totalPixels * 3) {
//                throw new IOException("Not enough data in InputStream");
//            }

            // Create an array to hold ARGB pixels (4 bytes per pixel: A, R, G, B)
            int[] argbPixels = new int[totalPixels];

            // Convert RGB bytes to ARGB_8888 format (A=255 for full opacity)
            for (int i = 0; i < totalPixels; i++) {
                int r = rgbBytes[i * 3] & 0xFF;     // Red component
                int g = rgbBytes[i * 3 + 1] & 0xFF; // Green component
                int b = rgbBytes[i * 3 + 2] & 0xFF; // Blue component

                // Pack the pixel into ARGB_8888 format
                // Alpha = 255 (fully opaque), then Red, Green, Blue
                argbPixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }

            // Create a Bitmap with the desired width and height, and ARGB_8888 format
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(argbPixels, 0, w, 0, 0, w, h);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    player.setImageBitmap(bitmap);
                }
            });
        }
    }


}
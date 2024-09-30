package com.example.clientv2;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements RightJoystick.JoystickListener,LeftJoystick.JoystickListener {
    public Button connect,disconnect;
    public Thread con = null;
    public ImageView player;
    public int w = 300,h = 200;
    public EditText ip,port;
    public TextView tv;
    public String SERVER_IP = "127.0.0.1";
    public int PORT = 8080;
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
    public float Rxc,Ryc,Lxc,Lyc;

    @Override
    public void onLeftJoystickMoved(float x, float y, int source) {
        Lxc = x;
        Lyc = y;
        new Thread(new comm()).start();
    }

    @Override
    public void onRightJoystickMoved(float x, float y, int source) {
        Rxc = x;
        Ryc = y;
        new Thread(new comm()).start();
    }

    class comm implements Runnable{

        @Override
        public void run() {
            String mess = "RX:"+Rxc+" RY:"+Ryc+"LX:"+Lxc+" LY:"+Lyc;
            out.write(mess);
            out.flush();
        }
    }

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
                Thread pic = new Thread(new VideoReceiver());
                pic.start();
            } catch (IOException e) {
                runOnUiThread(() -> tv.setText(e.getLocalizedMessage()));
            }

        }
    }



    private class VideoReceiver implements Runnable {
        @Override
        public void run() {
            try {
//                Socket socket = new Socket(SERVER_IP, PORT);

                byte[] sizeBuffer = new byte[4];

                while (in.read(sizeBuffer) != -1) {
                    // Read the size of the incoming frame

                    int frameSize = ByteBuffer.wrap(sizeBuffer).getInt();
                    System.out.println("Received image of size "+ frameSize);

                    // Read the frame data
                    byte[] frameData = new byte[frameSize];
//                    int bytesRead = 0;
//                    while (bytesRead < frameSize) {
//                        bytesRead += inputStream.read(frameData, bytesRead, frameSize - bytesRead);
//                    }
//                    in.read(frameData);
                    int bytesRead = 0;
                    while(bytesRead < frameSize){
                        int r = in.read(frameData,bytesRead,frameSize-bytesRead);
                        if(r==-1)break;
                        bytesRead += r;
                    }

                    // Decode the JPEG frame into a bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(frameData, 0, frameSize);
                    runOnUiThread(() -> player.setImageBitmap(bitmap)); // Update the ImageView
                }


            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(e.getLocalizedMessage());
                    }
                });
            }
        }
    }
}
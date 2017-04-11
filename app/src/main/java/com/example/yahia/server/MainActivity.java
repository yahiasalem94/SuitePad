package com.example.yahia.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

public class MainActivity extends Service {

    private String u = null;
    String result ="";
    public boolean stop = false;
    ServerSocket httpServerSocket;
    public static Runnable runnable = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
      u = "http://"+"127.0.0.1"+":8000/";

        runnable = new Runnable() {
            public void run() {
                Log.e("ip", "127.0.0.1"+HttpServerThread.HttpServerPORT);
                HttpServerThread httpServerThread = new HttpServerThread();
                httpServerThread.start();
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);

        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }

    private class HttpServerThread extends Thread {

        static final int HttpServerPORT = 8888;

        @Override
        public void run() {
            Socket socket = null;

            try {
                httpServerSocket = new ServerSocket(HttpServerPORT);

                while(true){
                    socket = httpServerSocket.accept();

                    HttpResponseThread httpResponseThread = new HttpResponseThread(socket);
                    httpResponseThread.start();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


    }



    private class HttpResponseThread extends Thread {

        Socket socket;

        HttpResponseThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader is;
            PrintWriter os;
            String request;


            try {
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                request = is.readLine();
                String [] x = request.split("/");
                Log.d("comparing", x[0] + " " + "GET");

                getDataSource getdatasource = new getDataSource();
                getdatasource.run();

                os = new PrintWriter(socket.getOutputStream(), true);
                os.print("HTTP/1.0 200" + "\r\n");
                os.print("Content type: text/html" + "\r\n");
                os.print("Content length: " + "\r\n");
                os.print("\r\n");
                os.print(result + "\r\n");
                os.flush();
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }
    }

    public HttpURLConnection setupConnection(URL url, String method){
        HttpURLConnection httpURLConnection = null;

        try {

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestMethod(method);

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return httpURLConnection;
    }

    public String getResult(HttpURLConnection httpURLConnection){
        InputStream inputStream = null;
        String result = "";
        String line = "";

        try {

            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    private class getDataSource extends Thread {

        getDataSource(){

        }

        @Override
        public void run() {
            try {
                URL url = new URL(u);
                HttpURLConnection httpURLConnection = setupConnection(url, "GET");
                result = getResult(httpURLConnection);
                httpURLConnection.disconnect();
                stop = true;
                if (stop){
                    return;
                }

            } catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
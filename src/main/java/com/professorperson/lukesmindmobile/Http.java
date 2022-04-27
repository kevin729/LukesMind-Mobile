package com.professorperson.lukesmindmobile;

import android.content.Context;
import android.net.ConnectivityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class Http {
    public static String get(String _url, Context context) {
        ConnectivityManager c = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (c.getActiveNetworkInfo() == null || !c.getActiveNetworkInfo().isConnected()) {
            return "";
        }

        AtomicReference<String> response = new AtomicReference<String>();
        Thread thread = new Thread(() -> {
            response.set(httpRequest(_url, "GET", null));
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response.get();
    }

    public static String put(String _url, String data, Context context) {
        ConnectivityManager c = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (c.getActiveNetworkInfo() == null || !c.getActiveNetworkInfo().isConnected()) {
            return "";
        }

        String[] response = new String[1];
        Thread thread = new Thread(() -> {
             response[0] = httpRequest(_url, "PUT", data);
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response[0];
    }

    private static String httpRequest(String _url, String method, String data) {

        try {
            URL url = new URL(_url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method);
            //data type
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            //response
            con.setRequestProperty("Accept", "application/json");
            //body
            if (data != null) {
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = data.getBytes("utf-8");
                    os.write(input);
                    os.flush();
                }
            }

            if (con.getResponseCode() != 200) {
                return "";
            }

            //reads REST stream
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String out = "";
            String tempOut = br.readLine();
            while (tempOut != null) {
                out += tempOut;
                tempOut = br.readLine();
            }
            return out;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return "";
    }
}

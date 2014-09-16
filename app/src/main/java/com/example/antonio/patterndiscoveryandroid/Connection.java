package com.example.antonio.patterndiscoveryandroid;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Si occupa della connessione al server
 * Created by Antonio on 22/08/2014.
 */
public class Connection {
    private DataInputStream in; // Stream di input per la comunicazione con il server
    private DataOutputStream out; // Stream di output per la comunicazione con il server
    private Socket socket; // socket client

    public Connection(String ip, int Port) throws IOException {
        Log.d("INFO", "Apro la connessione");
        socket = new Socket(ip, Port);
        Log.d("INFO", "Socket Inizializzato");
        out = new DataOutputStream(socket.getOutputStream()); // messaggi da inviare al server
        in = new DataInputStream(socket.getInputStream()); // risposte del server
        Log.d("INFO", "Stream aperti");
    }

    public void CloseConnection() throws IOException {
        if (out != null && in != null) {
            out.writeUTF("end");
            out.close();
            in.close();
        }
        if (socket != null)
            socket.close();
    }

    public String Comunicate(final float minSup, final float epsilon, final String table) {
        final String[] Return = new String[1];
        Thread conn = new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                try {
                    json.put("cmd", "discovery");
                    json.put("table", table);
                    json.put("minSup", minSup);
                    json.put("epsilon", epsilon + 0.0005);
                    out.writeUTF(json.toString());
                    out.flush();
                    Return[0] = in.readUTF();
                    if (Return[0] == null || Return[0].isEmpty()) {
                        Return[0] = in.readUTF();
                    }
                } catch (IOException e) {
                    Log.e("ERROR", "Impossibile Comunicare");
                    Return[0] = "Errore Comunicazione, Riprovare";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        conn.start();
        while (conn.isAlive()) ;
        return Return[0];
    }
}

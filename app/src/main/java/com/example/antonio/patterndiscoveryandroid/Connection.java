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

    public void CloseConnection() throws IOException, JSONException {
        if (out != null && in != null) {
            out.writeUTF(new JSONObject().put("cmd", "end").toString());
            out.close();
            in.close();
        }
        if (socket != null)
            socket.close();
    }

    public String Comunicate(final float minSup, final float epsilon, final String table) {
        String Return;
        JSONObject json = new JSONObject();
        try {
            json.put("cmd", "discovery");
            json.put("table", table);
            json.put("minSup", minSup);
            json.put("epsilon", epsilon + 0.0005);
            out.writeUTF(json.toString());
            out.flush();
            Return = in.readUTF();
            if (Return == null || Return.isEmpty()) {
                Return = "Errore Comunicazione, Return null Riprovare";
            }
        } catch (IOException e) {
            Log.e("ERROR", "Impossibile Comunicare");
            Return = "Errore Comunicazione, Riprovare";
        } catch (JSONException e) {
            e.printStackTrace();
            Return = "Errore Comunicazione JSON, Riprovare";
        }
        return Return;
    }
}

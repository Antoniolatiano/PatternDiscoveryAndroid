package com.example.antonio.patterndiscoveryandroid;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class MyActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private Connection connection;
    private EditText msgAreaTxt, MinSupEditText;
    private TextView EpsilonLabel;
    private String IP;
    private int Port;
    private float epsilonValue;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        msgAreaTxt = (EditText) findViewById(R.id.TextArea);
        MinSupEditText = (EditText) findViewById(R.id.MinSupEditText);
        EpsilonLabel = (TextView) findViewById(R.id.epsTxt);
        SeekBar epsilonSeekBar = (SeekBar) findViewById(R.id.EpsilonseekBar);
        epsilonSeekBar.setOnSeekBarChangeListener(this);
        msgAreaTxt.setKeyListener(null);//impedire alla tastiera di comparire
//      if(savedInstanceState==null) {//Controllo il bundle(casomai esiste)
        //caricamento Dati da Intent
        Log.i("PatternDiscovery", "Caricamento informazioni Dall'Intent");
        IP = getIntent().getStringExtra("IP");
        Port = getIntent().getIntExtra("Port", 8080);
 /*     }else{
            //caricamento Dati da Bundle
          Log.i("PatternDiscovery","Caricamento informazioni dal Bundle");
          IP=savedInstanceState.getString("IP","localhost");
          Port=savedInstanceState.getInt("Port",8080);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("PatternDiscovery", "Chiamato onResume");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int Tries=0;
                final ProgressDialog dialog=new ProgressDialog(MyActivity.this);
                dialog.setIndeterminate(true);
                dialog.setMessage("Connessione al server");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.show();
                    }
                });
                while(connection==null && Tries<5 ) {
                    try {
                        Thread.sleep(500);
                        Log.i("PatternDiscovery", "Tentativo di Connessione " +(Tries+1)+" ad " + IP + ":" + Port);
                        connection = new Connection(IP, Port);
                    } catch (IOException e) {
                        MakeToast("Tentativo di connessione " + (Tries + 1) + "Fallito", Toast.LENGTH_SHORT);
                        Tries++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(connection==null){
                    dialog.dismiss();
                }else {
                    dialog.dismiss();
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        if (connection != null) {
            try {
                connection.CloseConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    public void CalculatePressed(View v) {
        Log.d("PatternDiscovery", "Calculate Pressed");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(MinSupEditText.getWindowToken(), 0);
        String toshow = "";
        file = new File(getFilesDir(), "playtennis_" + MinSupEditText.getText() + "_" + epsilonValue);
        try {
            if (MinSupEditText.getText().length() != 0)
                if (!((CheckBox)findViewById(R.id.IgnoraCachecheckBox)).isChecked() && file.exists() && file.length() > 3) {
                    Log.d("PatternDiscovery", "File " + file.getName() + " Trovato");
                    DataInputStream input = new DataInputStream(new FileInputStream(file));
                    toshow = input.readUTF();
                    input.close();
                    Log.d("PatternDiscovery", "File caricato");
                    MakeToast("Pattern caricati da Cache",Toast.LENGTH_SHORT);
                } else {
                    toshow = connection.Comunicate(Float.valueOf(MinSupEditText.getText().toString()), epsilonValue, "playtennis");
                    MakeToast("Pattern caricati da Database",Toast.LENGTH_SHORT);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    Log.i("PatternDiscovery", "Salvo il file");
                    DataOutputStream data = new DataOutputStream(new FileOutputStream(file));
                    data.writeUTF(msgAreaTxt.getText().toString() + "\n");
                    data.close();
                    Log.i("PatternDiscovery", "File " + file.getName() + " Salvato");
                }
            else {
                toshow = "Riempire tutti i Campi";
                Log.d("ERROR", "Parametri Mancanti");
                MakeToast("Riempire Tutti i campi", Toast.LENGTH_SHORT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        msgAreaTxt.setText(toshow);
    }

    public void MakeToast(final String Message, final int ToastLenght) {
        final MyActivity instance = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(instance, Message, ToastLenght).show();
            }
        });
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Log.i("PatternDiscovery", "SeekBar at:" + i);
        epsilonValue = (float) i / 100;
        EpsilonLabel.setText("Epsilon(" + epsilonValue + ")");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    /*  @Override
    public void onSaveInstanceState(Bundle outState){
        Log.i("PatternDiscovery","Salvataggio informazioni nel Bundle");
        outState.putString("IP",IP);
        outState.putInt("Port",Port);
        super.onSaveInstanceState(outState);
    }*/
}


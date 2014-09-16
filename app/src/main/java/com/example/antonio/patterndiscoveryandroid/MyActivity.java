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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class MyActivity extends Activity implements SeekBar.OnSeekBarChangeListener, Runnable {
    public final String[] jsonString = {""};
    public final String[] toshow = {""};
    private final MyActivity instance = this;
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
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Connessione al Server");
        dialog.show();
        new Thread(new Runnable() {
            int Tentativi = 0;

            @Override
            public void run() {
                try {
                    while (connection == null && Tentativi < 5) {
                        try {
                            Thread.sleep(2000);
                            Log.i("PatternDiscovery", "Tentativo di Connessione " + (Tentativi + 1) + " ad " + IP + ":" + Port);
                            connection = new Connection(IP, Port);
                        } catch (IOException e) {
                            MakeToast("Tentativo di connessione " + (Tentativi + 1) + " Fallito", Toast.LENGTH_SHORT);
                            Thread.sleep(2500);
                        }
                        Tentativi++;
                    }
                    if (connection != null) {
                        MakeToast("Connesso", Toast.LENGTH_SHORT);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    dialog.dismiss();
                    Log.i("PatternDiscovery", "Dialog Chiusa");
                    if (connection == null) {
                        Log.i("PatternDiscovery", "Connessione nulla, torno indietro");
                        MakeToast("Connessione fallita, verificare i dati e riprovare", Toast.LENGTH_SHORT);
                        instance.finish();
                    }
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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            connection = null;
        }
        super.onPause();
    }

    public void CalculatePressed(View v) {
        Log.d("PatternDiscovery", "Calculate Pressed");
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(MinSupEditText.getWindowToken(), 0);
        file = new File(getFilesDir(), "playtennis_" + MinSupEditText.getText() + "_" + epsilonValue);
        if (MinSupEditText.getText().length() != 0) {
            if (!((CheckBox) findViewById(R.id.IgnoraCachecheckBox)).isChecked() && file.exists() && file.length() > 3) {
                Log.d("PatternDiscovery", "File " + file.getName() + " Trovato");
                msgAreaTxt.setText(SupportClass.GetPatternsFromFile(file, instance));
                Log.d("PatternDiscovery", "File caricato");
                MakeToast("Pattern caricati da Cache", Toast.LENGTH_SHORT);
            } else {
                SupportClass.GetPatternsFromDB(instance, connection, Float.valueOf(MinSupEditText.getText().toString()), epsilonValue);
            }
        } else {
            Log.d("ERROR", "Parametri Mancanti");
            MakeToast("Riempire Tutti i campi", Toast.LENGTH_SHORT);
        }
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

    @Override
    public void run() {
        MakeToast("Pattern caricati da Database", Toast.LENGTH_SHORT);
        try {
            JSONObject json = new JSONObject(jsonString[0]);
            toshow[0] = json.getString("Frequent") + json.getString("Closed");
            SupportClass.SaveFile(file, toshow[0] + "\n", instance);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("PatternDiscovery", "File " + file.getName() + " Salvato");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgAreaTxt.setText(toshow[0]);
            }
        });
    }

    /*  @Override
    public void onSaveInstanceState(Bundle outState){
        Log.i("PatternDiscovery","Salvataggio informazioni nel Bundle");
        outState.putString("IP",IP);
        outState.putInt("Port",Port);
        super.onSaveInstanceState(outState);
    }*/
}


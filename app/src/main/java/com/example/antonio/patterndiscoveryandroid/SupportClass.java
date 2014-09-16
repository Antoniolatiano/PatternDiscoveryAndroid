package com.example.antonio.patterndiscoveryandroid;

import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Classe con alcuni metodi utilizzati
 * Created by Antonio on 16/09/2014.
 */
public class SupportClass {
    public static void GetPatternsFromDB(final MyActivity instance, final Connection connection, final Float MinSup, final Float epsilonValue) {
        final CyclicBarrier barrier = new CyclicBarrier(1, instance);
        new Thread(new Runnable() {
            @Override
            public void run() {
                instance.jsonString[0] = connection.Comunicate(MinSup, epsilonValue, "playtennis");
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public static String GetPatternsFromFile(File file, MyActivity instance) {
        String patterns = "";
        try {
            DataInputStream input = new DataInputStream(new FileInputStream(file));
            patterns = input.readUTF();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return patterns;
    }

    public static void SaveFile(File file, String Data, MyActivity instance) {
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.i("MyActivity", "Impossibile creare File");
                    instance.MakeToast("Impossibile creare il File", Toast.LENGTH_SHORT);
                }
            }
            Log.i("PatternDiscovery", "Salvo il file");
            DataOutputStream data = new DataOutputStream(new FileOutputStream(file));
            data.writeUTF(Data);
            data.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

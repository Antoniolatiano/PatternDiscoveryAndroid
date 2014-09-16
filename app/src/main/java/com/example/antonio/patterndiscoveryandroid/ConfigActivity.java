package com.example.antonio.patterndiscoveryandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class ConfigActivity extends Activity {
    private EditText IPEditText;
    private EditText PortEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        IPEditText = (EditText)findViewById(R.id.IPeditText);
        PortEditText = (EditText)findViewById(R.id.PorteditText);
    }

    public void AvantiClicked(View v){
        Intent intent=new Intent(this,MyActivity.class);
        intent.putExtra("IP",IPEditText.getText().toString());
        intent.putExtra("Port",Integer.parseInt(PortEditText.getText().toString()));
        this.startActivity(intent);
    }
}

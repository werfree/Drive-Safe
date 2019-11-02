package com.example.drivesafe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class Edit extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor meditor;

    String name,phnNo1,phnNo2;
    EditText nameText,phn1Text,phn2Text;
    ImageView button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        nameText=findViewById(R.id.inputName);
        phn1Text=findViewById(R.id.em1);
        phn2Text=findViewById(R.id.em2);
        button=findViewById(R.id.next);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        meditor=sharedPreferences.edit();
        name = sharedPreferences.getString("name","");
        phnNo1=sharedPreferences.getString("phnNo1","");
        phnNo2=sharedPreferences.getString("phnNo2","");

        if(checkdata()){
            nameText.setText(name);
            phn1Text.setText(phnNo1);
            phn2Text.setText(phnNo2);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDetails();
            }
        });
    }

    private void submitDetails() {
        name=nameText.getText().toString();
        phnNo1=phn1Text.getText().toString();
        phnNo2=phn2Text.getText().toString();
        if(name.equals("")){
            nameText.setError("Enter your name");
        }else if(phnNo1.length()!=10){
            phn1Text.setError("Invalid Phone Number");
        }else if(phnNo2.length()!=10){
            phn2Text.setError("Invalid Phone Number");
        }else if(phnNo2.equals(phnNo1)){
            phn2Text.setError("Enter Different Number");
        }else{
                meditor.putString("name",name);
                meditor.putString("phnNo1",phnNo1);
                meditor.putString("phnNo2",phnNo2);
                meditor.commit();
                startActivity(new Intent(this,MainActivity.class));

        }
    }

    private boolean checkdata() {
        if(name.equals("")||phnNo2.equals("")||phnNo1.equals(""))
            return false;
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
    }
}

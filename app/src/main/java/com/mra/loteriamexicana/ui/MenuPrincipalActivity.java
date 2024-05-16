package com.mra.loteriamexicana.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.mra.loteriamexicana.R;

public class MenuPrincipalActivity extends AppCompatActivity {

    Button btnCartasLoteria, btnJuegaLoteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);
        init();
        actionControls();
    }
    private void init(){
        btnCartasLoteria = findViewById(R.id.btnCartasLoteria);
        btnJuegaLoteria = findViewById(R.id.btnJuegaLoteria);
    }
    private void actionControls(){
        btnCartasLoteria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                barajearCartas();
            }
        });
        btnJuegaLoteria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
    private void barajearCartas(){
        Intent intent = new Intent(MenuPrincipalActivity.this, BarajearCartasActivity.class);
        startActivity(intent);
    }
}
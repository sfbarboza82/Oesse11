package br.net.oesse11.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MenuTecnicoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_tecnico);

        getSupportActionBar().hide();
    }

    public void abrirTelaRequisicoes(View view){

        startActivity( new Intent(this, RequisicoesActivity.class));
    }

    public void abrirTelaTemp(View view){
        startActivity( new Intent());
    }
}

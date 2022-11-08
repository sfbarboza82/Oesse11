package br.net.oesse11.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import br.net.oesse11.adpter.RequisicoesAdapter;
import br.net.oesse11.config.ConfiguracaoFirebase;
import br.net.oesse11.model.Requisicao;
import br.net.oesse11.model.Destino;
import br.net.oesse11.model.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CadastroOsActivity extends AppCompatActivity {

    private EditText editNome;
    private EditText editProduto;
    private EditText editDefeito;
    private EditText editDestino;
    private LinearLayout linearLayoutDestino;
    private Button buttonCriarOs;

    private RecyclerView recyclerRequisicoes;
    private List<Requisicao> listaRequisicoes = new ArrayList<>();
    private GoogleMap mMap;
    private FirebaseAuth autenticacao;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean osCriada = false;
    private DatabaseReference firebaseRef;
    private Requisicao requisicao;
    private RequisicoesAdapter adapter;
    private Usuario tecnico;
    private String idRequisicao;
    private String statusRequisicao;
    private Destino destino;
    private LatLng localTecnico;
    private LatLng localDestino;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_os);

        inicializarComponentes();

    }

    public void criarOs(View view){

        if(buttonCriarOs.getText() == "CANCELAR OS"){

            requisicao.setStatus(Requisicao.STATUS_CANCELADA);
            requisicao.atualizarStatus();

        }else{

            if( !osCriada ){//Os não foi criada

                final String nome = editNome.getText().toString();
                final String produto = editProduto.getText().toString();
                final String defeito = editDefeito.getText().toString();
                String enderecoDestino = editDestino.getText().toString();

                if( !enderecoDestino.equals("") || enderecoDestino != null ){

                    Address addressDestino = recuperarEndereco( enderecoDestino );
                    if( addressDestino != null ){

                        final Destino destino = new Destino();
                        destino.setCep( addressDestino.getPostalCode() );
                        destino.setBairro( addressDestino.getSubLocality() );
                        destino.setRua( addressDestino.getThoroughfare() );
                        destino.setNumero( addressDestino.getFeatureName() );
                        destino.setLatitude( String.valueOf(addressDestino.getLatitude()) );
                        destino.setLongitude( String.valueOf(addressDestino.getLongitude()) );

                        StringBuilder mensagem = new StringBuilder();
                        mensagem.append( "\nCliente: " + nome );
                        mensagem.append( "\nProduto: " + produto );
                        mensagem.append( "\nDefeito: " + defeito );
                        mensagem.append( "\nRua: " + destino.getRua() );
                        mensagem.append( "\nBairro: " + destino.getBairro() );
                        mensagem.append( "\nNúmero: " + destino.getNumero() );
                        mensagem.append( "\nCep: " + destino.getCep() );

                        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                .setTitle("Confirme as Informações!")
                                .setMessage(mensagem)
                                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //salvar requisição
                                        salvarRequisicao( nome, produto, defeito, destino );
                                        osCriada = true;

                                    }
                                }).setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }

                }else {
                    Toast.makeText(this,
                            "Informe o endereço de destino!",
                            Toast.LENGTH_SHORT).show();
                }

            }else {
                //Cancelar a requisição

                osCriada = false;
            }
        }



    }

    private void salvarRequisicao(String nome, String produto, String defeito, Destino destino){

        Requisicao requisicao = new Requisicao();
        requisicao.setNome( nome );
        requisicao.setProduto( produto);
        requisicao.setDefeito( defeito );
        requisicao.setDestino( destino );

        requisicao.setStatus( Requisicao.STATUS_AGUARDANDO );
        requisicao.salvar();

        abrirTelaRequisicoes();

    }

    private void abrirTelaRequisicoes() {

        startActivity( new Intent(this, RequisicoesActivity.class));

    }

    private Address recuperarEndereco(String endereco){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(endereco, 1);
            if( listaEnderecos != null && listaEnderecos.size() > 0 ){
                Address address = listaEnderecos.get(0);

                return address;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuSair :
                autenticacao.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void inicializarComponentes(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Digite as Informações");
        setSupportActionBar(toolbar);

        //Componentes

        editNome = findViewById(R.id.editNome);
        editProduto = findViewById(R.id.editProduto);
        editDefeito = findViewById(R.id.editDefeito);
        editDestino = findViewById(R.id.editDestino);
        linearLayoutDestino = findViewById(R.id.linearLayoutDestino);
        buttonCriarOs = findViewById(R.id.buttonCriarOs);

        //Configurações iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

    }

}

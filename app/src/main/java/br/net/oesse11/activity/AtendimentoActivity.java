package br.net.oesse11.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import br.net.oesse11.config.ConfiguracaoFirebase;
import br.net.oesse11.helper.UsuarioFirebase;
import br.net.oesse11.model.Requisicao;
import br.net.oesse11.model.Destino;
import br.net.oesse11.model.Usuario;

import static br.net.oesse11.helper.UsuarioFirebase.getIdentificadorUsuario;
import static br.net.oesse11.helper.UsuarioFirebase.getUsuarioAtual;

public class AtendimentoActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    //componente
    private Button buttonAceitarOs;
    private FloatingActionButton fabRota;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localTecnico;
    private Usuario usuario;
    private Usuario tecnico;
    private Usuario atendente;
    private String idRequisicao;
    private Requisicao requisicao;
    private DatabaseReference firebaseRef;
    private Marker marcadorTecnico;
    private Marker marcadorDestino;
    private String statusRequisicao;
    private boolean requisicaoAtiva;
    private Destino destino;
    private boolean cancelarOs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atendimento);

        inicializarComponentes();

        //Recupera dados do usuário
        if( getIntent().getExtras().containsKey("idRequisicao")
                && getIntent().getExtras().containsKey("tecnico") ){
            Bundle extras = getIntent().getExtras();
            tecnico = (Usuario) extras.getSerializable("tecnico");
            localTecnico = new LatLng(
                    Double.parseDouble(tecnico.getLatitude()),
                    Double.parseDouble(tecnico.getLongitude())
            );
            idRequisicao = extras.getString("idRequisicao");
            requisicaoAtiva = extras.getBoolean("requisicaoAtiva");
            verificaStatusRequisicao();
        }

    }

    private void verificaStatusRequisicao(){

        DatabaseReference requisicoes = firebaseRef.child("requisicoes")
                .child( idRequisicao );
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Recupera requisição
                requisicao = dataSnapshot.getValue(Requisicao.class);
                if(requisicao != null){
                    statusRequisicao = requisicao.getStatus();
                    destino = requisicao.getDestino();
                    alteraInterfaceStatusRequisicao(statusRequisicao);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void alteraInterfaceStatusRequisicao(String status){

        switch ( status ){
            case Requisicao.STATUS_AGUARDANDO :
                requisicaoAguardando();
                break;
            case Requisicao.STATUS_A_CAMINHO :
                requisicaoACaminho();
                break;
            case Requisicao.STATUS_FINALIZADA :
                requisicaoFinalizada();
                break;
            case Requisicao.STATUS_CANCELADA :
                requisicaoCancelada();
                break;
        }

    }

    private void requisicaoCancelada(){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdentificadorUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Usuario usuario = dataSnapshot.getValue( Usuario.class );

                    String tipoUsuario = usuario.getTipo();
                    if( tipoUsuario.equals("A") ){

                        Context contexto = getApplicationContext();
                        String texto = "REQUISIÇÃO CANCELADA PELO TÉCNICO!";
                        int duracao = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(contexto, texto,duracao);
                        toast.show();

                        startActivity(new Intent(AtendimentoActivity.this, RequisicoesActivity.class));

                    }
                    else {

                        Context contexto = getApplicationContext();
                        String texto = "REQUISIÇÃO CANCELADA PELO ATENDENTE!";
                        int duracao = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(contexto, texto,duracao);
                        toast.show();

                        startActivity(new Intent(AtendimentoActivity.this, RequisicoesActivity.class));

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void requisicaoFinalizada(){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdentificadorUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Usuario usuario = dataSnapshot.getValue( Usuario.class );

                    String tipoUsuario = usuario.getTipo();
                    if( tipoUsuario.equals("A") ){

                    }
                    else {

                        getSupportActionBar().setTitle("OS FINALIZADA");
                        fabRota.setVisibility(View.GONE);
                        requisicaoAtiva = false;

                        if( marcadorTecnico != null )
                            marcadorTecnico.remove();

                        if( marcadorDestino != null )
                            marcadorDestino.remove();

                        //Exibe marcador de destino
                        LatLng localDestino = new LatLng(
                                Double.parseDouble(destino.getLatitude()),
                                Double.parseDouble(destino.getLongitude())
                        );
                        adicionaMarcadorDestino(localDestino, "Destino");
                        centralizarMarcador(localDestino);

                        //Calcular distancia
                        //float distancia = Local.calcularDistancia(localAtendente, localDestino);
                        //float valor = distancia * 8;//4.56
                        //DecimalFormat decimal = new DecimalFormat("0.00");
                        //String resultado = decimal.format(valor);

                        buttonAceitarOs.setText("OS finalizada - R$ " + null );

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void centralizarMarcador(LatLng local){
        mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(local, 20)
        );
    }

    private void requisicaoAguardando(){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdentificadorUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Usuario usuario = dataSnapshot.getValue( Usuario.class );

                    String tipoUsuario = usuario.getTipo();
                    if( tipoUsuario.equals("A") ){

                        getSupportActionBar().setTitle("AGUARDANDO UM TÉCNICO");
                        buttonAceitarOs.setText("CANCELAR OS");

                        LatLng localDestino = new LatLng(
                                Double.parseDouble(destino.getLatitude()),
                                Double.parseDouble(destino.getLongitude())
                        );
                        adicionaMarcadorDestino(localDestino, "Destino");
                        centralizarMarcador(localDestino);
                    }
                    else {

                        buttonAceitarOs.setText("ACEITAR OS");

                        //Exibe marcador do destino
                        LatLng localDestino = new LatLng(
                                Double.parseDouble(destino.getLatitude()),
                                Double.parseDouble(destino.getLongitude())
                        );
                        adicionaMarcadorDestino(localDestino, "Destino");
                        centralizarMarcador(localDestino);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void requisicaoACaminho(){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdentificadorUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Usuario usuario = dataSnapshot.getValue( Usuario.class );

                    String tipoUsuario = usuario.getTipo();
                    if( tipoUsuario.equals("A") ){

                        getSupportActionBar().setTitle("O TÉCNICO ESTA A CAMINHO");
                        buttonAceitarOs.setText("CANCELAR OS");

                        //Exibe marcador do tecnico
                        adicionaMarcadorTecnico(localTecnico, tecnico.getNome() );

                        //Exibe marcador de destino
                        LatLng localDestino = new LatLng(
                                Double.parseDouble(destino.getLatitude()),
                                Double.parseDouble(destino.getLongitude())
                        );
                        adicionaMarcadorDestino(localDestino, "Destino");

                        //Centralizar dois marcadores
                        centralizarDoisMarcadores(marcadorTecnico, marcadorDestino);

                        //Inicia monitoramento do tecnico / destino
                        iniciarMonitoramento(tecnico, localDestino, Requisicao.STATUS_A_CAMINHO );

                    }
                    else {

                        getSupportActionBar().setTitle("O TÉCNICO ESTA A CAMINHO");
                        buttonAceitarOs.setText("CANCELAR OS");
                        fabRota.setVisibility(View.VISIBLE);

                        //Exibe marcador do tecnico
                        adicionaMarcadorTecnico(localTecnico, tecnico.getNome() );

                        //Exibe marcador de destino
                        LatLng localDestino = new LatLng(
                                Double.parseDouble(destino.getLatitude()),
                                Double.parseDouble(destino.getLongitude())
                        );
                        adicionaMarcadorDestino(localDestino, "Destino");

                        //Centralizar dois marcadores
                        centralizarDoisMarcadores(marcadorTecnico, marcadorDestino);

                        //Inicia monitoramento do tecnico / destino
                        iniciarMonitoramento(tecnico, localDestino, Requisicao.STATUS_A_CAMINHO );

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void iniciarMonitoramento(final Usuario uOrigem, LatLng localDestino, final String status){

        //Inicializar GeoFire
        DatabaseReference localizacao = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("localizacao");
        GeoFire geoFire = new GeoFire(localizacao);

        //Adiciona círculo no destino
        final Circle circulo = mMap.addCircle(
                new CircleOptions()
                        .center( localDestino )
                        .radius(50)//em metros
                        .fillColor(Color.argb(90,255, 153,0))
                        .strokeColor(Color.argb(190,255,152,0))
        );

        final GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(localDestino.latitude, localDestino.longitude),
                0.05//em km (0.05 50 metros)
        );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if( key.equals(uOrigem.getId()) ){
                    //Log.d("onKeyEntered", "onKeyEntered: tecnico está dentro da área!");

                    //Altera status da requisicao
                    requisicao.setStatus(status);
                    requisicao.atualizarStatus();

                    //Remove listener
                    geoQuery.removeAllListeners();
                    circulo.remove();
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void centralizarDoisMarcadores(Marker marcador1, Marker marcador2){

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include( marcador1.getPosition() );
        builder.include( marcador2.getPosition() );

        LatLngBounds bounds = builder.build();

        int largura = getResources().getDisplayMetrics().widthPixels;
        int altura = getResources().getDisplayMetrics().heightPixels;
        int espacoInterno = (int) (largura * 0.20);

        mMap.moveCamera(
                CameraUpdateFactory.newLatLngBounds(bounds,largura,altura,espacoInterno)
        );

    }

    private void adicionaMarcadorTecnico(LatLng localizacao, String titulo){

        if( marcadorTecnico != null )
            marcadorTecnico.remove();

        marcadorTecnico = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tecnico))
        );

    }

    private void adicionaMarcadorDestino(LatLng localizacao, String titulo){

        if( marcadorDestino != null )
            marcadorDestino.remove();

        marcadorDestino = mMap.addMarker(
                new MarkerOptions()
                        .position(localizacao)
                        .title(titulo)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        recuperarLocalizacaoUsuario();

    }

    private void recuperarLocalizacaoUsuario() {

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                //recuperar latitude e longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                localTecnico = new LatLng(latitude, longitude);

                //Atualizar GeoFire
                UsuarioFirebase.atualizarDadosLocalizacao(latitude, longitude);

                //Atualizar localização tecnico no Firebase
                tecnico.setLatitude(String.valueOf(latitude));
                tecnico.setLongitude(String.valueOf(longitude));
                requisicao.setTecnico( tecnico );
                requisicao.atualizarLocalizacaoTecnico();

                alteraInterfaceStatusRequisicao(statusRequisicao);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizações de localização
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener
            );
        }

    }

    public void aceitarOs(View view){

        //Configura requisicao
        requisicao = new Requisicao();
        requisicao.setId( idRequisicao );
        requisicao.setTecnico( tecnico );
        requisicao.setStatus( Requisicao.STATUS_A_CAMINHO );

        requisicao.atualizar();

    }

    private void inicializarComponentes(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Destino");

        buttonAceitarOs = findViewById(R.id.buttonAceitarOs);

        //Configurações iniciais
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Adiciona evento de clique no FabRota
        fabRota = findViewById(R.id.fabRota);
        fabRota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status = statusRequisicao;
                if( status != null && !status.isEmpty() ){

                    String lat = "";
                    String lon = "";

                    switch ( status ){
                        case Requisicao.STATUS_A_CAMINHO :
                            lat = destino.getLatitude();
                            lon = destino.getLongitude();
                            break;
                    }

                    //Abrir rota
                    String latLong = lat + "," + lon;
                    Uri uri = Uri.parse("google.navigation:q="+latLong+"&mode=d");
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    i.setPackage("com.google.android.apps.maps");
                    startActivity(i);

                }

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (requisicaoAtiva){
            Toast.makeText(AtendimentoActivity.this,
                    "Necessário encerrar a requisição atual!",
                    Toast.LENGTH_SHORT).show();
        }else {
            Intent i = new Intent(AtendimentoActivity.this, RequisicoesActivity.class);
            startActivity(i);
        }

        /*
        //Verificar o status da requisição para encerrar
        if( statusRequisicao != null && !statusRequisicao.isEmpty() ){
            requisicao.setStatus(Requisicao.STATUS_ENCERRADA);
            requisicao.atualizarStatus();
        }
        */
        return false;
    }
}

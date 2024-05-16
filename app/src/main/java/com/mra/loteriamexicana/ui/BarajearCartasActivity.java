package com.mra.loteriamexicana.ui;

import static android.content.Context.MODE_PRIVATE;
import static com.mra.loteriamexicana.utilerias.CatalogoCartas.idsBaraja;
import static com.mra.loteriamexicana.utilerias.CatalogoCartas.nombresAudioBaraja;
import static com.mra.loteriamexicana.utilerias.CatalogoCartas.nombresAudioVersosBaraja;
import static com.mra.loteriamexicana.utilerias.CatalogoCartas.nombresBaraja;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.mra.loteriamexicana.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import android.view.WindowManager;

public class BarajearCartasActivity extends AppCompatActivity {

    ImageButton topLeftImageButton1, topLeftImageButton2, topLeftImageButton3;
    ImageButton topRightImageButton1, topRightImageButton2, topRightImageButton3;
    ImageButton nextButton, backButton;
    ImageView centerImageView;
    TextView leyendaTextView, nombreCartaTextView, contadorTiempoTextView;
    private ArrayList<Integer> barajaDesordenada = new ArrayList<>();
    private ArrayList<Integer> cartasMostradas = new ArrayList<>();
    private int indexActual = 0;

    // Define y declara una lista de cartas con leyenda
    private ArrayList<Integer> cartasConLeyenda = new ArrayList<>();

    // Mapa para asociar los IDs de las cartas con sus nombres
    private Map<Integer, String> nombreCartasMap = new HashMap<>();
    // Define una variable para almacenar el estado actual del ImageButton
    boolean isVolumeOff = false;
    boolean isAudioOff = true;
    // Dentro de tu actividad, declara el handler
    private Handler mainHandler;
    String lectura = "";
    String tiempo = "";
    String tipoCartas = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barajear_cartas);
        mainHandler = new Handler(Looper.getMainLooper());
        // Mantener la pantalla encendida
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        actionAcontrols();
        //llenarBarajaDesordenada();
        //mostrarSiguienteImagen();
        //empezarActualizacionAutomatica();
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        lectura = sharedPreferences.getString("lectura", "");
        tiempo = sharedPreferences.getString("tiempo", "");
        tipoCartas = sharedPreferences.getString("tipoCartas", "");

        if(tiempo.equalsIgnoreCase("Automático") || tiempo.equalsIgnoreCase("") ){
            nextButton.setVisibility(View.INVISIBLE);
            backButton.setVisibility(View.INVISIBLE);
            llenarBarajaDesordenada();
            mostrarSiguienteImagen();
            empezarActualizacionAutomatica();
        }else if(tiempo.equalsIgnoreCase("Manual")){
            llenarBarajaDesordenada();
            mostrarSiguienteImagenManual();
            nextButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        centerImageView = findViewById(R.id.centerImageView);
        topLeftImageButton1 = findViewById(R.id.topLeftImageButton1);
        topLeftImageButton2 = findViewById(R.id.topLeftImageButton2);
        topLeftImageButton3 = findViewById(R.id.topLeftImageButton3);
        topRightImageButton1 = findViewById(R.id.topRightImageButton1);
        topRightImageButton2 = findViewById(R.id.topRightImageButton2);
        topRightImageButton3 = findViewById(R.id.topRightImageButton3);
        nextButton = findViewById(R.id.nextButton);
        backButton = findViewById(R.id.backButton);
        leyendaTextView = findViewById(R.id.leyendaTextView);
        nombreCartaTextView = findViewById(R.id.nombreCartaTextView);
        contadorTiempoTextView = findViewById(R.id.contadorTiempoTextView);
        // Agrega los IDs de las cartas con leyenda al ArrayList
        cartasConLeyenda.add(R.drawable.el_gallo);
        // Agrega los demás IDs de las cartas con leyenda aquí
        // Llenar el mapa de nombres de cartas
        llenarMapaNombres();
    }

    private void actionAcontrols() {
        topLeftImageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reanudarActualizacionAutomatica();
            }
        });
        topLeftImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pausarActualizacionAutomatica();
            }
        });
        topRightImageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                indexActual = 0;
                cartasMostradas.clear();
                if (mPlayer != null) {
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                }
                // Detener la actualización automática si está en curso
                detenerActualizacionAutomatica();
                RegresarMenu();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrocederImagen();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarSiguienteImagen();
            }
        });

        topRightImageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reiniciarJuego("Reiniciar juego", "¿Deseas reiniciar el juego?");
                /*indexActual = 0;
                cartasMostradas.clear();
                llenarBarajaDesordenada();
                mostrarSiguienteImagen();
                //llenarMapaNombres();*/
            }
        });
        topLeftImageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verifica el estado actual del ImageButton
                if (isVolumeOff) {
                    // Si el volumen está apagado, cambia al icono de volumen encendido
                    isAudioOff = true;
                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_volume_up_48);
                    topLeftImageButton3.setImageDrawable(drawable);
                    if (nombreCarta != null) {
                        nombreCartaTextView.setText(nombreCarta);
                        for(int i=0; i<nombresBaraja.length; i++){
                            if(nombresBaraja[i].equalsIgnoreCase(nombreCarta)){
                                nombreAudio = nombresAudioBaraja[i];
                                if(lectura.equalsIgnoreCase("Leer cartas") || lectura.equalsIgnoreCase("")){
                                    nombreAudio = nombresAudioBaraja[i];
                                }else if(lectura.equalsIgnoreCase("Leer cartas con verso")){
                                    nombreAudio = nombresAudioVersosBaraja[i];
                                }
                                break;
                            }
                        }
                        if(isAudioOff){
                            reproduceMp3(nombreAudio);
                        }
                    }

                } else {
                    // Si el volumen está encendido, cambia al icono de volumen apagado
                    isAudioOff = false;
                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_volume_off_48);
                    topLeftImageButton3.setImageDrawable(drawable);
                    if(mPlayer != null){
                        if(mPlayer.isPlaying()){
                            mPlayer.stop();
                        }
                    }

                }
                // Cambia el estado del ImageButton
                isVolumeOff = !isVolumeOff;
            }
        });
        topRightImageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomDialog();
            }
        });
    }

    private void llenarMapaNombres() {
        for (int i = 0; i < idsBaraja.length; i++) {
            nombreCartasMap.put(idsBaraja[i], nombresBaraja[i]);
        }
    }

    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(BarajearCartasActivity.this);
        builder.setTitle("Herramientas");

        // Inflar el diseño personalizado del diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom_tools, null);
        builder.setView(dialogView);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Manejar la lógica cuando se hace clic en Aceptar
                handleDialogPositiveButton(dialogView);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Manejar la lógica cuando se hace clic en Cancelar
                dialog.dismiss(); // Cierra el diálogo
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para manejar la lógica cuando se hace clic en Aceptar
    private void handleDialogPositiveButton(View dialogView) {
        // Obtener la selección de cada RadioGroup desde la vista del diálogo
        RadioGroup radioGroupLecturaCartas = dialogView.findViewById(R.id.radioGroupLecturaCartas);
        int selectedLecturaId = radioGroupLecturaCartas.getCheckedRadioButtonId();
        RadioButton selectedLecturaButton = dialogView.findViewById(selectedLecturaId);

        RadioGroup radioGroupTiempo = dialogView.findViewById(R.id.radioGroupTiempo);
        int selectedTiempoId = radioGroupTiempo.getCheckedRadioButtonId();
        RadioButton selectedTiempoButton = dialogView.findViewById(selectedTiempoId);

        RadioGroup radioGroupTipoCartas = dialogView.findViewById(R.id.radioGroupTipoCartas);
        int selectedTipoCartasId = radioGroupTipoCartas.getCheckedRadioButtonId();
        RadioButton selectedTipoCartasButton = dialogView.findViewById(selectedTipoCartasId);

        String lecturaLocal = "";
        String tiempoLocal = "";
        String tipoCartasLocal = "";

        // Obtener el texto de cada selección
        if (selectedLecturaButton != null) {
            lecturaLocal = selectedLecturaButton.getText().toString();
        }
        if (selectedTiempoButton != null) {
            tiempoLocal = selectedTiempoButton.getText().toString();
        }
        if (selectedTipoCartasButton != null) {
            tipoCartasLocal = selectedTipoCartasButton.getText().toString();
        }

        String message = "Lectura: " + lecturaLocal + "\nTiempo: " + tiempoLocal + "\nTipo de Cartas: " + tipoCartasLocal;
        Toast.makeText(BarajearCartasActivity.this, message, Toast.LENGTH_LONG).show();

        // Guardar las selecciones en SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lectura", lecturaLocal);
        editor.putString("tiempo", tiempoLocal);
        editor.putString("tipoCartas", tipoCartasLocal);
        editor.apply();

        // Obtener las selecciones guardadas
        lectura = sharedPreferences.getString("lectura", "");
        tiempo = sharedPreferences.getString("tiempo", "");
        tipoCartas = sharedPreferences.getString("tipoCartas", "");

        // Acciones basadas en la selección de tiempo
        if (tiempo.equalsIgnoreCase("Automático") || tiempo.isEmpty()) {
            nextButton.setVisibility(View.INVISIBLE);
            backButton.setVisibility(View.INVISIBLE);
            contadorTiempoTextView.setVisibility(View.VISIBLE);
            llenarBarajaDesordenada();
            mostrarSiguienteImagen();
            empezarActualizacionAutomatica();
        } else if (tiempo.equalsIgnoreCase("Manual")) {
            detenerActualizacionAutomatica();
            llenarBarajaDesordenada();
            mostrarSiguienteImagenManual();
            nextButton.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
        }
    }


    private void llenarBarajaDesordenada() {
        barajaDesordenada.clear();
        cartasMostradas.clear();
        for (int id : idsBaraja) {
            barajaDesordenada.add(id);
        }
        Collections.shuffle(barajaDesordenada);
    }

    String nombreCarta = "";
    String nombreAudio = "";
    private CountDownTimer mCountDownTimer;
    private boolean ventanaReinicioMostrada = false;
    private void mostrarSiguienteImagen() {
        mCountDownTimer = null;

        if (indexActual < barajaDesordenada.size()) {
            int idImagen = barajaDesordenada.get(indexActual);
            // Usamos el handler para actualizar la UI
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    centerImageView.setImageResource(idImagen);
                }
            });

            if (cartasMostradas.contains(idImagen)) {
                // Si la carta ya se ha mostrado, mostramos la leyenda
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        leyendaTextView.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                // Si es una carta nueva, ocultamos la leyenda
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        leyendaTextView.setVisibility(View.GONE);
                    }
                });
            }
            cartasMostradas.add(idImagen);

            // Actualizar el nombre de la carta
            nombreCarta = nombreCartasMap.get(idImagen);
            nombreAudio = "";
            if (nombreCarta != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCountDownTimer = new CountDownTimer(5000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                String timerValue =   String.format("%02d:%02d:%02d",
                                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)), // The change is in this line
                                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                                contadorTiempoTextView.setText(timerValue);
                            }
                            public void onFinish() {
                                //contadorTiempoTextView.setText("00:00:00");
                            }
                        }.start();
                        Log.e("nombreCarta", nombreCarta);
                        nombreCartaTextView.setText(nombreCarta);
                    }
                });

                // Define un mapa para asociar los nombres de las cartas con los nombres de los archivos de audio
                HashMap<String, String> mapaNombresAudio = new HashMap<>();

                if(lectura.equalsIgnoreCase("Leer cartas") || lectura.equalsIgnoreCase("")){
                    for (int i = 0; i < nombresBaraja.length; i++) {
                        mapaNombresAudio.put(nombresBaraja[i].toLowerCase(), nombresAudioBaraja[i]);
                    }
                }else if(lectura.equalsIgnoreCase("Leer cartas con verso")){
                    for (int i = 0; i < nombresBaraja.length; i++) {
                        mapaNombresAudio.put(nombresBaraja[i].toLowerCase(), nombresAudioVersosBaraja[i]);
                    }
                }

// Busca el nombre del archivo de audio correspondiente al nombre de la carta
                String nombreAudio = mapaNombresAudio.get(nombreCarta.toLowerCase());

// Reproduce el audio si se encontró un nombre de archivo de audio correspondiente
                if (nombreAudio != null && !nombreAudio.isEmpty() && isAudioOff) {
                    reproduceMp3(nombreAudio);
                }


            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        nombreCartaTextView.setText("No name");
                    }
                });
            }

            indexActual++;
        } else {
            // Si ya se mostraron todas las imágenes, se reinicia la baraja
            //reiniciarJuego("Fin de la baraja", "¡Has llegado al final de la baraja! ¿Deseas reiniciar el juego?");
            if (!ventanaReinicioMostrada) {
                reiniciarJuego("Fin de la baraja", "¡Has llegado al final de la baraja! ¿Deseas reiniciar el juego?");
                ventanaReinicioMostrada = true; // Marca que la ventana se ha mostrado
            }
        }
    }

    private void mostrarSiguienteImagenManual() {
        contadorTiempoTextView.setVisibility(View.INVISIBLE);
        if (indexActual < barajaDesordenada.size()) {
            int idImagen = barajaDesordenada.get(indexActual);
            // Usamos el handler para actualizar la UI
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    centerImageView.setImageResource(idImagen);
                }
            });

            if (cartasMostradas.contains(idImagen)) {
                // Si la carta ya se ha mostrado, mostramos la leyenda
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        leyendaTextView.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                // Si es una carta nueva, ocultamos la leyenda
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        leyendaTextView.setVisibility(View.GONE);
                    }
                });
            }
            cartasMostradas.add(idImagen);

            // Actualizar el nombre de la carta
            nombreCarta = nombreCartasMap.get(idImagen);
            nombreAudio = "";
            if (nombreCarta != null) {

                Log.e("nombreCarta", nombreCarta);
                nombreCartaTextView.setText(nombreCarta);

                // Define un mapa para asociar los nombres de las cartas con los nombres de los archivos de audio
                HashMap<String, String> mapaNombresAudio = new HashMap<>();


                if(lectura.equalsIgnoreCase("Leer cartas") || lectura.equalsIgnoreCase("")){
                    for (int i = 0; i < nombresBaraja.length; i++) {
                        mapaNombresAudio.put(nombresBaraja[i].toLowerCase(), nombresAudioBaraja[i]);
                    }
                }else if(lectura.equalsIgnoreCase("Leer cartas con verso")){
                    for (int i = 0; i < nombresBaraja.length; i++) {
                        mapaNombresAudio.put(nombresBaraja[i].toLowerCase(), nombresAudioVersosBaraja[i]);
                    }
                }

// Busca el nombre del archivo de audio correspondiente al nombre de la carta
                String nombreAudio = mapaNombresAudio.get(nombreCarta.toLowerCase());

                if (isAudioOff) {
                    reproduceMp3(nombreAudio);
                }


            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        nombreCartaTextView.setText("No name");
                    }
                });
            }

            indexActual++;
        } else {
            // Si ya se mostraron todas las imágenes, se reinicia la baraja
            //reiniciarJuego("Fin de la baraja", "¡Has llegado al final de la baraja! ¿Deseas reiniciar el juego?");
            if (!ventanaReinicioMostrada) {
                reiniciarJuego("Fin de la baraja", "¡Has llegado al final de la baraja! ¿Deseas reiniciar el juego?");
                ventanaReinicioMostrada = true; // Marca que la ventana se ha mostrado
            }
        }
    }

    private void reiniciarJuego(String titulo, String mensaje) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(BarajearCartasActivity.this);
                    builder.setTitle(titulo)
                            .setMessage(mensaje)
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ventanaReinicioMostrada = false;
                                    indexActual = 0;
                                    cartasMostradas.clear();
                                    llenarBarajaDesordenada();
                                    mostrarSiguienteImagen();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    RegresarMenu();
                                }
                            })
                            .setCancelable(false);

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }



    private void retrocederImagen() {
        if (indexActual > 1) { // Se asegura de que no estamos en la primera carta
            indexActual -= 2; // Retrocede dos veces el índice
            int idImagen = barajaDesordenada.get(indexActual); // Obtiene la imagen anterior
            centerImageView.setImageResource(idImagen);
            // Verificar si la carta mostrada tiene leyenda
            if (cartasMostradas.contains(idImagen)) {
                leyendaTextView.setVisibility(View.VISIBLE);
            } else {
                leyendaTextView.setVisibility(View.GONE);
            }

            // Actualizar el nombre de la carta
            String nombreCarta = nombreCartasMap.get(idImagen);
            if (nombreCarta != null) {
                nombreCartaTextView.setText(nombreCarta);
            } else {
                nombreCartaTextView.setText("No name");
            }

            indexActual++; // Avanzamos uno para ajustar el índice actual
        } else if (indexActual == 1) {
            indexActual--; // Si estamos en la segunda carta, solo retrocedemos una vez
            int idImagen = barajaDesordenada.get(indexActual); // Obtiene la imagen anterior
            centerImageView.setImageResource(idImagen);

            // Verificar si la carta mostrada tiene leyenda
            if (cartasMostradas.contains(idImagen)) {
                leyendaTextView.setVisibility(View.VISIBLE);
            } else {
                leyendaTextView.setVisibility(View.GONE);
            }

            // Actualizar el nombre de la carta
            String nombreCarta = nombreCartasMap.get(idImagen);
            if (nombreCarta != null) {
                nombreCartaTextView.setText(nombreCarta);
            } else {
                nombreCartaTextView.setText("No name");
            }
        } else {
            // Si estamos en la primera carta mostrada, no hay retroceso posible
            Toast.makeText(this, "No hay cartas anteriores", Toast.LENGTH_SHORT).show();
        }
    }
    private void RegresarMenu(){
        finish();
        Intent intent = new Intent(BarajearCartasActivity.this, MenuPrincipalActivity.class);
        startActivity(intent);
    }
    MediaPlayer mPlayer;
    public void reproduceMp3(String nombreAudio){

        if(mPlayer != null){
            mPlayer.release(); // Liberar el recurso MediaPlayer
        }
        String nombreValido = nombreAudio.replace("-", "_").replace(".mp3.mp3", ".mp3" ).toLowerCase();
        int filePlay = getResources().getIdentifier(nombreValido , "raw", getPackageName());
        mPlayer = MediaPlayer.create(BarajearCartasActivity.this, filePlay);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //casette.setVisibility(View.INVISIBLE);
            }
        });
        mPlayer.start();
        //casette.setVisibility(View.VISIBLE);
    }


    // Declara un Timer y un TimerTask como variables de clase
    private Timer timer;
    private TimerTask timerTask;
    private boolean isPaused = false;
    // Método para iniciar la actualización automática de imágenes cada 5 segundos
    // Método para iniciar la actualización automática de imágenes cada 5 segundos
    private void iniciarActualizacionAutomatica() {
        if (isPaused) return;  // No iniciar si está en pausa

        // Crea un nuevo Timer
        timer = new Timer();
        // Crea un nuevo TimerTask
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Ejecuta la lógica para mostrar la siguiente imagen
                mostrarSiguienteImagen();
            }
        };

        // Programa el TimerTask para que se ejecute cada 5 segundos (5000 milisegundos)
        timer.schedule(timerTask, 0, 5000);
    }

    // Método para detener la actualización automática de imágenes
    private void detenerActualizacionAutomatica() {
        // Cancela el TimerTask si está en ejecución
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        // Cancela el Timer
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        if(mCountDownTimer != null){
            mCountDownTimer.cancel();
        }
    }

    // Método para iniciar la actualización automática cuando sea necesario
    private void empezarActualizacionAutomatica() {
        // Detén cualquier actualización automática previa antes de comenzar una nueva
        detenerActualizacionAutomatica();
        // Inicia la actualización automática
        iniciarActualizacionAutomatica();
    }

    // Método para pausar la actualización automática
    private void pausarActualizacionAutomatica() {
        isPaused = true;
        detenerActualizacionAutomatica();
    }

    // Método para reanudar la actualización automática
    private void reanudarActualizacionAutomatica() {
        isPaused = false;
        empezarActualizacionAutomatica();
    }

    @Override
    protected void onDestroy() {
        // Detener la reproducción de audio si está en curso
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        // Detener la actualización automática si está en curso
        detenerActualizacionAutomatica();

        // Llamar al método de la superclase para completar el proceso de destrucción
        super.onDestroy();
    }


}



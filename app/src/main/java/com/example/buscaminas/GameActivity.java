package com.example.buscaminas;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class GameActivity extends AppCompatActivity {
    private TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int difficulty = getDificultad();

        timer = findViewById(R.id.timer);
        int totalMines = Logicas.getContadorMinas(difficulty);

        TextView textoMinasTotales = findViewById(R.id.mines_counter);
        textoMinasTotales.setText(getString(R.string.minas_total, totalMines));

        // Creamos la tabla
        int[][] board = Logicas.generarTablero(difficulty);

        TableLayout t = findViewById(R.id.tablero);
        Logicas.crearMatriz(t, difficulty, board, this);

        startTimer(timer);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu_toolbar, m);
        return true;
    }

    //Menu que mostrará el icono del personaje
    @Override
    public boolean onPrepareOptionsMenu(Menu m) {
        // Recupera el índice del personaje seleccionado desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("Ajustes", MODE_PRIVATE);
        int selectedCharacterIndex = prefs.getInt("personajeSeleccionadoIndex", 0); // Default to 0 (Hipo 1)

        // Cargar los datos de personajes
        Object[] characterData = cargarPersonaje(this);
        int[] characterImages = (int[]) characterData[1];

        if (selectedCharacterIndex >= 0 && selectedCharacterIndex < characterImages.length) {
            int selectedCharacterResId = characterImages[selectedCharacterIndex];

            MenuItem selectCharacterItem = m.findItem(R.id.selec_personaje);
            selectCharacterItem.setIcon(ContextCompat.getDrawable(this, selectedCharacterResId));
        }

        return true;
    }

    //diferentes opciones del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.instrucciones) {
            instrucciones();
            return true;
        } else if (itemId == R.id.nuevo_juego) {
            startNewGame();
            return true;
        } else if (itemId == R.id.action_settings) {
            showDifficultySelection();
            return true;
        } else if (itemId == R.id.selec_personaje) {
            menuSeleccion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //metodo para cargar el personaje guardado
    private Object[] cargarPersonaje(Context context) {

        String[] characterNames = getResources().getStringArray(R.array.nombres);

        TypedArray characterImagesArray = getResources().obtainTypedArray(R.array.imagenes);
        int[] characterImages = new int[characterImagesArray.length()];

        for (int i = 0; i < characterImagesArray.length(); i++) {
            characterImages[i] = characterImagesArray.getResourceId(i, -1);
        }

        characterImagesArray.recycle();

        return new Object[]{characterNames, characterImages};
    }

    //metodo para el spinner de personajes
    private void menuSeleccion() {

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.selec_personaje_spinner, null);

        Spinner spinnerCharacter = dialogView.findViewById(R.id.spinner);

        Object[] characterData = cargarPersonaje(this);
        String[] characterNames = (String[]) characterData[0];
        int[] characterImages = (int[]) characterData[1];

        AdaptadorPersonajes adapter = new AdaptadorPersonajes(this, characterNames, characterImages);
        spinnerCharacter.setAdapter(adapter);

        int savedIndex = Logicas.getPersonajeIndex(this);
        spinnerCharacter.setSelection(savedIndex);

        new AlertDialog.Builder(this)
                .setTitle(R.string.selecciona_personaje)
                .setView(dialogView)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    int selectedPosition = spinnerCharacter.getSelectedItemPosition();
                    Logicas.guardarPersonaje(this, characterImages[selectedPosition], selectedPosition);
                    updateToolbarIcon(characterImages[selectedPosition]);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void updateToolbarIcon(int indexPersonaje) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().findItem(R.id.selec_personaje).setIcon(indexPersonaje);
        }
    }

    private void startNewGame() {
        int difficulty = getDificultad();
        TableLayout layoutTablero = findViewById(R.id.tablero);
        Logicas.iniciarContadorMinas(difficulty);
        int[][] board = Logicas.generarTablero(difficulty);
        Logicas.crearMatriz(layoutTablero, difficulty, board, this);

        startTimer(timer);
    }

    private void instrucciones() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.instrucciones)
                .setMessage(R.string.texto_instrucciones)
                .setPositiveButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void setDificultad(RadioButton easy, RadioButton medio, RadioButton dificil, int dificultadActual) {
        if (dificultadActual == 8) {
            easy.setChecked(true);
        } else if (dificultadActual == 10) {
            medio.setChecked(true);
        } else if (dificultadActual == 12) {
            dificil.setChecked(true);
        }
    }

    private void showDifficultySelection() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.difficulty, null);

        RadioGroup tiposDificultad = dialogView.findViewById(R.id.rg_difficulties);
        RadioButton facil = dialogView.findViewById(R.id.rb_easy);
        RadioButton medio = dialogView.findViewById(R.id.rb_medium);
        RadioButton dificil = dialogView.findViewById(R.id.rb_hard);

        int currentDifficulty = getDificultad();
        setDificultad(facil, medio, dificil, currentDifficulty);

        new AlertDialog.Builder(this).setTitle(R.string.difficulty)
                .setView(dialogView)
                .setPositiveButton(R.string.accept, (dialog, which) -> nuevaDificultad(tiposDificultad, currentDifficulty))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private int getDificultad() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        return prefs.getInt("difficulty", 8);
    }

    private void nuevaDificultad(RadioGroup listaDificultades, int dificultadActual) {
        int selectedDifficulty = getSelectedDifficulty(listaDificultades);
        if (selectedDifficulty != dificultadActual) {
            getSharedPreferences("GamePrefs", MODE_PRIVATE).edit().putInt("difficulty", selectedDifficulty).apply();
            recreate();
        }
    }

    private int getSelectedDifficulty(RadioGroup listaDificultades) {
        int checkedRadioButtonId = listaDificultades.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rb_medium) {
            return 10;
        } else if (checkedRadioButtonId == R.id.rb_hard) {
            return 12;
        }
        return 8;
    }

    private void startTimer(TextView timer) {
        final long startTime = System.currentTimeMillis();

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long milisegundos = System.currentTimeMillis() - startTime;
                int seconds = (int) (milisegundos / 1000) % 60;
                int minutes = (int) (milisegundos / 1000) / 60;


                String timeFormatted = String.format(Locale.getDefault(), "Tiempo: %02d:%02d", minutes, seconds);
                timer.setText(timeFormatted);

                //update cada segundo
                handler.postDelayed(this, 1000);
            }
        });
    }


}

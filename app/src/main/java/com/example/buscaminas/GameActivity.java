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
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;
//TODO EL SHARED PREF SE PASAN UN INDEX RARO
//TODO El usuario realiza un click largo (onLongClick) sobre una casilla donde no hay una
//hipotenocha. El juego muestra la casilla descubierta y termina.
//2. El usuario realiza un click largo donde sí hay una hipotenocha, en este caso, se marca y
//se indica que se ha encontrado una hipotenocha.
//3. Se realiza un click corto (onClick) en una
//casilla donde sí hay una hipotenocha. El
//juego termina con derrota mostrando
//una hipotenocha muerta (boca abajo y
//tachada).

public class GameActivity extends AppCompatActivity {
    private TextView timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Get saved difficulty
        int difficulty = getSavedDifficulty();

        // Set up the GridLayout for the minesweeper grid
        GridLayout gridMinesweeper = findViewById(R.id.grid_minesweeper);
        gridMinesweeper.setColumnCount(difficulty);
        gridMinesweeper.setRowCount(difficulty);


        timer = findViewById(R.id.timer);
        int totalMines = Logicas.getContadorMinas(difficulty);

        // Inicializar el TextView para mostrar las minas totales
        TextView totalMinesTextView = findViewById(R.id.mines_counter);
        totalMinesTextView.setText(getString(R.string.minas_total, totalMines));

        // Create the game board
        int[][] board = Logicas.generateBoard(difficulty);

        Logicas.crearMatriz(gridMinesweeper, difficulty, board);



        startTimer(timer);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Recupera el índice del personaje seleccionado desde SharedPreferences
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        int selectedCharacterIndex = prefs.getInt("selectedCharacterIndex", 0); // Default to 0 (Hipo 1)

        // Cargar los datos de personajes
        Object[] characterData = loadCharacterData(this);
        String[] characterNames = (String[]) characterData[0];
        int[] characterImages = (int[]) characterData[1];

        // Asegúrate de que el índice no esté fuera de rango
        if (selectedCharacterIndex >= 0 && selectedCharacterIndex < characterImages.length) {
            int selectedCharacterResId = characterImages[selectedCharacterIndex];

            // Actualiza el icono del personaje en el menú
            MenuItem selectCharacterItem = menu.findItem(R.id.selec_personaje);
            selectCharacterItem.setIcon(selectedCharacterResId);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.instrucciones) {
            showInstructionsDialog();
            return true;
        } else if (itemId == R.id.nuevo_juego) {
            startNewGame();
            return true;
        } else if (itemId == R.id.action_settings) {
            showDifficultySelection();
            return true;
        } else if (itemId == R.id.selec_personaje) {
            showCharacterSelectionDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Object[] loadCharacterData(Context context) {
        // Cargar los nombres de los personajes desde arrays.xml
        String[] characterNames = getResources().getStringArray(R.array.nombres);

        // Cargar las imágenes de los personajes desde arrays.xml
        TypedArray characterImagesArray = getResources().obtainTypedArray(R.array.imagenes);
        int[] characterImages = new int[characterImagesArray.length()];

        // Llenar el arreglo de imágenes con los recursos de tipo drawable
        for (int i = 0; i < characterImagesArray.length(); i++) {
            characterImages[i] = characterImagesArray.getResourceId(i, -1);
        }

        // Liberar los recursos del TypedArray
        characterImagesArray.recycle();

        // Devolver los datos como un arreglo de objetos
        return new Object[]{characterNames, characterImages};
    }


    private void showCharacterSelectionDialog() {
        // Inflar el layout personalizado
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.selec_personaje_spinner, null);

        // Referencia al Spinner
        Spinner spinnerCharacter = dialogView.findViewById(R.id.spinner);

        // Cargar los datos de personajes
        Object[] characterData = loadCharacterData(this);
        String[] characterNames = (String[]) characterData[0];
        int[] characterImages = (int[]) characterData[1];

        // Configurar el adaptador para el Spinner
        AdaptadorPersonajes adapter = new AdaptadorPersonajes(this, characterNames, characterImages);
        spinnerCharacter.setAdapter(adapter);

        // Cargar el índice de personaje previamente seleccionado
        int savedIndex = Logicas.getSelectedCharacterIndex(this);
        spinnerCharacter.setSelection(savedIndex);

        // Mostrar el diálogo de selección
        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu personaje")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    int selectedPosition = spinnerCharacter.getSelectedItemPosition();
                    Logicas.saveSelectedCharacter(this, characterImages[selectedPosition], selectedPosition);
                    updateToolbarIcon(characterImages[selectedPosition]);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }


    private void updateToolbarIcon(int characterResId) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.getMenu().findItem(R.id.selec_personaje).setIcon(characterResId);
        }
    }



    private void startNewGame() {
        // Reiniciar el juego sin recrear la actividad
        int difficulty = getSavedDifficulty();
        GridLayout gridMinesweeper = findViewById(R.id.grid_minesweeper);

        // Reiniciar lógica y matriz
        Logicas.iniciarContadorMinas(difficulty);
        int[][] board = Logicas.generateBoard(difficulty);
        Logicas.crearMatriz(gridMinesweeper, difficulty, board);


        startTimer(timer);
    }

    private void showInstructionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.instrucciones)
                .setMessage(R.string.texto_instrucciones)
                .setPositiveButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void setCheckedDifficulty(RadioButton rbEasy, RadioButton rbMedium, RadioButton rbHard, int currentDifficulty) {
        if (currentDifficulty == 8) {
            rbEasy.setChecked(true);
        } else if (currentDifficulty == 10) {
            rbMedium.setChecked(true);
        } else if (currentDifficulty == 12) {
            rbHard.setChecked(true);
        }
    }

    private void showDifficultySelection() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.difficulty, null);

        RadioGroup rgDifficulties = dialogView.findViewById(R.id.rg_difficulties);
        RadioButton rbEasy = dialogView.findViewById(R.id.rb_easy);
        RadioButton rbMedium = dialogView.findViewById(R.id.rb_medium);
        RadioButton rbHard = dialogView.findViewById(R.id.rb_hard);

        int currentDifficulty = getSavedDifficulty();
        setCheckedDifficulty(rbEasy, rbMedium, rbHard, currentDifficulty);

        new AlertDialog.Builder(this).setTitle(R.string.difficulty)
                .setView(dialogView)
                .setPositiveButton(R.string.accept, (dialog, which) -> saveNewDifficulty(rgDifficulties, currentDifficulty))
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    private int getSavedDifficulty() {
        SharedPreferences prefs = getSharedPreferences("GamePrefs", MODE_PRIVATE);
        return prefs.getInt("difficulty", 8); // Default to 8x8 if not saved
    }

    private void saveNewDifficulty(RadioGroup rgDifficulties, int currentDifficulty) {
        int selectedDifficulty = getSelectedDifficulty(rgDifficulties);
        if (selectedDifficulty != currentDifficulty) {
            getSharedPreferences("GamePrefs", MODE_PRIVATE).edit().putInt("difficulty", selectedDifficulty).apply();
            recreate();
        }
    }

    private int getSelectedDifficulty(RadioGroup rgDifficulties) {
        int checkedRadioButtonId = rgDifficulties.getCheckedRadioButtonId();
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
                long elapsedMillis = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedMillis / 1000) % 60;
                int minutes = (int) (elapsedMillis / 1000) / 60;

                //TODO
                String timeFormatted = String.format(Locale.getDefault(), "Tiempo: %02d:%02d", minutes, seconds);
                timer.setText(timeFormatted);

                handler.postDelayed(this, 1000); // Update every second
            }
        });
    }


}

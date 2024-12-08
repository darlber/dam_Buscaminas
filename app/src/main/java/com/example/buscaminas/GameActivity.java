package com.example.buscaminas;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

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

        // Inicializar el TextView para mostrar las minas totales
        TextView totalMinesTextView = findViewById(R.id.mines_counter);

        // Obtener el número de minas según la dificultad
        int totalMines = Logicas.contadorMinas(difficulty);

        // Actualizar el texto del total de minas
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
            selectCharacter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startNewGame() {
        // Reiniciar el juego sin recrear la actividad
        int difficulty = getSavedDifficulty();
        GridLayout gridMinesweeper = findViewById(R.id.grid_minesweeper);

        // Reiniciar lógica y matriz
        Logicas.initializeMineCounter(difficulty);
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
                String timeFormatted = String.format(Locale.getDefault(), "Tiempo: %02d:%02d", minutes, seconds);                timer.setText(timeFormatted);

                handler.postDelayed(this, 1000); // Update every second
            }
        });
    }

    private void selectCharacter() {
        Toast.makeText(this, "Seleccionar personaje no implementado aún", Toast.LENGTH_SHORT).show();
    }
}

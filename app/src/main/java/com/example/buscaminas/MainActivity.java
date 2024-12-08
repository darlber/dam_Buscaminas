package com.example.buscaminas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE);

        Button btnInstructions = findViewById(R.id.btn_instructions);
        btnInstructions.setOnClickListener(v -> showInstructionsDialog());

        Button btnConfigureGame = findViewById(R.id.btn_configure_game);
        btnConfigureGame.setOnClickListener(v -> showDifficultyDialog(this));

        Button btnNewGame = findViewById(R.id.btn_new_game);
        btnNewGame.setOnClickListener(v -> openGameActivity());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void openGameActivity() {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        startActivity(intent);
    }

    private void showInstructionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.instrucciones)
                .setMessage(getString(R.string.texto_instrucciones))
                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }

    private void showDifficultyDialog(Context context) {
        // Inflar el diseño del diálogo
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.difficulty, null);

        // Obtener referencias a los RadioButtons
        RadioGroup rgDifficulties = dialogView.findViewById(R.id.rg_difficulties);
        RadioButton rbEasy = dialogView.findViewById(R.id.rb_easy);
        RadioButton rbMedium = dialogView.findViewById(R.id.rb_medium);
        RadioButton rbHard = dialogView.findViewById(R.id.rb_hard);

        int currentDifficulty = getCurrentDifficulty();
        if (currentDifficulty == 8) {
            rbEasy.setChecked(true);
        } else if (currentDifficulty == 10) {
            rbMedium.setChecked(true);
        } else if (currentDifficulty == 12) {
            rbHard.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.difficulty)
                .setView(dialogView)
                .setPositiveButton(R.string.accept, (dialog, which) -> {

                    int selectedDifficulty = 8; // Default
                    if (rbMedium.isChecked()) {
                        selectedDifficulty = 10;
                    } else if (rbHard.isChecked()) {
                        selectedDifficulty = 12;
                    }
                    prefs.edit().putInt("difficulty", selectedDifficulty).apply();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private int getCurrentDifficulty() {
        return prefs.getInt("difficulty", 8);
    }

    private void setDifficulty(int difficulty) {
        prefs.edit().putInt("difficulty", difficulty).apply();
    }
}

//TODO
//2 cambiar colores e iconos
//3 refactorizar y comentar


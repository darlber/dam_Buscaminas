package com.example.buscaminas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

//TODO REVISAR CODIGO Y REFACTORIZAR,
//TODO CAMBIAR NOMBRES HIPOS

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

        Button btnSelectCharacter = findViewById(R.id.btn_select_character);
        btnSelectCharacter.setOnClickListener(v -> selectCharacter());

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
        dialogView.findViewById(R.id.rg_difficulties);
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

    private int getSelectedCharacterIndex() {
        return Logicas.getSelectedCharacterIndex(this);
    }

    private void selectCharacter() {
        // Inflar el layout personalizado
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.selec_personaje_spinner, null);

        // Referencia al Spinner
        Spinner spinnerCharacter = dialogView.findViewById(R.id.spinner);

        // Lista de nombres y recursos desde arrays.xml
        String[] characterNames = getResources().getStringArray(R.array.nombres);
        TypedArray characterImages = getResources().obtainTypedArray(R.array.imagenes);

        // Cargar el personaje previamente seleccionado de SharedPreferences
        int savedCharacterIndex = getSelectedCharacterIndex();  // Método para obtener el índice guardado
        String savedCharacterName = characterNames[savedCharacterIndex];
        int savedCharacterImage = characterImages.getResourceId(savedCharacterIndex, -1);

        // Crear una lista modificada para que el personaje seleccionado aparezca en la parte superior
        List<String> modifiedCharacterNames = new ArrayList<>();
        List<Integer> modifiedCharacterImages = new ArrayList<>();

        // Agregar el personaje seleccionado en el top
        modifiedCharacterNames.add(savedCharacterName);
        modifiedCharacterImages.add(savedCharacterImage);

        // Agregar el resto de los personajes (excepto el que ya se ha agregado)
        for (int i = 0; i < characterNames.length; i++) {
            if (i != savedCharacterIndex) {
                modifiedCharacterNames.add(characterNames[i]);
                modifiedCharacterImages.add(characterImages.getResourceId(i, -1));
            }
        }

        // Crear el nuevo adaptador con la lista modificada
        AdaptadorPersonajes updatedAdapter = new AdaptadorPersonajes(this,
                modifiedCharacterNames.toArray(new String[0]),
                modifiedCharacterImages.stream().mapToInt(Integer::intValue).toArray());

        // Establecer el adaptador al Spinner
        spinnerCharacter.setAdapter(updatedAdapter);

        // Mostrar el diálogo con el spinner
        new AlertDialog.Builder(this)
                .setTitle("Selecciona tu personaje")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    int selectedPosition = spinnerCharacter.getSelectedItemPosition();

                    // Guardar el personaje seleccionado para la próxima vez
                    Logicas.saveSelectedCharacter(this,
                            modifiedCharacterImages.get(selectedPosition),
                            selectedPosition);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();

        // Liberar los recursos de TypedArray
        characterImages.recycle();
    }
}


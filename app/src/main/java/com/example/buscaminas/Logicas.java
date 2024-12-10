package com.example.buscaminas;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.Random;

public class Logicas {

    private static int minasRestantes;
    private static final int MINA = -1;

    static int encontradas = 0;
    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_SELECTED_CHARACTER = "selectedCharacter";
    private static final String KEY_SELECTED_CHARACTER_INDEX = "selectedCharacterIndex";
    static TableLayout tableLayout;

    // Método para inicializar el número de minas restantes según la dificultad
    protected static void iniciarContadorMinas(int difficulty) {
        minasRestantes = getContadorMinas(difficulty); // Establece las minas restantes basadas en la dificultad
    }

    // Método estático para crear la matriz de botones en el GridLayout
    protected static void crearMatriz(TableLayout tableLayout, int difficulty, int[][] board, Context context) {

        tableLayout.removeAllViews();

        // Crear filas y columnas para la cuadrícula
        for (int row = 0; row < difficulty; row++) {
            TableRow tableRow = new TableRow(context);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    0 // Altura de la fila (usamos layout_weight para que las filas ocupen espacio dinámicamente)
            );
            rowParams.weight = 1.0f; // Esto asegura que las filas se distribuyan equitativamente y ocupen el espacio disponible
            tableRow.setLayoutParams(rowParams);

            for (int col = 0; col < difficulty; col++) {
                // Crear un botón normal para cada celda
                Button cellButton = new Button(context);
                cellButton.setText(""); // Puedes poner un texto vacío o cualquier otro texto
                cellButton.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
                cellButton.setClickable(true);
                cellButton.setFocusable(false);

                // Configurar el layout de cada botón dentro de la fila
                TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                        0, // Ancho dinámico
                        TableRow.LayoutParams.MATCH_PARENT, // Alto completo para la fila
                        1.0f // Peso para distribuir igualmente el espacio entre las celdas
                );
                cellParams.setMargins(4, 4, 4, 4); // Márgenes opcionales, si los deseas
                cellButton.setLayoutParams(cellParams);

                // Lógica de clic
                int finalRow = row;
                int finalCol = col;
                cellButton.setOnClickListener(v -> {
                    revealCell(tableLayout, finalRow, finalCol, board, difficulty, context);
                });

                // Lógica de clic largo
                cellButton.setOnLongClickListener(v -> {
                    marcarComoMina(tableLayout, difficulty, finalRow, finalCol, board, context);
                    return true; // Consumir el evento
                });

                // Añadir el botón a la fila
                tableRow.addView(cellButton);
            }

            // Añadir la fila al TableLayout
            tableLayout.addView(tableRow);
        }
    }


    // Método estático para colocar las minas aleatoriamente en la matriz
    protected static void placeMines(int[][] board, int difficulty) {
        int numMines = getContadorMinas(difficulty);  // Obtenemos la cantidad de minas según la dificultad
        Random rand = new Random();
        int placedMines = 0;

        // Colocamos las minas aleatoriamente
        while (placedMines < numMines) {
            int row = rand.nextInt(difficulty);
            int col = rand.nextInt(difficulty);

            // Colocamos la mina en la celda si aún no hay una mina allí
            if (board[row][col] != MINA) {
                board[row][col] = -1; // Colocamos una mina
                placedMines++;
            }
        }

        // Ahora actualizamos el número de minas adyacentes para cada celda (excepto donde hay una mina)
        contarMinasAdyacentes(board, difficulty);

    }

    // Método para generar el tablero con celdas vacías y minas
    protected static int[][] generateBoard(int difficulty) {
        // Crear una matriz de celdas vacías
        int[][] board = new int[difficulty][difficulty];

        // Colocar minas aleatoriamente
        placeMines(board, difficulty);

        return board;
    }

    // Método para contar el número de minas según la dificultad
    protected static int getContadorMinas(int difficulty) {
        switch (difficulty) {
            case 8:  // Fácil
                return 10;
            case 10: // Medio
                return 30;
            case 12: // Difícil
                return 60;
            default:
                return 10;  // Fallback en caso de error, por ejemplo, 10 minas
        }
    }

    private static void contarMinasAdyacentes(int[][] gameBoard, int difficulty) {
        for (int row = 0; row < difficulty; row++) {
            for (int col = 0; col < difficulty; col++) {
                if (gameBoard[row][col] == MINA) continue;  // Saltamos las minas

                int adjacentMines = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int neighborRow = row + i;
                        int neighborCol = col + j;

                        if (esValido(neighborRow, neighborCol, difficulty) &&
                                gameBoard[neighborRow][neighborCol] == MINA) {
                            adjacentMines++;
                        }
                    }
                }

                gameBoard[row][col] = adjacentMines;  // Asignar el número de minas adyacentes
            }
        }
    }

    private static boolean esValido(int row, int col, int difficulty) {
        return row >= 0 && row < difficulty && col >= 0 && col < difficulty;
    }

    private static void revealCell(TableLayout tableLayout, int row, int col, int[][] board, int difficulty, Context context) {
        // Verificar si la celda está dentro del rango válido
        if (!esValido(row, col, difficulty)) return;

        // Obtener la fila correspondiente del TableLayout
        TableRow tableRow = (TableRow) tableLayout.getChildAt(row);
        // Obtener el botón correspondiente dentro de la fila
        Button cellButton = (Button) tableRow.getChildAt(col);

        // Verificar si la celda ya ha sido revelada (esto previene que se revele varias veces)
        if (!cellButton.getText().toString().isEmpty()) return;

        // Si la celda tiene una mina, revelar todas las celdas y mostrar el mensaje de derrota
        if (board[row][col] == MINA) {

            revealAllCells(tableLayout, board, difficulty, context);
            showGameOverDialog(context);
        } else if (board[row][col] == 0) {
            // Si no hay minas adyacentes, revelar las celdas adyacentes de manera recursiva
            revelarAdyacentes(tableLayout, row, col, board, difficulty);
        } else {
            // Si la celda tiene un número de minas adyacentes, mostrar el número
            cellButton.setText(String.valueOf(board[row][col])); // Mostrar el número de minas adyacentes
            cellButton.setBackgroundColor(tableLayout.getContext().getResources().getColor(R.color.colorRevealedCell, null));

        }
        disableButton(cellButton);
    }

    // Método recursivo para revelar las celdas adyacentes (si son 0)
    private static void revelarAdyacentes(TableLayout g, int row, int col, int[][] board, int difficulty) {
        // Verificar si la celda está dentro del rango válido
        if (!esValido(row, col, difficulty)) return;

        // Obtener la fila correspondiente del TableLayout
        TableRow tableRow = (TableRow) g.getChildAt(row);

        // Obtener el botón correspondiente dentro de la fila
        Button cellButton = (Button) tableRow.getChildAt(col);

        // Verificar si la celda ya ha sido revelada (esto previene que se revele varias veces)
        if (cellButton == null || !cellButton.getText().toString().isEmpty() || board[row][col] == Integer.MIN_VALUE)
            return;

        int cellValue = board[row][col];
        if (cellValue == MINA) {
            disableButton(cellButton);
            return;
        }

        if (cellValue > 0) {
            cellButton.setText(String.valueOf(cellValue));
            cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));

        } else {
            cellButton.setText("");
            cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
            board[row][col] = Integer.MIN_VALUE;  // Marcar la celda como revelada

            disableButton(cellButton);

            // Llamar recursivamente a las celdas adyacentes si no tiene minas adyacentes
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    // Asegúrate de no llamar a la celda actual
                    if (i != 0 || j != 0) {
                        revelarAdyacentes(g, row + i, col + j, board, difficulty);
                    }
                }
            }
        }


    }


    private static void revealAllCells(TableLayout tableLayout, int[][] board, int difficulty, Context context) {
        for (int row = 0; row < difficulty; row++) {
            // Obtener la fila correspondiente en TableLayout
            TableRow tableRow = (TableRow) tableLayout.getChildAt(row);
            int selectedCharacter = getSelectedCharacterResId(context);

            Drawable drawable = AppCompatResources.getDrawable(context, selectedCharacter);

            for (int col = 0; col < difficulty; col++) {
                // Obtener el botón correspondiente dentro de la fila
                Button cellButton = (Button) tableRow.getChildAt(col);

                if (board[row][col] == MINA) {
                    cellButton.setPadding(0, 0, 0, 0);
                    cellButton.setTextColor(Color.RED);
                    cellButton.setText("X");
                    cellButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                    cellButton.setBackground(drawable);
                    cellButton.setScaleY(-1);
                    // Verificar si el jugador ha ganado


                } else {
                    if (board[row][col] > 0) {
                        cellButton.setText(String.valueOf(board[row][col]));
                    }
                    cellButton.setBackgroundColor(tableLayout.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                }


                cellButton.setOnClickListener(null);
                cellButton.setOnLongClickListener(null);
            }
        }
    }

    private static void marcarComoMina(TableLayout tableLayout, int difficulty, int row, int col, int[][] board, Context context) {
        // Obtener la fila correspondiente del TableLayout
        TableRow tableRow = (TableRow) tableLayout.getChildAt(row);

        // Obtener el botón correspondiente dentro de la fila
        Button cellButton = (Button) tableRow.getChildAt(col);

        // Verificar si en esa casilla hay una mina
        if (board[row][col] == MINA) {
            // Obtener el ID del personaje seleccionado (imagen)
            int selectedCharacter = getSelectedCharacterResId(context);


            // Crear un Drawable para la imagen
            Drawable drawable = AppCompatResources.getDrawable(context, selectedCharacter);

            assert drawable != null;
            drawable.setBounds(0, 0, 80, 80); // Ajusta el tamaño de la imagen dentro del botón

            // Crear un ImageSpan con la imagen
            ImageSpan imageSpan = new ImageSpan(drawable);

            // Establecer la imagen en el texto del botón
            SpannableString spannableString = new SpannableString(" ");  // Puedes agregar cualquier texto aquí si lo deseas
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            cellButton.setText(spannableString);
            cellButton.setBackgroundColor(context.getResources().getColor(R.color.colorBackground, null));
            disableButton(cellButton);

            encontradas++;
            if (encontradas == getContadorMinas(difficulty)) {
                showWinningDialog(tableLayout.getContext());
            }

        } else {
            // Si no es una mina, marcar la casilla con "M" o el valor que prefieras
            cellButton.setBackgroundColor(context.getResources().getColor(R.color.colorBackground, null));  // Cambiar fondo a color de fondo
            cellButton.setText("M");  // Marcar la casilla con la letra "M" o el valor que prefieras
            disableButton(cellButton);
        }

    }


    private static void showGameOverDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¡Has Perdido!")
                .setMessage("¡Lo siento! Has hecho clic en una mina.")
                .setPositiveButton("Reintentar", (dialog, which) -> {
                    encontradas = 0;
                    ((GameActivity) context).recreate();
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    ((GameActivity) context).finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private static void showWinningDialog(Context context) {
        encontradas = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¡Has Ganado!")
                .setMessage("Has revelado todas las casillas sin minas.")
                .setPositiveButton("Volver a Jugar", (dialog, which) -> {
                    ((GameActivity) context).recreate();
                })
                .setNegativeButton("Salir", (dialog, which) -> {
                    ((GameActivity) context).finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }


    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveSelectedCharacter(Context context, int characterResId, int index) {
        SharedPreferences prefs = getPreferences(context);
        prefs.edit()
                .putInt(KEY_SELECTED_CHARACTER, characterResId)
                .putInt(KEY_SELECTED_CHARACTER_INDEX, index)
                .apply();
    }

    public static int getSelectedCharacterIndex(Context context) {
        return getPreferences(context).getInt(KEY_SELECTED_CHARACTER_INDEX, 0); // Predeterminado: 0
    }

    public static int getSelectedCharacterResId(Context context) {
        return getPreferences(context).getInt(KEY_SELECTED_CHARACTER, R.drawable.hipo1); // Predeterminado: hipo1
    }

    private static void disableButton(Button button) {
        button.setClickable(false); // Deshabilitar clic en el botón
        button.setFocusable(false); // Deshabilitar enfoque
        button.setOnClickListener(null);
        button.setOnLongClickListener(null);
    }
}

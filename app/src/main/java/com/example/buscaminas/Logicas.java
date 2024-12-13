package com.example.buscaminas;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AlertDialog;

import java.util.Random;

public class Logicas {

    private static int minasRestantes;
    private static final int MINA = -1;

    static int encontradas = 0;
    private static final String AJUSTES = "Ajustes";
    private static final String PERSONAJESELECCIONADO = "personajeSeleccionado";
    private static final String INDEXPERSONAJE = "personajeSeleccionadoIndex";


    // Método para inicializar el número de minas restantes según la dificultad
    protected static void iniciarContadorMinas(int difficulty) {
        minasRestantes = getContadorMinas(difficulty); // Establece las minas restantes basadas en la dificultad
    }

    protected static void crearMatriz(TableLayout tableLayout, int difficulty, int[][] board, Context context) {
        //limpia las anteriores vistas, para poder hacer un cambio de dificultad dinamico
        tableLayout.removeAllViews();

        for (int row = 0; row < difficulty; row++) {
            TableRow tableRow = new TableRow(context);
            TableLayout.LayoutParams rowParams = new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    0
            );
            rowParams.weight = 1.0f;
            tableRow.setLayoutParams(rowParams);

            for (int col = 0; col < difficulty; col++) {
                View cellView;

                // Si la celda tiene mina, usa ImageButton
                if (board[row][col] == MINA) {
                    cellView = new ImageButton(context);

                } else {
                    cellView = new Button(context);
                    ((Button) cellView).setText("");
                }

                cellView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary, null));
                cellView.setClickable(true);
                cellView.setFocusable(false);

                TableRow.LayoutParams cellParams = new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.MATCH_PARENT,
                        1.0f
                );
                cellParams.setMargins(4, 4, 4, 4);
                cellView.setLayoutParams(cellParams);

                // Lógica de clic
                int finalRow = row;
                int finalCol = col;
                cellView.setOnClickListener(v -> revealCell(tableLayout, finalRow, finalCol, board, difficulty, context));

                // Lógica de clic largo
                cellView.setOnLongClickListener(v -> {
                    marcarComoMina(tableLayout, difficulty, finalRow, finalCol, board, context);
                    return true;
                });

                tableRow.addView(cellView);
            }

            tableLayout.addView(tableRow);
        }
    }

    protected static void placeMines(int[][] board, int difficulty) {
        int numMines = getContadorMinas(difficulty);
        Random rand = new Random();
        int placedMines = 0;

        while (placedMines < numMines) {
            int row = rand.nextInt(difficulty);
            int col = rand.nextInt(difficulty);

            if (board[row][col] != MINA) {
                board[row][col] = MINA; // Colocamos una mina
                placedMines++;
            }
        }

        contarMinasAdyacentes(board, difficulty);

    }

    protected static int[][] generarTablero(int difficulty) {
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
        if (!esValido(row, col, difficulty)) return;

        TableRow tableRow = (TableRow) tableLayout.getChildAt(row);
        // Obtener la vista correspondiente dentro de la fila (puede ser un Button o un ImageButton)
        View cellView = tableRow.getChildAt(col);

        if (board[row][col] == MINA) {

            revealAllCells(tableLayout, board, difficulty, context);
            gameOver(context);

        } else if (board[row][col] == 0) {

            revelarAdyacentes(tableLayout, row, col, board, difficulty);
        } else {

            //en caso de que la celda pulsada sea button o imagebutton
            if (cellView instanceof Button) {
                Button cellButton = (Button) cellView;
                //mostramos las minas adyacentes
                cellButton.setText(String.valueOf(board[row][col]));
                cellButton.setBackgroundColor(tableLayout.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                //deshabilitamos
                disableButton(cellButton);
            } else if (cellView instanceof ImageButton) {
                ImageButton cellImageButton = (ImageButton) cellView;
                cellImageButton.setBackgroundColor(tableLayout.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                disableButton(cellImageButton);
            }
        }
    }

    //metodo recursivo
    private static void revelarAdyacentes(TableLayout g, int row, int col, int[][] board, int difficulty) {

        if (!esValido(row, col, difficulty)) return;

        TableRow tableRow = (TableRow) g.getChildAt(row);
        Button cellButton = (Button) tableRow.getChildAt(col);

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

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) {
                        revelarAdyacentes(g, row + i, col + j, board, difficulty);
                    }
                }
            }
        }
    }

    private static void revealAllCells(TableLayout tableLayout, int[][] board, int difficulty, Context context) {
        for (int row = 0; row < difficulty; row++) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(row);

            for (int col = 0; col < difficulty; col++) {
                View cellView = tableRow.getChildAt(col);

                if (cellView instanceof ImageButton) {
                    ImageButton cellImageButton = (ImageButton) cellView;

                    if (board[row][col] == MINA) {

                        cellImageButton.setPadding(0, 0, 0, 0);
                        cellImageButton.setBackgroundColor(Color.RED);
                        int selectedCharacter = getPersonajeIDResources(context);
                        cellImageButton.setImageResource(selectedCharacter);
                        cellImageButton.setScaleY(-1);

                    } else {
                        if (board[row][col] > 0) {
                            // Mostrar el número de minas adyacentes
                            cellImageButton.setContentDescription(String.valueOf(board[row][col]));
                            cellImageButton.setBackgroundColor(tableLayout.getContext().getResources().getColor(R.color.colorRevealedCell, null));

                        } else {
                            // 0 minas adyacentes
                            cellImageButton.setBackgroundColor(tableLayout.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                        }
                    }
                    disableButton(cellImageButton);
                }
            }
        }
    }

    private static void marcarComoMina(TableLayout tableLayout, int difficulty, int row, int col, int[][] board, Context context) {

        TableRow tableRow = (TableRow) tableLayout.getChildAt(row);
        View cellView = tableRow.getChildAt(col);

        if (cellView instanceof ImageButton) {
            ImageButton cellImageButton = (ImageButton) cellView;

            cellImageButton.setBackgroundColor(context.getResources().getColor(R.color.colorBackground, null));

            int selectedCharacter = getPersonajeIDResources(context);
            cellImageButton.setImageResource(selectedCharacter);
            disableButton(cellImageButton);

            encontradas++;
            if (encontradas == getContadorMinas(difficulty)) {
                ganarJuego(tableLayout.getContext());
            }

        } else if (cellView instanceof Button) {

            Button cellButton = (Button) cellView;

            if (!cellButton.getText().toString().isEmpty() && cellButton.getText().toString().equals("M")) {
                return;
            }

            cellButton.setBackgroundColor(context.getResources().getColor(R.color.colorBackground, null));
            cellButton.setText("M");

            disableButton(cellButton);
        }
    }

    private static void gameOver(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.perder)
                .setMessage(R.string.perder_sub)
                .setPositiveButton(R.string.retry, (dialog, which) -> {
                    encontradas = 0;
                    ((GameActivity) context).recreate();
                })
                .setNegativeButton(R.string.salir, (dialog, which) -> {
                    ((GameActivity) context).finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }

    private static void ganarJuego(Context context) {
        encontradas = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.ganar)
                .setMessage(R.string.ganar_sub)
                .setPositiveButton(R.string.volver, (dialog, which) -> {
                    ((GameActivity) context).recreate();
                })
                .setNegativeButton(R.string.salir, (dialog, which) -> {
                    ((GameActivity) context).finish();
                })
                .setCancelable(false)
                .create()
                .show();
    }


    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(AJUSTES, Context.MODE_PRIVATE);
    }

    public static void guardarPersonaje(Context context, int characterResId, int index) {
        SharedPreferences prefs = getPreferences(context);
        prefs.edit()
                .putInt(PERSONAJESELECCIONADO, characterResId)
                .putInt(INDEXPERSONAJE, index)
                .apply();
    }

    public static int getPersonajeIndex(Context context) {
        return getPreferences(context).getInt(INDEXPERSONAJE, 0); // Predeterminado: 0
    }

    public static int getPersonajeIDResources(Context context) {
        return getPreferences(context).getInt(PERSONAJESELECCIONADO, R.drawable.hipo1); // Predeterminado: hipo1
    }

    //sobrecarga para iamgebutton y buttons
    private static void disableButton(Button button) {
        button.setClickable(false); // Deshabilitar clic en el botón
        button.setFocusable(false); // Deshabilitar enfoque
        button.setOnClickListener(null);
        button.setOnLongClickListener(null);
    }

    private static void disableButton(ImageButton button) {
        button.setClickable(false);
        button.setFocusable(false);
        button.setOnClickListener(null);
        button.setOnLongClickListener(null);
    }
}
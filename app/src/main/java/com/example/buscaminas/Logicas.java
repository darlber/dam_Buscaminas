package com.example.buscaminas;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Button;
import android.widget.GridLayout;

import androidx.appcompat.app.AlertDialog;

import java.util.Random;

public class Logicas {

    private static int minasRestantes;
    private static final int MINA = -1;
    private static int revealedCells = 0;  // Contador de celdas reveladas
    private static int totalCellsWithoutMines;  // Número total de celdas sin minas

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_SELECTED_CHARACTER = "selectedCharacter";
    private static final String KEY_SELECTED_CHARACTER_INDEX = "selectedCharacterIndex";


    // Método para inicializar el número de minas restantes según la dificultad
    protected static void iniciarContadorMinas(int difficulty) {
        minasRestantes = getContadorMinas(difficulty); // Establece las minas restantes basadas en la dificultad
    }

    // Método estático para crear la matriz de botones en el GridLayout
    protected static void crearMatriz(GridLayout g, int difficulty, int[][] board) {
        // Limpiar cualquier vista previa
        g.removeAllViews();

        // Crear botones para cada celda en la cuadrícula
        for (int row = 0; row < difficulty; row++) {
            for (int col = 0; col < difficulty; col++) {
                Button cellButton = new Button(g.getContext()); // Necesitamos el contexto aquí

                // Configurar propiedades del botón
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = 0;
                params.columnSpec = GridLayout.spec(col, 1f);
                params.rowSpec = GridLayout.spec(row, 1f);
                params.setMargins(4, 4, 4, 4); // Margen entre botones

                cellButton.setLayoutParams(params);
                cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorPrimary, null));


                // Agregar la lógica del botón (puedes implementar los clicks de las celdas más tarde)
                int finalRow = row;
                int finalCol = col;

                cellButton.setOnClickListener(v -> {
                    revealCell(g, finalRow, finalCol, board, difficulty, g.getContext());
                });

                cellButton.setOnLongClickListener(v -> {
                    // Lógica de clic largo
                    markCellAsMine(g, finalRow, finalCol);
                    return true; // Consumir el evento
                });

                // Añadir el botón al GridLayout
                g.addView(cellButton);
            }
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
        totalCellsWithoutMines = (difficulty * difficulty) - placedMines;


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

    private static void revealCell(GridLayout g, int row, int col, int[][] board, int difficulty, Context context) {
        if (!esValido(row, col, difficulty)) return;


        int index = row * difficulty + col;
        if (!esIndiceValido(index, g.getChildCount())) return;

        Button cellButton = (Button) g.getChildAt(index);
        if (cellButton == null || !cellButton.getText().toString().isEmpty()) return;

        if (board[row][col] == MINA) {
            revealAllCells(g, board, difficulty);
            showGameOverDialog(context);
        } else if (board[row][col] == 0) {
            revelarAdyacentes(g, row, col, board, difficulty);
        } else {
            cellButton.setText(String.valueOf(board[row][col]));
            cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
            revealedCells++;
        }

        if (revealedCells == totalCellsWithoutMines) {
            showWinningDialog(context);
        }
    }

    private static boolean esIndiceValido(int index, int count) {
        return index >= 0 && index < count;
    }

    // Método recursivo para revelar las celdas adyacentes (si son 0)
    private static void revelarAdyacentes(GridLayout g, int row, int col, int[][] board, int difficulty) {
            if (!esValido(row, col, difficulty)) return;

            int index = row * difficulty + col;
            if (!esIndiceValido(index, g.getChildCount())) return;

            Button cellButton = (Button) g.getChildAt(index);
            if (cellButton == null || !cellButton.getText().toString().isEmpty() || board[row][col] == Integer.MIN_VALUE) return;

            int cellValue = board[row][col];
            if (cellValue == MINA) return;

            if (cellValue > 0) {
                cellButton.setText(String.valueOf(cellValue));
                cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                revealedCells++;
            } else {
                cellButton.setText("");
                cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                board[row][col] = Integer.MIN_VALUE;
                revealedCells++;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (i != 0 || j != 0) {
                            revelarAdyacentes(g, row + i, col + j, board, difficulty);
                        }
                    }
                }
            }

            if (revealedCells == totalCellsWithoutMines) {
                showWinningDialog(g.getContext());
            }
        }


        //TODO CAMBIAR MINAS POR HIPOTENOCHA MIA
    private static void revealAllCells(GridLayout g, int[][] board, int difficulty) {
        for (int row = 0; row < difficulty; row++) {
            for (int col = 0; col < difficulty; col++) {
                Button cellButton = (Button) g.getChildAt(row * difficulty + col);

                if (board[row][col] == MINA) {
                    cellButton.setText("💣");
                    cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorAccent, null));
                } else {
                    if (board[row][col] > 0) {
                        cellButton.setText(String.valueOf(board[row][col]));
                    }
                    cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                }

                cellButton.setOnClickListener(null);
                cellButton.setOnLongClickListener(null);
            }
        }
    }


    private static void markCellAsMine(GridLayout g, int row, int col) {
        Button cellButton = (Button) g.getChildAt(row * g.getColumnCount() + col);
        cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorBackground, null));
        cellButton.setText("M");
    }


    private static void showGameOverDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¡Has Perdido!")
                .setMessage("¡Lo siento! Has hecho clic en una mina.")
                .setPositiveButton("Reintentar", (dialog, which) -> {
                    revealedCells = 0;
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
        revealedCells = 0;

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
    }

package com.example.buscaminas;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Random;

public class Logicas {

    private static int remainingMines;
    private static final int MINA = -1;
    private static int revealedCells = 0;  // Contador de celdas reveladas
    private static int totalCellsWithoutMines;  // Número total de celdas sin minas


    // Método para inicializar el número de minas restantes según la dificultad
    protected static void initializeMineCounter(int difficulty) {
        remainingMines = contadorMinas(difficulty); // Establece las minas restantes basadas en la dificultad
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
        int numMines = contadorMinas(difficulty);  // Obtenemos la cantidad de minas según la dificultad
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
        updateCellCounts(board, difficulty);
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
    protected static int contadorMinas(int difficulty) {
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

    // Método para actualizar las celdas con el número de minas adyacentes
    private static void updateCellCounts(int[][] gameBoard, int difficulty) {
        // Recorremos  el tablero para actualizar las celdas que no son minas
        for (int row = 0; row < difficulty; row++) {
            for (int col = 0; col < difficulty; col++) {
                if (gameBoard[row][col] == MINA) continue;  // Si la celda tiene una mina, la saltamos

                int adjacentMines = 0;

                // Revisa las celdas vecinas (alrededor de la celda actual)
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int neighborRow = row + i;
                        int neighborCol = col + j;

                        // Asegúrate de que la celda vecina esté dentro de los límites del tablero
                        if (neighborRow >= 0 && neighborRow < difficulty &&
                                neighborCol >= 0 && neighborCol < difficulty &&
                                gameBoard[neighborRow][neighborCol] == MINA) {
                            adjacentMines++;  // Cuenta las minas adyacentes
                        }
                    }
                }

                // Asignamos el número de minas adyacentes a la celda
                gameBoard[row][col] = adjacentMines;
            }
        }
    }

    // Método para revelar una celda (click normal)
    private static void revealCell(GridLayout g, int row, int col, int[][] board, int difficulty, android.content.Context context) {
        // Validar si estamos dentro de los límites
        if (row < 0 || row >= difficulty || col < 0 || col >= difficulty) {
            return; // Salir si estamos fuera de los límites
        }

        // Calcular el índice en el GridLayout
        int index = row * difficulty + col;

        // Validar si el índice es válido
        if (index < 0 || index >= g.getChildCount()) {
            return; // Salir si el índice es inválido
        }

        // Obtener el botón asociado
        Button cellButton = (Button) g.getChildAt(index);

        // Validar si el botón es nulo
        if (cellButton == null || !cellButton.getText().toString().isEmpty()) {
            return; // Salir si el botón no existe o ya ha sido revelado
        }

        // Revelar la celda
        if (board[row][col] == -1) {
            revealAllCells(g, board, difficulty);
            showGameOverDialog(context);
        } else if (board[row][col] == 0) {
            revealAdjacentCells(g, row, col, board, difficulty);
        } else {
            cellButton.setText(String.valueOf(board[row][col]));
            cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
            revealedCells++; // Incrementamos el contador de celdas reveladas
        }

        // Verificamos si hemos revelado todas las celdas no minadas
        if (revealedCells == totalCellsWithoutMines) {
            showWinningDialog(context);
        }
    }

    // Método recursivo para revelar las celdas adyacentes (si son 0)
    private static void revealAdjacentCells(GridLayout g, int row, int col, int[][] board, int difficulty) {
        // Verificar si estamos dentro de los límites
        if (row < 0 || row >= difficulty || col < 0 || col >= difficulty) {
            return;
        }

        // Verificar el índice del botón en el GridLayout
        int index = row * difficulty + col;
        if (index < 0 || index >= g.getChildCount()) {
            return;
        }

        // Obtener el botón asociado
        Button cellButton = (Button) g.getChildAt(index);

        // Si ya está revelado, salir
        if (cellButton == null || !cellButton.getText().toString().isEmpty() || board[row][col] == Integer.MIN_VALUE) {
            return; // Ya procesado o celda inválida
        }

        // Revisar el valor de la celda en el tablero
        int cellValue = board[row][col];

        if (cellValue == MINA) {
            return;
        }

        // Revelar la celda actual
        if (cellValue > 0) {
            // Mostrar el número si es mayor que 0
            cellButton.setText(String.valueOf(cellValue));
            cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
            revealedCells++;
        } else {
            // Si es 0, revelamos y marcamos la celda como procesada
            cellButton.setText("");
            cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
            board[row][col] = Integer.MIN_VALUE; // Marcamos la celda como procesada
            revealedCells++;

            // Llamadas recursivas para las celdas vecinas
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i != 0 || j != 0) { // Evitar procesar la misma celda
                        revealAdjacentCells(g, row + i, col + j, board, difficulty);
                    }
                }
            }
        }
        // Verificamos si hemos revelado todas las celdas no minadas
        if (revealedCells == totalCellsWithoutMines) {
            showWinningDialog(g.getContext());
        }

    }

    // Método para revelar todas las celdas después de perder
    private static void revealAllCells(GridLayout g, int[][] board, int difficulty) {
        for (int row = 0; row < difficulty; row++) {
            for (int col = 0; col < difficulty; col++) {
                Button cellButton = (Button) g.getChildAt(row * difficulty + col);

                if (board[row][col] == MINA) {
                    // Si es una mina, mostrar un símbolo especial (por ejemplo, "💣")
                    cellButton.setText("💣");
                    cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorAccent, null));
                } else {
                    // Si no es una mina, mostrar el número de minas adyacentes
                    if (board[row][col] > 0) {
                        cellButton.setText(String.valueOf(board[row][col]));
                    }
                    cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorRevealedCell, null));
                }

                // Desactivar clics en todas las celdas
                cellButton.setOnClickListener(null);
                cellButton.setOnLongClickListener(null);
            }
        }
    }

    // Método para marcar una celda como mina (clic largo)
    private static void markCellAsMine(GridLayout g, int row, int col) {
        Button cellButton = (Button) g.getChildAt(row * g.getColumnCount() + col);
        // Cambiar el fondo o el texto del botón para indicar que es una mina marcada
        cellButton.setBackgroundColor(g.getContext().getResources().getColor(R.color.colorBackground, null));
        cellButton.setText("M");
    }

    // Método para mostrar el diálogo de "Has Perdido"
    private static void showGameOverDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¡Has Perdido!")
                .setMessage("¡Lo siento! Has hecho clic en una mina.")
                .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Aquí puedes reiniciar el juego o salir de la actividad
                        // Por ejemplo, se puede reiniciar la actividad actual
                        revealedCells=0;
                        ((GameActivity) context).recreate(); // Reinicia la actividad actual
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cierra el juego y sale
                        ((GameActivity) context).finish();
                    }
                })
                .setCancelable(false); // Hace que no se pueda cancelar tocando fuera del diálogo


        // Muestra el diálogo
        builder.create().show();
    }

    private static void showWinningDialog(Context context) {
        revealedCells=0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("¡Has Ganado!")
                .setMessage("Has revelado todas las casillas sin minas.")
                .setPositiveButton("Volver a Jugar", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((GameActivity) context).recreate(); // Reinicia la actividad actual
                    }
                })
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cierra el juego y sale
                        ((GameActivity) context).finish();
                    }
                })
                .setCancelable(false); // Hace que no se pueda cancelar tocando fuera del diálogo


        // Muestra el diálogo
        builder.create().show();
    }



}

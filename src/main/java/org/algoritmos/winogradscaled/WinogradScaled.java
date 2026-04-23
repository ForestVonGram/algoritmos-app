package org.algoritmos.winogradscaled;

import org.algoritmos.MatrixGenerator;

import java.io.*;
import java.nio.file.*;

/**
 * WinogradScaled - Variante escalada del algoritmo de Winograd.
 *
 * Extiende WinogradOriginal agregando un paso de escalado previo:
 * cada fila de A y cada columna de B se escalan por un factor lambda
 * calculado para minimizar el error numérico y mejorar la estabilidad.
 *
 * Paso 1 - Escalado:
 *   lambda_i = 0.5 * ( max|A[i][k]| + min|A[i][k]| )  por fila
 *   mu_j     = 0.5 * ( max|B[k][j]| + min|B[k][j]| )  por columna
 *   A'[i][k] = A[i][k] / lambda_i
 *   B'[k][j] = B[k][j] / mu_j
 *
 * Paso 2 - Winograd sobre A' y B' (con long redondeado)
 *
 * Paso 3 - Reescalado del resultado:
 *   C[i][j] = C'[i][j] * lambda_i * mu_j
 *
 * Nota: dado que las matrices usan long (enteros), el escalado se hace
 * en double y el resultado final se redondea con Math.round().
 *
 * Complejidad temporal: O(n^3) — igual que Winograd, con mejor estabilidad
 * Complejidad espacial: O(n^2 + n)
 */
public class WinogradScaled {

    private static final String RESULT_DIR = "datos/resultados/WinogradScaled";
    private static final String LOG_FILE   = "datos/logs/tiempos_WinogradScaled.txt";

    // -----------------------------------------------------------------------
    // Algoritmo Winograd Scaled
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {

        // --- Paso 1: calcular factores de escala por fila (A) y columna (B) ---
        double[] lambdaRow = new double[n];
        double[] muCol     = new double[n];

        for (int i = 0; i < n; i++) {
            long maxVal = Long.MIN_VALUE, minVal = Long.MAX_VALUE;
            for (int k = 0; k < n; k++) {
                long v = Math.abs(A[i][k]);
                if (v > maxVal) maxVal = v;
                if (v < minVal) minVal = v;
            }
            lambdaRow[i] = (maxVal + minVal) == 0 ? 1.0 : 0.5 * (maxVal + minVal);
        }

        for (int j = 0; j < n; j++) {
            long maxVal = Long.MIN_VALUE, minVal = Long.MAX_VALUE;
            for (int k = 0; k < n; k++) {
                long v = Math.abs(B[k][j]);
                if (v > maxVal) maxVal = v;
                if (v < minVal) minVal = v;
            }
            muCol[j] = (maxVal + minVal) == 0 ? 1.0 : 0.5 * (maxVal + minVal);
        }

        // --- Paso 2: escalar A y B ---
        double[][] As = new double[n][n];
        double[][] Bs = new double[n][n];

        for (int i = 0; i < n; i++)
            for (int k = 0; k < n; k++)
                As[i][k] = A[i][k] / lambdaRow[i];

        for (int k = 0; k < n; k++)
            for (int j = 0; j < n; j++)
                Bs[k][j] = B[k][j] / muCol[j];

        // --- Paso 3: Winograd sobre matrices escaladas (en double) ---
        int      half      = n / 2;
        double[] rowFactor = new double[n];
        double[] colFactor = new double[n];

        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int k = 0; k < half; k++)
                sum += As[i][2 * k] * As[i][2 * k + 1];
            rowFactor[i] = sum;
        }

        for (int j = 0; j < n; j++) {
            double sum = 0;
            for (int k = 0; k < half; k++)
                sum += Bs[2 * k][j] * Bs[2 * k + 1][j];
            colFactor[j] = sum;
        }

        double[][] Cs = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = -rowFactor[i] - colFactor[j];
                for (int k = 0; k < half; k++)
                    sum += (As[i][2 * k] + Bs[2 * k + 1][j])
                            * (As[i][2 * k + 1] + Bs[2 * k][j]);
                Cs[i][j] = sum;
            }
        }

        // Residuo si n es impar
        if (n % 2 != 0)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    Cs[i][j] += As[i][n - 1] * Bs[n - 1][j];

        // --- Paso 4: reescalar resultado y convertir a long ---
        long[][] C = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = Math.round(Cs[i][j] * lambdaRow[i] * muCol[j]);

        return C;
    }

    // -----------------------------------------------------------------------
    // Guardar resultado en disco
    // -----------------------------------------------------------------------
    private static void saveResult(long[][] C, int n, int caseNum) throws IOException {
        Files.createDirectories(Paths.get(RESULT_DIR));
        String path = RESULT_DIR + "/caso" + caseNum + "_C_" + n + "x" + n + ".txt";
        MatrixGenerator.save(C, n, path);
    }

    // -----------------------------------------------------------------------
    // Registrar tiempo de ejecución
    // -----------------------------------------------------------------------
    private static void logTime(int caseNum, int n, double timeMs) throws IOException {
        Files.createDirectories(Paths.get("datos/logs"));
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(String.format(
                    "Algoritmo=WinogradScaled | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos y tamaños (llamado desde Main)
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: WinogradScaled");

        for (int caseNum = 1; caseNum <= MatrixGenerator.NUM_CASES; caseNum++) {
            System.out.println("  -- Caso " + caseNum + " --");

            for (int n : MatrixGenerator.SIZES) {
                String pA = MatrixGenerator.pathA(caseNum, n);
                String pB = MatrixGenerator.pathB(caseNum, n);

                System.out.printf("  [%4dx%-4d] Cargando matrices...", n, n);
                long[][] A = MatrixGenerator.load(pA);
                long[][] B = MatrixGenerator.load(pB);

                System.out.printf(" multiplicando...");
                long t0 = System.nanoTime();
                long[][] C = multiply(A, B, n);
                long t1 = System.nanoTime();
                double elapsedMs = (t1 - t0) / 1_000_000.0;

                saveResult(C, n, caseNum);
                logTime(caseNum, n, elapsedMs);

                System.out.printf(" TE = %.3f ms%n", elapsedMs);
            }
            System.out.println();
        }
    }
}
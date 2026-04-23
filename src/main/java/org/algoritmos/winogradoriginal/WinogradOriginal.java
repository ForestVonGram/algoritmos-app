package org.algoritmos.winogradoriginal;

import org.algoritmos.MatrixGenerator;

import java.io.*;
import java.nio.file.*;

/**
 * WinogradOriginal - Multiplicación de matrices usando el algoritmo de Winograd.
 *
 * Winograd reduce el número de multiplicaciones reescribiendo el producto
 * punto a punto. Para cada fila i de A y columna j de B, pre-calcula:
 *
 *   rowFactor[i] = sum( A[i][2k] * A[i][2k+1] )  para k = 0..floor(n/2)-1
 *   colFactor[j] = sum( B[2k][j] * B[2k+1][j] )  para k = 0..floor(n/2)-1
 *
 * Luego:
 *   C[i][j] = -rowFactor[i] - colFactor[j]
 *           + sum( (A[i][2k] + B[2k+1][j]) * (A[i][2k+1] + B[2k][j]) )
 *
 * Si n es impar, se agrega el término residual A[i][n-1] * B[n-1][j].
 *
 * Complejidad temporal: O(n^3) — con ~n^3/2 multiplicaciones (mitad que Naiv)
 * Complejidad espacial: O(n^2 + n)
 */
public class WinogradOriginal {

    private static final String RESULT_DIR = "datos/resultados/WinogradOriginal";
    private static final String LOG_FILE   = "datos/logs/tiempos_WinogradOriginal.txt";

    // -----------------------------------------------------------------------
    // Algoritmo de Winograd original
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {
        long[][] C     = new long[n][n];
        long[]   rowFactor = new long[n];
        long[]   colFactor = new long[n];

        int half = n / 2;

        // Pre-cálculo de factores de fila
        for (int i = 0; i < n; i++) {
            long sum = 0;
            for (int k = 0; k < half; k++)
                sum += A[i][2 * k] * A[i][2 * k + 1];
            rowFactor[i] = sum;
        }

        // Pre-cálculo de factores de columna
        for (int j = 0; j < n; j++) {
            long sum = 0;
            for (int k = 0; k < half; k++)
                sum += B[2 * k][j] * B[2 * k + 1][j];
            colFactor[j] = sum;
        }

        // Cálculo principal
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long sum = -rowFactor[i] - colFactor[j];
                for (int k = 0; k < half; k++)
                    sum += (A[i][2 * k] + B[2 * k + 1][j])
                            * (A[i][2 * k + 1] + B[2 * k][j]);
                C[i][j] = sum;
            }
        }

        // Corrección si n es impar (término residual)
        if (n % 2 != 0) {
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    C[i][j] += A[i][n - 1] * B[n - 1][j];
        }

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
                    "Algoritmo=WinogradOriginal | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos y tamaños (llamado desde Main)
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: WinogradOriginal");

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
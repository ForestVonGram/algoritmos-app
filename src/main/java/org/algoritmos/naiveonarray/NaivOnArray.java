package org.algoritmos.naiveonarray;

import org.algoritmos.MatrixGenerator;

import java.io.*;
import java.nio.file.*;

/**
 * NaivOnArray - Multiplicación de matrices cuadradas (n x n)
 * Triple bucle clásico sobre arreglo bidimensional.
 *
 * Complejidad temporal: O(n^3)
 * Complejidad espacial: O(n^2)
 *
 * Las matrices son cargadas desde disco (generadas por MatrixGenerator).
 * Los resultados y tiempos se persisten en: datos/resultados/ y datos/logs/
 */
public class NaivOnArray {

    private static final String RESULT_DIR = "datos/resultados/NaivOnArray";
    private static final String LOG_FILE   = "datos/logs/tiempos_NaivOnArray.txt";

    // -----------------------------------------------------------------------
    // Algoritmo: triple bucle clásico
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {
        long[][] C = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                for (int k = 0; k < n; k++)
                    C[i][j] += A[i][k] * B[k][j];
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
                    "Algoritmo=NaivOnArray | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos y tamaños (llamado desde Main)
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: NaivOnArray");

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
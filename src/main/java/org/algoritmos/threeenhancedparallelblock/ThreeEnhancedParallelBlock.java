package org.algoritmos.threeenhancedparallelblock;

import org.algoritmos.MatrixGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * ThreeEnhancedParallelBlock - Bloques paralelos mejorados (Row by Column).
 *
 * Implementa III.5 Enhanced Parallel Block del paper de referencia.
 * Divide el trabajo en 2 mitades de filas que se ejecutan en paralelo:
 *   - Hilo 1: filas [0 ... n/2)
 *   - Hilo 2: filas [n/2 ... n)
 *
 * Patrón Row by Column: C[i][j] += A[i][k] * B[k][j]
 *
 * Ventaja: menor overhead que crear muchos hilos pequeños,
 * mantiene buena localidad de caché.
 *
 * Complejidad temporal: O(n^3 / 2) ≈ O(n^3)
 * Complejidad espacial: O(n^2)
 */
public class ThreeEnhancedParallelBlock {

    private static final String RESULT_DIR = "datos/resultados/ThreeEnhancedParallelBlock";
    private static final String LOG_FILE   = "datos/logs/tiempos_ThreeEnhancedParallelBlock.txt";

    private static final int BLOCK_SIZE = 32;

    // -----------------------------------------------------------------------
    // Multiplicación paralela mejorada - Row by Column
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n)
            throws InterruptedException {

        long[][] C = new long[n][n];
        int mid = n / 2;

        Thread worker1 = new Thread(() -> processRange(A, B, C, n, 0, mid));
        Thread worker2 = new Thread(() -> processRange(A, B, C, n, mid, n));

        worker1.start();
        worker2.start();

        worker1.join();
        worker2.join();

        return C;
    }

    // -----------------------------------------------------------------------
    // Procesa rango de filas [startRow, endRow) con patrón Row by Column
    // -----------------------------------------------------------------------
    private static void processRange(
            long[][] A, long[][] B, long[][] C,
            int n, int startRow, int endRow) {

        for (int i1 = startRow; i1 < endRow; i1 += BLOCK_SIZE) {
            for (int j1 = 0; j1 < n; j1 += BLOCK_SIZE) {
                for (int k1 = 0; k1 < n; k1 += BLOCK_SIZE) {

                    for (int i = i1; i < i1 + BLOCK_SIZE && i < endRow && i < n; i++) {
                        for (int j = j1; j < j1 + BLOCK_SIZE && j < n; j++) {
                            for (int k = k1; k < k1 + BLOCK_SIZE && k < n; k++) {
                                C[i][j] += A[i][k] * B[k][j];
                            }
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Guardar resultado
    // -----------------------------------------------------------------------
    private static void saveResult(long[][] C, int n, int caseNum) throws IOException {
        Files.createDirectories(Paths.get(RESULT_DIR));
        String path = RESULT_DIR + "/caso" + caseNum + "_C_" + n + "x" + n + ".txt";
        MatrixGenerator.save(C, n, path);
    }

    // -----------------------------------------------------------------------
    // Registrar tiempo
    // -----------------------------------------------------------------------
    private static void logTime(int caseNum, int n, double timeMs) throws IOException {
        Files.createDirectories(Paths.get("datos/logs"));
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(String.format(
                    "Algoritmo=ThreeEnhancedParallelBlock | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos
    // -----------------------------------------------------------------------
    public static void run() throws Exception {
        System.out.println(">>> Algoritmo: ThreeEnhancedParallelBlock");

        for (int caseNum = 1; caseNum <= MatrixGenerator.NUM_CASES; caseNum++) {
            System.out.println("  -- Caso " + caseNum + " --");

            for (int n : MatrixGenerator.SIZES) {
                String pA = MatrixGenerator.pathA(caseNum, n);
                String pB = MatrixGenerator.pathB(caseNum, n);

                System.out.printf("  [%4dx%-4d] Cargando matrices...", n, n);
                long[][] A = MatrixGenerator.load(pA);
                long[][] B = MatrixGenerator.load(pB);

                System.out.print(" multiplicando...");
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

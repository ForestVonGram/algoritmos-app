package org.algoritmos.threeparallelblock;

import org.algoritmos.MatrixGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 * ThreeParallelBlock - Multiplicación por bloques paralelos (Row by Column).
 *
 * Implementa III.4 Parallel Block del paper de referencia.
 * Variante paralela del Sequential Block usando el patrón Row by Column:
 *
 *   C[i][j] += A[i][k] * B[k][j]
 *
 * Paraleliza sobre i1 (bloques de filas). Cada hilo trabaja filas distintas
 * de C, evitando condiciones de carrera.
 *
 * Complejidad temporal: O(n^3 / p) con p hilos
 * Complejidad espacial: O(n^2)
 */
public class ThreeParallelBlock {

    private static final String RESULT_DIR = "datos/resultados/ThreeParallelBlock";
    private static final String LOG_FILE   = "datos/logs/tiempos_ThreeParallelBlock.txt";

    private static final int BLOCK_SIZE = 32;

    // -----------------------------------------------------------------------
    // Multiplicación por bloques paralelos - Row by Column
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {

        long[][] C = new long[n][n];

        int totalBlocks = (n + BLOCK_SIZE - 1) / BLOCK_SIZE;

        IntStream.range(0, totalBlocks)
                .parallel()
                .forEach(blockI -> {

                    int i1 = blockI * BLOCK_SIZE;

                    for (int j1 = 0; j1 < n; j1 += BLOCK_SIZE) {
                        for (int k1 = 0; k1 < n; k1 += BLOCK_SIZE) {

                            for (int i = i1; i < i1 + BLOCK_SIZE && i < n; i++) {
                                for (int j = j1; j < j1 + BLOCK_SIZE && j < n; j++) {
                                    for (int k = k1; k < k1 + BLOCK_SIZE && k < n; k++) {
                                        C[i][j] += A[i][k] * B[k][j];
                                    }
                                }
                            }
                        }
                    }
                });

        return C;
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
                    "Algoritmo=ThreeParallelBlock | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: ThreeParallelBlock");

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

package org.algoritmos.fiveparallelblock;

import org.algoritmos.MatrixGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.IntStream;

/**
 * FiveParallelBlock
 *
 * Multiplicación de matrices por bloques paralelos.
 *
 * Variante V.4 de la guia:
 *
 * A[k,i] += B[k,j] * C[j,i]
 *
 * Adaptación:
 * Resultado = X * Y
 *
 * R[k][i] += X[k][j] * Y[j][i]
 *
 * Estrategia de paralelización:
 * - Se paraleliza por bloques de columnas (i1).
 * - Cada hilo trabaja columnas distintas del resultado.
 * - No hay condiciones de carrera porque cada columna
 *   es escrita por un solo hilo.
 *
 * Complejidad:
 * Tiempo  O(n^3 / p) aprox.
 * Espacio O(n^2)
 */
public class FiveParallelBlock {

    private static final String RESULT_DIR =
            "datos/resultados/FiveParallelBlock";

    private static final String LOG_FILE =
            "datos/logs/tiempos_FiveParallelBlock.txt";

    private static final int BLOCK_SIZE = 32;

    // ---------------------------------------------------------
    // Multiplicación paralela por bloques
    // ---------------------------------------------------------
    public static long[][] multiply(long[][] X, long[][] Y, int n) {

        long[][] R = new long[n][n];

        int totalBlocks = (n + BLOCK_SIZE - 1) / BLOCK_SIZE;

        IntStream.range(0, totalBlocks)
                .parallel()
                .forEach(blockI -> {

                    int i1 = blockI * BLOCK_SIZE;

                    for (int j1 = 0; j1 < n; j1 += BLOCK_SIZE) {
                        for (int k1 = 0; k1 < n; k1 += BLOCK_SIZE) {

                            for (int i = i1;
                                 i < i1 + BLOCK_SIZE && i < n;
                                 i++) {

                                for (int j = j1;
                                     j < j1 + BLOCK_SIZE && j < n;
                                     j++) {

                                    long yji = Y[j][i];

                                    for (int k = k1;
                                         k < k1 + BLOCK_SIZE && k < n;
                                         k++) {

                                        R[k][i] += X[k][j] * yji;
                                    }
                                }
                            }
                        }
                    }
                });

        return R;
    }

    // ---------------------------------------------------------
    // Guardar resultado
    // ---------------------------------------------------------
    private static void saveResult(long[][] C, int n, int caseNum)
            throws IOException {

        Files.createDirectories(Paths.get(RESULT_DIR));

        String path = RESULT_DIR +
                "/caso" + caseNum +
                "_C_" + n + "x" + n + ".txt";

        MatrixGenerator.save(C, n, path);
    }

    // ---------------------------------------------------------
    // Registrar tiempo
    // ---------------------------------------------------------
    private static void logTime(int caseNum, int n, double timeMs)
            throws IOException {

        Files.createDirectories(Paths.get("datos/logs"));

        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {

            fw.write(String.format(
                    "Algoritmo=FiveParallelBlock | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // ---------------------------------------------------------
    // Ejecutar pruebas
    // ---------------------------------------------------------
    public static void run() throws IOException {

        System.out.println(">>> Algoritmo: FiveParallelBlock");

        for (int caseNum = 1;
             caseNum <= MatrixGenerator.NUM_CASES;
             caseNum++) {

            System.out.println("  -- Caso " + caseNum + " --");

            for (int n : MatrixGenerator.SIZES) {

                String pA = MatrixGenerator.pathA(caseNum, n);
                String pB = MatrixGenerator.pathB(caseNum, n);

                System.out.printf(
                        "  [%4dx%-4d] Cargando matrices...",
                        n, n
                );

                long[][] A = MatrixGenerator.load(pA);
                long[][] B = MatrixGenerator.load(pB);

                System.out.print(" multiplicando...");

                long t0 = System.nanoTime();

                long[][] C = multiply(A, B, n);

                long t1 = System.nanoTime();

                double elapsedMs =
                        (t1 - t0) / 1_000_000.0;

                saveResult(C, n, caseNum);
                logTime(caseNum, n, elapsedMs);

                System.out.printf(
                        " TE = %.3f ms%n",
                        elapsedMs
                );
            }

            System.out.println();
        }
    }
}
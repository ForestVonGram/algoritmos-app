package org.algoritmos.fivesequentialblock;

import org.algoritmos.MatrixGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FiveSequentialBlock
 *
 * Multiplicación de matrices por bloques secuenciales.
 *
 * Variante V.3 dada por la guia:
 *
 * A[k,i] += B[k,j] * C[j,i]
 *
 * Adaptación:
 * Resultado = A * B
 *
 * entonces:
 *
 * C[k][i] += X[k][j] * Y[j][i]
 *
 * donde:
 * X = matriz izquierda
 * Y = matriz derecha
 * C = resultado
 *
 * Complejidad:
 * Tiempo  O(n^3)
 * Espacio O(n^2)
 *
 * Beneficio:
 * Mejor comportamiento de caché que el naive clásico.
 */
public class FiveSequentialBlock {

    private static final String RESULT_DIR =
            "datos/resultados/FiveSequentialBlock";

    private static final String LOG_FILE =
            "datos/logs/tiempos_FiveSequentialBlock.txt";

    private static final int BLOCK_SIZE = 32;

    // ---------------------------------------------------------
    // Multiplicación por bloques secuencial (patrón V.3)
    // ---------------------------------------------------------
    public static long[][] multiply(long[][] X, long[][] Y, int n) {

        long[][] C = new long[n][n];

        for (int i1 = 0; i1 < n; i1 += BLOCK_SIZE) {
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

                                C[k][i] += X[k][j] * yji;
                            }
                        }
                    }
                }
            }
        }

        return C;
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
                    "Algoritmo=FiveSequentialBlock | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // ---------------------------------------------------------
    // Ejecutar pruebas
    // ---------------------------------------------------------
    public static void run() throws IOException {

        System.out.println(">>> Algoritmo: FiveSequentialBlock");

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
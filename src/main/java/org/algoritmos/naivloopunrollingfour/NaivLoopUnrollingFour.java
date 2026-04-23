package org.algoritmos.naivloopunrollingfour;

import org.algoritmos.MatrixGenerator;

import java.io.*;
import java.nio.file.*;

/**
 * NaivLoopUnrollingFour - Multiplicación de matrices con loop unrolling factor 4.
 *
 * El bucle interno (k) se desenrolla de 4 en 4:
 * en cada iteración se acumulan 4 productos simultáneamente,
 * maximizando el uso del pipeline del procesador y reduciendo
 * el overhead de control del bucle respecto al factor 2.
 *
 * Si n no es múltiplo de 4, los elementos sobrantes (1, 2 o 3)
 * se procesan en el bloque residual al final.
 *
 * Complejidad temporal: O(n^3)  — misma asintótica, mejor constante que x2
 * Complejidad espacial: O(n^2)
 */
public class NaivLoopUnrollingFour {

    private static final String RESULT_DIR = "datos/resultados/NaivLoopUnrollingFour";
    private static final String LOG_FILE   = "datos/logs/tiempos_NaivLoopUnrollingFour.txt";

    // -----------------------------------------------------------------------
    // Algoritmo: bucle k desenrollado de 4 en 4
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {
        long[][] C = new long[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long sum = 0;
                int k = 0;

                // Bucle principal: procesa 4 elementos por iteración
                for (; k <= n - 4; k += 4) {
                    sum += A[i][k]     * B[k][j]
                            + A[i][k + 1] * B[k + 1][j]
                            + A[i][k + 2] * B[k + 2][j]
                            + A[i][k + 3] * B[k + 3][j];
                }

                // Residuo: procesa los elementos sobrantes (0, 1, 2 o 3)
                for (; k < n; k++) {
                    sum += A[i][k] * B[k][j];
                }

                C[i][j] = sum;
            }
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
                    "Algoritmo=NaivLoopUnrollingFour | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos y tamaños (llamado desde Main)
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: NaivLoopUnrollingFour");

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
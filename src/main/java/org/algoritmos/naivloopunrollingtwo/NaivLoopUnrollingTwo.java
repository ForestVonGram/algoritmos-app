package org.algoritmos.naivloopunrollingtwo;

import org.algoritmos.MatrixGenerator;

import java.io.*;
import java.nio.file.*;

/**
 * NaivLoopUnrollingTwo - Multiplicación de matrices con loop unrolling factor 2.
 *
 * El bucle interno (k) se desenrolla de 2 en 2:
 * en cada iteración se acumulan 2 productos simultáneamente,
 * reduciendo overhead de control y mejorando el pipeline del procesador.
 *
 * Si n es impar, el elemento sobrante se procesa al final (caso residual).
 *
 * Complejidad temporal: O(n^3)  — misma asintótica que Naiv, mejor constante
 * Complejidad espacial: O(n^2)
 */
public class NaivLoopUnrollingTwo {

    private static final String RESULT_DIR = "datos/resultados/NaivLoopUnrollingTwo";
    private static final String LOG_FILE   = "datos/logs/tiempos_NaivLoopUnrollingTwo.txt";

    // -----------------------------------------------------------------------
    // Algoritmo: bucle k desenrollado de 2 en 2
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {
        long[][] C = new long[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                long sum = 0;
                int k = 0;

                // Bucle principal: procesa 2 elementos por iteración
                for (; k <= n - 2; k += 2) {
                    sum += A[i][k]     * B[k][j]
                            + A[i][k + 1] * B[k + 1][j];
                }

                // Residuo: si n es impar, procesa el último elemento
                if (k < n) {
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
                    "Algoritmo=NaivLoopUnrollingTwo | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos y tamaños (llamado desde Main)
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: NaivLoopUnrollingTwo");

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
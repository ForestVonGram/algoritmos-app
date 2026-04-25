package org.algoritmos.strassenwinograd;

import org.algoritmos.MatrixGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * StrassenWinograd - Variante de Strassen con la optimización de Winograd.
 *
 * Reduce el número de sumas/restas de Strassen original (18 → 15)
 * mediante la reformulación de Winograd de los 7 productos.
 *
 * Productos de Winograd:
 *   S1  = A21 + A22
 *   S2  = S1  - A11
 *   S3  = A11 - A21
 *   S4  = A12 - S2
 *   S5  = B12 - B11
 *   S6  = B22 - S5
 *   S7  = B22 - B12
 *   S8  = S6  - B21
 *
 *   P1  = S2  * S6
 *   P2  = A11 * B11
 *   P3  = A12 * B21
 *   P4  = S3  * S7
 *   P5  = S1  * S5
 *   P6  = S4  * B22
 *   P7  = A22 * S8
 *
 *   T1  = P1 + P2
 *   T2  = T1 + P4
 *
 *   C11 = P2 + P3
 *   C12 = T1 + P5 + P6
 *   C21 = T2 - P7
 *   C22 = T2 + P5
 *
 * Complejidad temporal: O(n^2.807)
 * Complejidad espacial: O(n^2 log n)
 */
public class StrassenWinograd {

    private static final String RESULT_DIR = "datos/resultados/StrassenWinograd";
    private static final String LOG_FILE   = "datos/logs/tiempos_StrassenWinograd.txt";

    // -----------------------------------------------------------------------
    // Operaciones auxiliares
    // -----------------------------------------------------------------------
    private static long[][] add(long[][] A, long[][] B, int n) {
        long[][] R = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                R[i][j] = A[i][j] + B[i][j];
        return R;
    }

    private static long[][] sub(long[][] A, long[][] B, int n) {
        long[][] R = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                R[i][j] = A[i][j] - B[i][j];
        return R;
    }

    // -----------------------------------------------------------------------
    // Algoritmo de Strassen-Winograd recursivo
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {

        // Caso base
        if (n == 1) {
            long[][] C = new long[1][1];
            C[0][0] = A[0][0] * B[0][0];
            return C;
        }

        int half = n / 2;

        // Dividir en submatrices
        long[][] A11 = new long[half][half];
        long[][] A12 = new long[half][half];
        long[][] A21 = new long[half][half];
        long[][] A22 = new long[half][half];

        long[][] B11 = new long[half][half];
        long[][] B12 = new long[half][half];
        long[][] B21 = new long[half][half];
        long[][] B22 = new long[half][half];

        for (int i = 0; i < half; i++) {
            for (int j = 0; j < half; j++) {
                A11[i][j] = A[i][j];
                A12[i][j] = A[i][j + half];
                A21[i][j] = A[i + half][j];
                A22[i][j] = A[i + half][j + half];

                B11[i][j] = B[i][j];
                B12[i][j] = B[i][j + half];
                B21[i][j] = B[i + half][j];
                B22[i][j] = B[i + half][j + half];
            }
        }

        // Sumas intermedias de Winograd
        long[][] S1 = add(A21, A22, half);
        long[][] S2 = sub(S1,  A11, half);
        long[][] S3 = sub(A11, A21, half);
        long[][] S4 = sub(A12, S2,  half);

        long[][] S5 = sub(B12, B11, half);
        long[][] S6 = sub(B22, S5,  half);
        long[][] S7 = sub(B22, B12, half);
        long[][] S8 = sub(S6,  B21, half);

        // 7 productos recursivos
        long[][] P1 = multiply(S2,  S6,  half);
        long[][] P2 = multiply(A11, B11, half);
        long[][] P3 = multiply(A12, B21, half);
        long[][] P4 = multiply(S3,  S7,  half);
        long[][] P5 = multiply(S1,  S5,  half);
        long[][] P6 = multiply(S4,  B22, half);
        long[][] P7 = multiply(A22, S8,  half);

        // Combinaciones
        long[][] T1  = add(P1, P2, half);
        long[][] T2  = add(T1, P4, half);

        long[][] C11 = add(P2, P3, half);
        long[][] C12 = add(add(T1, P5, half), P6, half);
        long[][] C21 = sub(T2, P7, half);
        long[][] C22 = add(T2, P5, half);

        // Ensamblar resultado
        long[][] C = new long[n][n];
        for (int i = 0; i < half; i++) {
            for (int j = 0; j < half; j++) {
                C[i][j]               = C11[i][j];
                C[i][j + half]        = C12[i][j];
                C[i + half][j]        = C21[i][j];
                C[i + half][j + half] = C22[i][j];
            }
        }

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
                    "Algoritmo=StrassenWinograd | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: StrassenWinograd");

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

package org.algoritmos.strassennaiv;

import org.algoritmos.MatrixGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * StrassenNaiv - Multiplicación de matrices usando el algoritmo de Strassen.
 *
 * Divide la matriz en 4 submatrices de n/2 x n/2 y realiza 7 multiplicaciones
 * recursivas en lugar de 8 (algoritmo clásico).
 *
 * Cuando n == 1 (caso base), multiplica directamente.
 *
 * Complejidad temporal: O(n^2.807)  [Strassen 1969]
 * Complejidad espacial: O(n^2 log n)
 *
 * Nota: Las matrices deben ser cuadradas con n potencia de 2.
 */
public class StrassenNaiv {

    private static final String RESULT_DIR = "datos/resultados/StrassenNaiv";
    private static final String LOG_FILE   = "datos/logs/tiempos_StrassenNaiv.txt";

    // -----------------------------------------------------------------------
    // Suma de matrices
    // -----------------------------------------------------------------------
    private static long[][] add(long[][] A, long[][] B, int n) {
        long[][] R = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                R[i][j] = A[i][j] + B[i][j];
        return R;
    }

    // -----------------------------------------------------------------------
    // Resta de matrices
    // -----------------------------------------------------------------------
    private static long[][] sub(long[][] A, long[][] B, int n) {
        long[][] R = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                R[i][j] = A[i][j] - B[i][j];
        return R;
    }

    // -----------------------------------------------------------------------
    // Algoritmo de Strassen recursivo
    // -----------------------------------------------------------------------
    public static long[][] multiply(long[][] A, long[][] B, int n) {

        // Caso base
        if (n == 1) {
            long[][] C = new long[1][1];
            C[0][0] = A[0][0] * B[0][0];
            return C;
        }

        int half = n / 2;

        // Dividir A y B en submatrices
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

        // 7 productos de Strassen
        long[][] P1 = multiply(add(A11, A22, half), add(B11, B22, half), half);
        long[][] P2 = multiply(add(A21, A22, half), B11, half);
        long[][] P3 = multiply(A11, sub(B12, B22, half), half);
        long[][] P4 = multiply(A22, sub(B21, B11, half), half);
        long[][] P5 = multiply(add(A11, A12, half), B22, half);
        long[][] P6 = multiply(sub(A21, A11, half), add(B11, B12, half), half);
        long[][] P7 = multiply(sub(A12, A22, half), add(B21, B22, half), half);

        // Combinar resultados
        long[][] C11 = add(sub(add(P1, P4, half), P5, half), P7, half);
        long[][] C12 = add(P3, P5, half);
        long[][] C21 = add(P2, P4, half);
        long[][] C22 = add(sub(add(P1, P3, half), P2, half), P6, half);

        // Ensamblar matriz resultado
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
                    "Algoritmo=StrassenNaiv | Caso=%d | n=%d | n*n=%d | TE=%.6f ms%n",
                    caseNum, n, (long) n * n, timeMs
            ));
        }
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los casos
    // -----------------------------------------------------------------------
    public static void run() throws IOException {
        System.out.println(">>> Algoritmo: StrassenNaiv");

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

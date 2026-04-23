package org.algoritmos;

import java.io.*;
import java.nio.file.*;
import java.util.Random;

/**
 * MatrixGenerator - Genera y persiste las matrices compartidas.
 *
 * Las matrices se guardan en: datos/matrices/caso{1|2}_A_NxN.txt
 *                                              caso{1|2}_B_NxN.txt
 *
 * Todos los algoritmos deben cargar sus matrices desde estos archivos.
 *
 * Tamaños: 32, 64, 128, 512, 1024, 2048  (todos potencias de 2)
 * Valores: mínimo 6 dígitos [100000, 999999]
 * Casos  : 2 pares de matrices independientes por tamaño
 */
public class MatrixGenerator {

    public static final String MATRIX_DIR = "datos/matrices";
    public static final int[] SIZES = {32, 64, 128, 512, 1024, 2048};
    public static final int NUM_CASES = 2;

    // -----------------------------------------------------------------------
    // Ruta estándar para acceder a las matrices
    // -----------------------------------------------------------------------
    public static String pathA(int caseNum, int n) {
        return MATRIX_DIR + "/caso" + caseNum + "_A_" + n + "x" + n + ".txt";
    }

    public static String pathB(int caseNum, int n) {
        return MATRIX_DIR + "/caso" + caseNum + "_B_" + n + "x" + n + ".txt";
    }

    // -----------------------------------------------------------------------
    // Generar matriz con valores de mínimo 6 dígitos [100000, 999999]
    // -----------------------------------------------------------------------
    public static long[][] generate(int n, Random rng) {
        long[][] M = new long[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                M[i][j] = 100000L + (long)(rng.nextDouble() * 900000L);
        return M;
    }

    // -----------------------------------------------------------------------
    // Guardar matriz en disco
    // -----------------------------------------------------------------------
    public static void save(long[][] M, int n, String path) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(n + "\n");
            for (int i = 0; i < n; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    if (j > 0) sb.append(" ");
                    sb.append(M[i][j]);
                }
                bw.write(sb.toString() + "\n");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Cargar matriz desde disco (usado por todos los algoritmos)
    // -----------------------------------------------------------------------
    public static long[][] load(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(path)))) {
            int n = Integer.parseInt(br.readLine().trim());
            long[][] M = new long[n][n];
            for (int i = 0; i < n; i++) {
                String[] parts = br.readLine().trim().split(" ");
                for (int j = 0; j < n; j++)
                    M[i][j] = Long.parseLong(parts[j]);
            }
            return M;
        }
    }

    // -----------------------------------------------------------------------
    // Verificar si ya existen todas las matrices en disco
    // -----------------------------------------------------------------------
    public static boolean allExist() {
        for (int c = 1; c <= NUM_CASES; c++)
            for (int n : SIZES)
                if (!Files.exists(Paths.get(pathA(c, n))) ||
                        !Files.exists(Paths.get(pathB(c, n))))
                    return false;
        return true;
    }

    // -----------------------------------------------------------------------
    // Generar y guardar todas las matrices (si no existen)
    // -----------------------------------------------------------------------
    public static void generateAll() throws IOException {
        Files.createDirectories(Paths.get(MATRIX_DIR));

        // Semillas fijas por caso para garantizar reproducibilidad
        long[] seeds = {42L, 84L};

        System.out.println("=== Generando matrices compartidas ===\n");

        for (int c = 1; c <= NUM_CASES; c++) {
            Random rng = new Random(seeds[c - 1]);
            System.out.println("--- Caso " + c + " ---");

            for (int n : SIZES) {
                String pA = pathA(c, n);
                String pB = pathB(c, n);

                if (Files.exists(Paths.get(pA)) && Files.exists(Paths.get(pB))) {
                    System.out.printf("  [%4dx%-4d] Ya existen, omitiendo.%n", n, n);
                    // Avanzar el rng igual para mantener consistencia
                    generate(n, rng); // A (descartada)
                    generate(n, rng); // B (descartada)
                    continue;
                }

                System.out.printf("  [%4dx%-4d] Generando...", n, n);
                long t0 = System.nanoTime();

                long[][] A = generate(n, rng);
                long[][] B = generate(n, rng);

                save(A, n, pA);
                save(B, n, pB);

                double ms = (System.nanoTime() - t0) / 1_000_000.0;
                System.out.printf(" listo (%.1f ms) → %s%n", ms,
                        Paths.get(pA).getFileName());
            }
            System.out.println();
        }

        System.out.println("=== Matrices listas en: " + MATRIX_DIR + " ===\n");
    }

    // -----------------------------------------------------------------------
    // Main independiente para generar matrices sin ejecutar algoritmos
    // -----------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        generateAll();
    }
}
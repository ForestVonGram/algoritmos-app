package org.algoritmos;

import org.algoritmos.MatrixGenerator;
import org.algoritmos.naiveonarray.NaivOnArray;
import org.algoritmos.naivloopunrollingtwo.NaivLoopUnrollingTwo;
import org.algoritmos.naivloopunrollingfour.NaivLoopUnrollingFour;
import org.algoritmos.winogradoriginal.WinogradOriginal;
// import org.algoritmos.winogradscaled.WinogradScaled;
// import org.algoritmos.strassennaiv.StrassenNaiv;
// import org.algoritmos.strassenwinograd.StrassenWinograd;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main - Menú interactivo para seleccionar y ejecutar algoritmos
 * de multiplicación de matrices.
 */
public class Main {

    // Registro de algoritmos disponibles: nombre → runnable
    // Agregar aquí cada nuevo algoritmo cuando se implemente
    private static final LinkedHashMap<String, AlgorithmRunner> ALGORITHMS = new LinkedHashMap<>();

    static {
        ALGORITHMS.put("NaivOnArray",          NaivOnArray::run);
        ALGORITHMS.put("NaivLoopUnrollingTwo",  NaivLoopUnrollingTwo::run);
        ALGORITHMS.put("NaivLoopUnrollingFour", NaivLoopUnrollingFour::run);
        ALGORITHMS.put("WinogradOriginal",       WinogradOriginal::run);
        // ALGORITHMS.put("WinogradScaled",          WinogradScaled::run);
    }

    @FunctionalInterface
    interface AlgorithmRunner {
        void run() throws Exception;
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        // ------------------------------------------------------------------
        // PASO 1: Generar matrices si no existen
        // ------------------------------------------------------------------
        System.out.println("==========================================");
        System.out.println("  Multiplicación de Matrices - Análisis");
        System.out.println("==========================================\n");

        if (!MatrixGenerator.allExist()) {
            MatrixGenerator.generateAll();
        } else {
            System.out.println("=== Matrices ya existentes en disco. ===\n");
        }

        // ------------------------------------------------------------------
        // PASO 2: Menú de selección
        // ------------------------------------------------------------------
        boolean exit = false;

        while (!exit) {
            printMenu();
            System.out.print("Selección: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q") || input.equalsIgnoreCase("0")) {
                exit = true;

            } else if (input.equalsIgnoreCase("all") || input.equals("*")) {
                runAll();

            } else {
                // Puede ingresar múltiples opciones separadas por coma: "1,3"
                String[] tokens = input.split(",");
                String[] names  = new String[tokens.length];
                boolean valid   = true;

                for (int i = 0; i < tokens.length; i++) {
                    String resolved = resolveSelection(tokens[i].trim());
                    if (resolved == null) {
                        System.out.println("\n  ✗ Opción no reconocida: \"" + tokens[i].trim() + "\"\n");
                        valid = false;
                        break;
                    }
                    names[i] = resolved;
                }

                if (valid) {
                    for (String name : names) {
                        System.out.println();
                        ALGORITHMS.get(name).run();
                    }
                }
            }

            if (!exit) {
                System.out.print("\nPresiona Enter para continuar...");
                scanner.nextLine();
                System.out.println();
            }
        }

        System.out.println("\n==========================================");
        System.out.println("  Logs en:       datos/logs/");
        System.out.println("  Resultados en: datos/resultados/");
        System.out.println("  ¡Hasta luego!");
        System.out.println("==========================================");
        scanner.close();
    }

    // -----------------------------------------------------------------------
    // Imprime el menú con los algoritmos registrados
    // -----------------------------------------------------------------------
    private static void printMenu() {
        System.out.println("------------------------------------------");
        System.out.println("  Seleccione algoritmo(s) a ejecutar:");
        System.out.println("------------------------------------------");

        int i = 1;
        for (String name : ALGORITHMS.keySet()) {
            System.out.printf("  [%2d] %s%n", i++, name);
        }

        System.out.println("------------------------------------------");
        System.out.println("  [*]  Ejecutar TODOS");
        System.out.println("  [0]  Salir");
        System.out.println("------------------------------------------");
        System.out.println("  Tip: separa opciones con coma → 1,3");
        System.out.println("------------------------------------------");
    }

    // -----------------------------------------------------------------------
    // Resuelve número o nombre a clave del mapa
    // -----------------------------------------------------------------------
    private static String resolveSelection(String input) {
        // Por número
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= ALGORITHMS.size()) {
                return (String) ALGORITHMS.keySet().toArray()[idx - 1];
            }
            return null;
        } catch (NumberFormatException ignored) {}

        // Por nombre (case-insensitive)
        for (String name : ALGORITHMS.keySet()) {
            if (name.equalsIgnoreCase(input)) return name;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Ejecutar todos los algoritmos registrados
    // -----------------------------------------------------------------------
    private static void runAll() throws Exception {
        System.out.println("\n=== Ejecutando TODOS los algoritmos ===\n");
        for (Map.Entry<String, AlgorithmRunner> entry : ALGORITHMS.entrySet()) {
            entry.getValue().run();
        }
    }
}

package org.algoritmos;

import org.algoritmos.MatrixGenerator;
import org.algoritmos.naiveonarray.NaivOnArray;
// import org.algoritmos.naivloopunrollingtwo.NaivLoopUnrollingTwo;
// import org.algoritmos.naivloopunrollingfour.NaivLoopUnrollingFour;
// import org.algoritmos.winogradoriginal.WinogradOriginal;
// import org.algoritmos.winogradscaled.WinogradScaled;
// import org.algoritmos.strassennaiv.StrassenNaiv;
// import org.algoritmos.strassenwinograd.StrassenWinograd;

/**
 * Main - Punto de entrada central.
 *
 * Paso 1: Genera las matrices compartidas (si no existen).
 * Paso 2: Ejecuta los algoritmos seleccionados sobre esas matrices.
 *
 * Tamaños: 32x32, 64x64, 128x128, 512x512, 1024x1024, 2048x2048
 * Casos  : 2 pares de matrices por tamaño
 */
public class Main {

    public static void main(String[] args) throws Exception {

        System.out.println("==========================================");
        System.out.println("  Multiplicación de Matrices - Análisis");
        System.out.println("==========================================\n");

        // ------------------------------------------------------------------
        // PASO 1: Generar matrices compartidas (solo si no existen en disco)
        // ------------------------------------------------------------------
        if (!MatrixGenerator.allExist()) {
            MatrixGenerator.generateAll();
        } else {
            System.out.println("=== Matrices ya existentes, omitiendo generación. ===\n");
        }

        // ------------------------------------------------------------------
        // PASO 2: Ejecutar algoritmos
        // Comentar/descomentar según los algoritmos que se deseen ejecutar
        // ------------------------------------------------------------------

        NaivOnArray.run();

        // NaivLoopUnrollingTwo.run();
        // NaivLoopUnrollingFour.run();
        // WinogradOriginal.run();
        // WinogradScaled.run();
        // StrassenNaiv.run();
        // StrassenWinograd.run();

        System.out.println("==========================================");
        System.out.println("  Ejecución finalizada.");
        System.out.println("  Logs en: datos/logs/");
        System.out.println("  Resultados en: datos/resultados/");
        System.out.println("==========================================");
    }
}
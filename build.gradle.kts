plugins {
    id("java")
}

group = "org.despegar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Tarea para lanzar la interfaz gráfica
tasks.register<JavaExec>("runGui") {
    group = "application"
    description = "Lanza la interfaz gráfica de multiplicación de matrices"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.algoritmos.gui.MatrixApp")
}

// Tarea para lanzar el menú de consola original
tasks.register<JavaExec>("runConsole") {
    group = "application"
    description = "Lanza el menú interactivo de consola"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.algoritmos.Main")
    standardInput = System.`in`
}
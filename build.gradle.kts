import org.openstreetmap.josm.gradle.plugin.task.GeneratePot

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
    id("org.openstreetmap.josm") version "0.7.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}

group = "org.openstreetmap.josm.plugins.hexgrid"
version = "0.0.3"

repositories {
    mavenCentral()
}

josm {
    pluginName = "hexgrid-generator"
    debugPort = 12345
    josmCompileVersion = "18191"
    manifest {
        description = "JOSM plugin for generating hexgrid"
        mainClass = "org.openstreetmap.josm.plugins.hexgrid.HexGridPlugin"
        minJosmVersion = "17919"
        author = "Dmitriy K."
        canLoadAtRuntime = true
        iconPath = "images/icon.svg"
    }
}

dependencies {
    packIntoJar(kotlin("stdlib"))
}

tasks {
    create<GeneratePot>(
        "generateKotlinPot",
        provider { sourceSets["main"].allSource.filter { it.extension == "kt" }.files }
    )
}

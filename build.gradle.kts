plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
}

group = "org.hazelv"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.minestom:minestom-snapshots:d1634fb586")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("de.articdive:jnoise-pipeline:4.1.0")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "org.hazelv.RollInitiative.Main"
        }
    }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
    test {
        useJUnitPlatform()
    }
}
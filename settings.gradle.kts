plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "kokonut_nametag"

include("common", "api", "api-paper", "paper")
includeBuild("..")

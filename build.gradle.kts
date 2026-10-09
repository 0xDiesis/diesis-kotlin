plugins {
    kotlin("jvm") version "2.4.20"
    `java-library`
    `maven-publish`
}

group = "xyz.diesis"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
}

dependencies {
    api("org.web3j:core:6.0.0")

    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("diesis.canonicalJson", layout.projectDirectory.file("canonical.json").asFile.absolutePath)
    systemProperty("diesis.readme", layout.projectDirectory.file("README.md").asFile.absolutePath)
    val contractsDir = System.getenv("DIESIS_CONTRACTS_DIR")
        ?: layout.projectDirectory.dir("../../diesis-core/diesis/contracts").asFile.absolutePath
    systemProperty("diesis.contractsDir", contractsDir)
    inputs.file("canonical.json")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "diesis"
            from(components["java"])
            pom {
                name.set("Diesis Kotlin SDK")
                description.set("Typed access to the Diesis system contracts for Kotlin and Java")
                url.set("https://github.com/0xDiesis/diesis-kotlin")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}

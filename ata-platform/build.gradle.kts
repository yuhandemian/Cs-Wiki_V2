import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    kotlin("jvm") version "1.9.25" apply false
    kotlin("plugin.spring") version "1.9.25" apply false
    kotlin("plugin.jpa") version "1.9.25" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")

    group = "io.ata"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.5")
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
        }
    }

    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "implementation"("com.fasterxml.jackson.module:jackson-module-kotlin")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.spring.dependency-management") version "1.0.9.RELEASE"

    kotlin("jvm") version "1.3.72"
}

group = "com.codete"
version = "0.0.1-SNAPSHOT"

extra["springBootVersion"] = "2.3.2.RELEASE"

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework:spring-webflux")

    testImplementation(kotlin("test"))
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.9")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.9")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}")
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}
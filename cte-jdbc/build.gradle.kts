plugins {
    kotlin("jvm") version "1.3.72"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.vladsch.kotlin-jdbc:kotlin-jdbc:0.5.0")
    runtimeOnly("com.h2database:h2:1.4.198")
    runtimeOnly("org.slf4j:slf4j-simple:1.7.30")

}

application {
    mainClassName = "Main"
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    application
}


group = "com.livebus.ghostdriver"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.springframework:spring-websocket:6.1.10")
    implementation("org.springframework:spring-messaging:6.1.10")
    implementation("org.apache.tomcat.embed:tomcat-embed-websocket:10.1.25")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

application {
    mainClass.set("com.livebus.ghostdriver.MainKt")
}

kotlin {
    jvmToolchain(17)
}

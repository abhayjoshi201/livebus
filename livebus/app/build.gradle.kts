plugins {
    java
    // Adds Spring Boot support (creates runnable fat JARs, bootRun task, etc.)
    id("org.springframework.boot") version "3.4.1"
    
}

group = "com.livebus.backend"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.4.1"))

    // 1. Core Web (REST APIs, embedded Tomcat)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // 2. Security (Authentication & Authorization)
    implementation("org.springframework.boot:spring-boot-starter-security")

    // 3. Database Layer (JPA, Postgres Driver, and PostGIS Spatial support)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.hibernate.orm:hibernate-spatial") // Crucial for parsing GPS Coordinates

    // 4. WebSockets (For real-time location streaming later)
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // 5. Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

springBoot {
    mainClass.set("livebus.App")
}
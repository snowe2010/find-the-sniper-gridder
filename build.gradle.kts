plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
//    id("io.quarkus")
    id("io.quarkus") version "2.6.2.Final"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
}

group = "com.tylerthrailkill"
version = "0.0.1"

repositories {
    mavenCentral()
}

val ktor_version = "1.6.7"
val cdk_version = "1.138.2"
val sdk_version = "2.17.107"

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Quarkus
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:quarkus-amazon-services-bom:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-amazon-lambda")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-awt")
//    implementation("io.quarkus:quarkus-resteasy")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    implementation("io.quarkiverse.amazonservices:quarkus-amazon-secretsmanager")

    // Ktor
    implementation("io.ktor:ktor:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-locations:1.6.7")
    implementation("io.ktor:ktor-server-core:1.6.7")
    implementation("io.ktor:ktor-client-core-jvm:1.6.7")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // AWS CDK
    implementation("software.amazon.awscdk:apigateway:${cdk_version}")
    implementation("software.amazon.awscdk:core:${cdk_version}")
    implementation("software.amazon.awscdk:s3:${cdk_version}")
    implementation("software.amazon.awscdk:lambda:${cdk_version}")
    implementation("software.amazon.awscdk:ssm:${cdk_version}")
    implementation("software.amazon.awscdk:codedeploy:${cdk_version}")
    implementation("software.amazon.awscdk:dynamodb:${cdk_version}")

    // aws sdk
    implementation("software.amazon.awssdk:secretsmanager")
    implementation("com.amazonaws:aws-java-sdk-secretsmanager:1.12.138")
    implementation("software.amazon.awssdk:url-connection-client")
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:dynamodb-enhanced")
    implementation("software.amazon.awssdk:netty-nio-client")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
}

tasks.quarkusBuild {
    nativeArgs {
        "container-build" to true
        "build-image" to "quay.io/quarkus/ubi-quarkus-native-image:21.3.0-java11"
        "java-home" to "/opt/java-11/"
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}

tasks.register<JavaExec>("cdk") {
    main = "com.tylerthrailkill.sniper.helpers.FindTheSniperApp"
    classpath = sourceSets.main.get().runtimeClasspath
}

//task runTwaService(type: JavaExec) {
//    main = 'com.service.main.TWAService'
//    classpath = sourceSets.main.runtimeClasspath
//}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

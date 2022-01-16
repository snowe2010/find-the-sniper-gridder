plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
    kotlin("plugin.noarg") version "1.6.10"
//    id("io.quarkus")
    id("io.quarkus") version "2.6.2.Final"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
    application
}

group = "com.tylerthrailkill"
version = "0.0.1"

repositories {
    mavenCentral()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

fun awsCdk(module: String, version: String? = "1.138.2") = "software.amazon.awscdk:$module:$version"
fun awsSdk(module: String, version: String? = "2.17.107") = "software.amazon.awssdk:$module:$version"
fun ktor(module: String? = null, version: String? = "1.6.7") = "io.ktor:$module:$version"

fun quarkus(module: String, version: String? = null) = "io.quarkus:$module${if (version != null) ":$version" else ""}"
fun quarkiverse(module: String, version: String? = null) = "io.quarkiverse.amazonservices:$module${if (version != null) ":$version" else ""}"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Quarkus
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:quarkus-amazon-services-bom:${quarkusPlatformVersion}"))
    implementation(quarkus("quarkus-amazon-lambda"))
    implementation(quarkus("quarkus-kotlin"))
    implementation(quarkus("quarkus-arc"))
    implementation(quarkus("quarkus-awt"))
    implementation(quarkiverse("quarkus-amazon-secretsmanager"))
    implementation(quarkiverse("quarkus-amazon-dynamodb"))
    
    testImplementation(quarkus("quarkus-junit5"))
    testImplementation("io.rest-assured:rest-assured")

    // Ktor
    implementation(ktor("ktor")) 
    implementation(ktor("ktor-client-core")) 
    implementation(ktor("ktor-client-cio")) 
    implementation(ktor("ktor-auth")) 
    implementation(ktor("ktor-server-netty")) 
    implementation(ktor("ktor-client-apache")) 
    implementation(ktor("ktor-client-auth")) 
    implementation(ktor("ktor-locations"))
    implementation(ktor("ktor-server-core")) 
    implementation(ktor("ktor-client-core-jvm")) 
    implementation(ktor("ktor-client-serialization"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // AWS CDK
    implementation(awsCdk("apigateway"))
    implementation(awsCdk("core")) 
    implementation(awsCdk("s3")) 
    implementation(awsCdk("lambda")) 
    implementation(awsCdk("ssm")) 
    implementation(awsCdk("codedeploy")) 
    implementation(awsCdk("secretsmanager")) 
    implementation(awsCdk("dynamodb"))

    // aws sdk
    implementation(awsSdk("secretsmanager"))
    implementation(awsSdk("url-connection-client")) 
    implementation(awsSdk("dynamodb")) 
    implementation(awsSdk("dynamodb-enhanced"))
    implementation(awsSdk("netty-nio-client"))
    implementation(awsSdk("apache-client"))
    implementation("com.amazonaws:aws-java-sdk-secretsmanager:1.12.138")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.apache.logging.log4j:log4j-api:2.17.0")
    implementation("org.apache.logging.log4j:log4j-core:2.17.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.0")
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.5.0")
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

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

noArg {
    annotation("software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean")
}

application {
    mainClass.set("com.tylerthrailkill.sniper.helpers.FindTheSniperApp")
}

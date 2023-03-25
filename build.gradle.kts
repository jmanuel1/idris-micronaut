import com.lingocoder.plugin.jarexec.ExecJar

buildscript {
    dependencies {
        classpath("com.lingocoder:jarexec.plugin")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlin.kapt") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.7.4"
//    id("com.lingocoder.jarexec") version "0.5.2"
    id("com.lingocoder.jarexec")
}


version = "0.1"
group = "com.jasonmanuel.idrismicronaut"

val kotlinVersion=project.properties.get("kotlinVersion")
repositories {
    mavenCentral()
}

val idrisCompiler: Configuration by configurations.creating

dependencies {
    idrisCompiler("io.github.mmhelloworld:idris-jvm-runtime:0.6.0.2")
    idrisCompiler("io.github.mmhelloworld:idris-jvm-compiler:0.6.0.2")

    kapt("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    runtimeOnly("ch.qos.logback:logback-classic")
    implementation("io.micronaut:micronaut-validation")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
}


application {
    mainClass.set("com.jasonmanuel.idrismicronaut.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.jasonmanuel.idrismicronaut.*")
    }
}

// https://github.com/mmhelloworld/idris-spring-boot-example/blob/master/build.gradle

val ipkg: String = fileTree("dir" to "${projectDir}/src/main/idris", "include" to "*.ipkg").singleFile.absolutePath
println(ipkg)

task("idrisBuild", ExecJar::class) {
//    workingDir = File("${projectDir}/src/main/idris")
    val idrisCompilerJar = idrisCompiler.find { it.name.startsWith("idris-jvm-compiler") }!!
    setJar(objects.property(File::class.java).value(idrisCompilerJar))
//    setClasspath(objects.property(FileCollection::class.java).value(sourceSets.main.get().runtimeClasspath))
//    classpath(idrisJar, sourceSets.main.get().runtimeClasspath)
    val idrisRuntimeJar = idrisCompiler.find { it.name.startsWith("idris-jvm-runtime") }!!
    setClasspath(objects.property(FileCollection::class.java).value(files(idrisRuntimeJar)))
    setArgs(objects.listProperty(String::class.java).value(listOf("--build", ipkg)))

    setMainClass(objects.property(String::class.java).value("idris2.Main"))

    inputs.files(fileTree("${projectDir}/src/main/idris"))
//    outputs.files(fileTree("dir" to "${projectDir}/src/main/idris", "include" to "**/*.ibc"))
    outputs.files(fileTree("${buildDir}/classes/idris"))
}

task("idrisClean", Exec::class) {
    workingDir = File("${projectDir}/src/main/idris")

    val idrisJar = idrisCompiler.find { it.name.startsWith("idris-jvm-compiler") }!!
//    classpath(idrisJar)
    executable = "java"
//    val classpath = sourceSets.main.get().compileClasspath.asPath
    val arguments = listOf("-jar", idrisJar.absolutePath, /*"-cp", classpath,*/ "--clean", ipkg)
    setArgs(arguments)
//    println(arguments)

    inputs.files(fileTree("${projectDir}/src/main/idris"))
    outputs.files(fileTree("dir" to "${projectDir}/src/main/idris", "include" to "**/*.ibc"))
}

tasks.named("clean") {
    dependsOn(":idrisClean")
}

tasks.named("build") {
    dependsOn(":idrisBuild")
}

tasks.named("run") {
    dependsOn(":idrisBuild")
}

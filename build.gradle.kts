import com.github.gradle.node.pnpm.task.PnpmTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val version: String by project
val jacksonVersion: String by project
val commonmarkVersion: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.node-gradle.node") version "7.0.2"
    id("run.halo.plugin.devtools") version "0.0.9"
}

group = "io.sakurasou.halo.typecho"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/releases")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation(platform("run.halo.tools.platform:plugin:2.11.0-SNAPSHOT"))
    compileOnly("run.halo.app:api")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("org.commonmark:commonmark:0.22.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:$commonmarkVersion")
    implementation("org.commonmark:commonmark-ext-heading-anchor:$commonmarkVersion")
    implementation("com.belerweb:pinyin4j:2.5.1")

    testImplementation("run.halo.app:api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

node {
    nodeProjectDir.set(file("${project.projectDir}/ui"))
}

tasks.register("buildFrontend", PnpmTask::class.java) {
    args.add("build")
}

tasks.getByName<JavaCompile>("compileJava")
    .dependsOn(tasks.getByName("buildFrontend"))
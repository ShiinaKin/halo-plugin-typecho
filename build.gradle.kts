import com.github.gradle.node.pnpm.task.PnpmTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    id("com.github.node-gradle.node") version "7.0.2"
    id("run.halo.plugin.devtools") version "0.0.7"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")

    implementation(platform("run.halo.tools.platform:plugin:2.11.0-SNAPSHOT"))
    compileOnly("run.halo.app:api")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.commonmark:commonmark:0.22.0")

    testImplementation("run.halo.app:api")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
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
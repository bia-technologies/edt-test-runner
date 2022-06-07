import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    `java-library`
    id("com.github.hierynomus.license") version "0.16.1"
    id("it.filippor.p2") version "0.0.10"
}

group = "ru.biatech.edt.xtest"
version = "0.2.6"
repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Copy>() {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
        resources.srcDirs("src/main/resources")
        resources.srcDirs("META-INF")
    }
    test {
        java.srcDirs("src/test/java")
        resources.srcDirs("src/test/resources")
    }
}

val edtLocation = findProperty("edtLocation") ?: ""

dependencies {
    implementation(fileTree(edtLocation) { include("*.jar") })
}

license {
    header = rootProject.file("templates/HEADER.txt")
    ext["year"] = "2021-" + Calendar.getInstance().get(Calendar.YEAR)
    ext["owner"] = "BIA-Technologies Limited Liability Company"
    useDefaultMappings = false
    includes(listOf("**/*.java", "**/*.properties", "**/*.gradle.kts"))
    strictCheck = true
    mapping("java", "SLASHSTAR_STYLE")
}

tasks.register<com.hierynomus.gradle.license.tasks.LicenseFormat>("licenseEclipseProject") {
    header = rootProject.file("templates/HEADER_FOR_PLUGIN_TEMPLATE.txt")
    ext["year"] = "2021-" + Calendar.getInstance().get(Calendar.YEAR)
    ext["owner"] = "BIA-Technologies Limited Liability Company"
    source = fileTree("eclipse_project")
    useDefaultMappings = true
    strictCheck = true
    setIncludes(listOf("**/*.properties", "**/*.xml"))
    group = "license"
}

tasks.named("licenseFormat") {
    dependsOn(tasks.named("licenseEclipseProject"))
}

val pluginBuildPath = layout.buildDirectory.dir("buildPlugin").get().asFile

tasks.register<Copy>("buildPlugin-copyFiles") {
    // TODO: Добавить очистку каталога сборки
    from("templates/eclipse_project")

    into(pluginBuildPath)

    from(layout.projectDirectory.dir("src/main/java")) {
        into("bundles/test_runner/src")
    }
    from(layout.projectDirectory.dir("src/main/resources")) {
        into("bundles/test_runner/resources")
    }
    from(layout.projectDirectory.dir("META-INF")) {
        into("bundles/test_runner/META-INF")
    }
    from(layout.projectDirectory.dir("plugin.xml")) {
        into("bundles/test_runner")
    }
    group = "build"
}

tasks.register<Exec>("buildPlugin") {
    isIgnoreExitValue = true
    workingDir = pluginBuildPath
    standardOutput = System.out

    environment("MAVEN_OPTS", "-Dhttps.protocols=TLSv1.2")

    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        commandLine("mvn.cmd", "package")
    } else {
        commandLine("mvn", "package")
    }

    dependsOn(tasks.named("buildPlugin-copyFiles"))
    group = "build"
}

var publishTo = (findProperty("publishTo") ?: "").toString()

tasks.register<Copy>("publishToPath") {
    doFirst{
        if(publishTo==""){
            throw GradleException("You must specify a property 'publishTo' for the publish task is 'gradle.properties'")
        }
    }
    from("$pluginBuildPath/repositories/ru.biatech.edt.xtest.repository/target/repository"){
        into("$version")
        into("latest")
    }
    from("$pluginBuildPath/repositories/ru.biatech.edt.xtest.repository/target/ru.biatech.edt.xtest.repository.zip"){
        into("$version")
        into("latest")
    }
    into(publishTo)
    group = "publish"
    dependsOn(tasks.named("buildPlugin"))

    doLast{
        print("Published to: $publishTo")
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "7.1.1"
}
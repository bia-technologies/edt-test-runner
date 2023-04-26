import org.apache.tools.ant.taskdefs.condition.Os
import java.io.ByteArrayOutputStream
import java.util.*

plugins {
    `java-library`
    id("com.github.hierynomus.license") version "0.16.1"
    id("it.filippor.p2") version "0.0.10"
}

group = "ru.biatech.edt.xtest"
version = "22.10.0"
val vendor = "BIA-Technologies Limited Liability Company"
val createProjectYear = 2021
val licenseYear = if (Calendar.getInstance().get(Calendar.YEAR) == createProjectYear) "$createProjectYear"
else "$createProjectYear-${Calendar.getInstance().get(Calendar.YEAR)}"
val edtLocation = findProperty("edtLocation") ?: ""
val pluginBuildPath = layout.buildDirectory.dir("buildPlugin").get().asFile
val publishTo = (findProperty("publishTo") ?: "").toString()

var subProjects = arrayOf("viewer")
val mvnCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) "mvn.cmd" else "mvn"

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
        java.srcDirs(subProjects.map { "$it/src/main/java" })
        resources.srcDirs(subProjects.map { "$it/src/main/resources" })
        resources.srcDirs(subProjects.map { "$it/META-INF" })
    }
}

dependencies {
    implementation(fileTree(edtLocation) { include("*.jar") })
}

license {
    header = rootProject.file("templates/HEADER.txt")
    ext["year"] = licenseYear
    ext["owner"] = vendor
    useDefaultMappings = false
    includes(listOf("**/*.java", "**/*.properties", "**/*.gradle.kts", "**/*.xml"))
    strictCheck = true
    mapping("xml", "XML_STYLE")
    mapping("java", "SLASHSTAR_STYLE")
}

tasks.register<com.hierynomus.gradle.license.tasks.LicenseFormat>("licenseEclipseProject") {
    header = rootProject.file("templates/HEADER_FOR_PLUGIN_TEMPLATE.txt")
    ext["year"] = licenseYear
    ext["owner"] = vendor
    source = fileTree("templates/eclipse_project")
    useDefaultMappings = true
    strictCheck = true
    setIncludes(listOf("**/*.properties", "**/*.xml"))
    group = "license"
}

tasks.named("licenseFormat") {
    dependsOn(tasks.named("licenseEclipseProject"))
}

tasks.register<Copy>("buildPlugin-copyFiles") {
    // TODO: Добавить очистку каталога сборки
    from("templates/eclipse_project")

    into(pluginBuildPath)

    subProjects.forEach {
        var sourceDir = layout.projectDirectory.dir(it)
        from(sourceDir) {
            into("bundles/$it")
        }
    }

    group = "build"
}

tasks.register<Exec>("download-lombok-plugin") {
    workingDir = pluginBuildPath
    standardOutput = System.out


    environment("MAVEN_OPTS", "")
    commandLine(mvnCommand, "clean", "dependency:copy@get-lombok")
    group = "build"
    dependsOn(tasks.named("buildPlugin-copyFiles"))
}

tasks.register<Exec>("buildPlugin") {
    workingDir = pluginBuildPath
    standardOutput = System.out

    environment("MAVEN_OPTS", "-javaagent:target/lombok.jar=ECJ -Dhttps.protocols=TLSv1.2")
    commandLine(mvnCommand, "dependency:resolve", "package")

    dependsOn(tasks.named("download-lombok-plugin"))
    group = "build"
}

tasks.register<Copy>("publishToPath") {
    doFirst {
        if (publishTo == "") {
            throw GradleException("You must specify a property 'publishTo' for the publish task is 'gradle.properties'")
        }
    }
    from("$pluginBuildPath/repositories/repository/target/repository") {
        into("$version")
        into("latest")
    }
    from("$pluginBuildPath/repositories/repository/target/repository.zip") {
        into("$version")
        into("latest")
    }
    into(publishTo)
    group = "publish"
    dependsOn(tasks.named("buildPlugin"))

    doLast {
        print("Published to: $publishTo")
    }
}

tasks.register<Exec>("publishPlugin") {
    isIgnoreExitValue = true
    workingDir = pluginBuildPath
    standardOutput = System.out

    val ghPagesPath = layout.buildDirectory.dir("buildPlugin").get().asFile
    commandLine(mvnCommand, "dependency:resolve", "deploy", "-Prelease-composite")

    dependsOn(tasks.named("buildPlugin-copyFiles"))
    group = "build"
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = "7.1.1"
}
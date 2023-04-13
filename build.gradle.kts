plugins {
    id("java")
    id("application")
}

group = "com.jaspervanmerle.tcmm141"
version = "1.0.0"

project.projectDir.resolve("src/main/java/com/jaspervanmerle/tcmm141").listFiles()?.forEach {
    task<Jar>("${it.name}-jar") {
        group = "solver"

        from(sourceSets["main"].runtimeClasspath)

        archiveBaseName.set(it.name)
        archiveVersion.set("")

        manifest {
            attributes["Main-Class"] = "com.jaspervanmerle.tcmm141.${it.name}.TrafficController"
        }
    }

    task<JavaExec>("${it.name}-run") {
        group = "solver"

        classpath = sourceSets["main"].runtimeClasspath
        standardInput = System.`in`

        mainClass.set("com.jaspervanmerle.tcmm141.${it.name}.TrafficController")
    }
}

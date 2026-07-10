apply(plugin = "maven-publish")

val javadocJar = tasks.getByName("javadocJar")
val sourcesJar = tasks.getByName("sourcesJar")

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("release") {
            artifact(sourcesJar)
            artifact(javadocJar)
            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set("Koin")
                description.set("KOIN - Kotlin simple Dependency Injection Framework. tvOS-enabled build of InsertKoinIO/koin (Apache-2.0)")
                url.set("https://github.com/sajidalidev/koin")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/sajidalidev/koin")
                    connection.set("scm:git:https://github.com/sajidalidev/koin.git")
                }
                developers {
                    developer {
                        name.set("Arnaud Giuliani")
                        email.set("arnaud@kotzilla.io")
                    }
                    developer {
                        name.set("Sajid Ali")
                        email.set("sajidhanif865@gmail.com")
                    }
                }
            }
        }
    }
}


apply(from = file("../../gradle/signing.gradle.kts"))
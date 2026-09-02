apply(plugin = "signing")

// tvOS-only publications: consumers get every other target from the official
// io.insert-koin upstream; this fork only fills the tvOS gap. Restricting
// which publications actually publish keeps us under Maven Central's file quota.
// NOTE: filtered by task name, not by reading AbstractPublishToMaven.getPublication()
// directly - that property is discarded from configuration-cache state (and is not
// guaranteed to be assigned yet at task-creation time either), so an onlyIf/eager read
// of `publication` throws/NPEs. Publish task names follow the stable Gradle convention
// `publish<PublicationName>PublicationTo<...>`, which is safe to match on.
val forkPublicationNames = listOf("KotlinMultiplatform", "TvosArm64", "TvosSimulatorArm64")
val allowedPublicationTaskNamePrefixes = forkPublicationNames.map { "publish${it}PublicationTo" }
val allowedSignTaskNames = forkPublicationNames.map { "sign${it}Publication" }
// Fork scope: only the modules whose official io.insert-koin release ships NO tvOS artifacts.
// Everything else (koin-core, koin-core-viewmodel since 4.2.2, ...) is consumed from upstream.
val forkPublishedProjects = setOf(
    ":compose:koin-compose",
    ":compose:koin-compose-viewmodel",
)
val inForkScope = project.path in forkPublishedProjects
tasks.withType<AbstractPublishToMaven>().configureEach {
    onlyIf {
        inForkScope && allowedPublicationTaskNamePrefixes.any { name.startsWith(it) }
    }
}

fun isReleaseBuild(): Boolean = System.getenv("IS_RELEASE") == "true" || false

fun getSigningKeyId(): String = findProperty("SIGNING_KEY_ID")?.toString() ?: System.getenv("SIGNING_KEY_ID") ?: ""

fun getSigningKey(): String = findProperty("SIGNING_KEY")?.toString() ?: System.getenv("SIGNING_KEY") ?: ""

fun getSigningPassword(): String =
    findProperty("SIGNING_PASSWORD")?.toString() ?: System.getenv("SIGNING_PASSWORD") ?: ""

if (isReleaseBuild()) {

    // Upstream depends on ALL Sign tasks from every publish task (KT-46466 workaround). For the
    // tvOS-only fork that drags signAndroidReleasePublication -> bundleReleaseAar ->
    // checkReleaseAarMetadata into every tvOS publish graph, which fails against compose 1.12.0's
    // Android artifacts (they require compileSdk 37 / AGP 9.1). Only the Sign tasks of the
    // publications this fork actually publishes are needed - the signing plugin already attaches
    // each publication's own signatures as artifacts of that publication.
    val forkSignTasks = tasks.withType<Sign>().matching { it.name in allowedSignTaskNames }

    tasks.withType<PublishToMavenLocal>().configureEach {
        dependsOn(forkSignTasks)
    }
    tasks.matching { it.name.endsWith("ToSonatypeRepository") }.configureEach {
        dependsOn(forkSignTasks)
    }
    tasks.matching { it.name.endsWith("ToNmcpRepository") }.configureEach {
        dependsOn(forkSignTasks)
    }

    configure<SigningExtension> {
        useInMemoryPgpKeys(
            getSigningKeyId(),
            getSigningKey(),
            getSigningPassword(),
        )

        sign(extensions.getByType<PublishingExtension>().publications)
    }
}
plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "dev.xqedii"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "dev.xqedii"

    minecraft {
        registerVersion(versions.toTypedArray()) {
            runs {
                getByName("client") {
                    // When the property is set to true, you can log in with a Minecraft account
                    // devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "offlinecosmetics"
        displayName = "Offline Cosmetics"
        author = "Xqedii, TotallyFuz"
        description = "Restores client-side visibility of cosmetics on cracked servers."
        minecraftVersion = "*"
        version = rootProject.version.toString()

        iconPath("assets/offlinecosmetics/icon.png") 
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")

    group = rootProject.group
    version = rootProject.version

    // The version modules compile against the Java version their Minecraft version runs on
    // (Java 25 for 26.1+), so the shared modules have to stay on the oldest one instead of
    // following the build JDK.
    extensions.findByType(JavaPluginExtension::class.java)?.apply {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

tasks.withType<org.gradle.api.tasks.bundling.Jar>().configureEach {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

// The ordinary root JAR is only an intermediate artifact. LabyGradle's
// createReleaseJar task is what combines core + all versioned game-runner
// implementations into the single JAR that can be installed in LabyMod.
tasks.register("buildRelease") {
    group = "build"
    description = "Builds the addon and creates the production-ready release JAR."
    dependsOn("build")
    finalizedBy("createReleaseJar")
}

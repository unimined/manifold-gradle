package xyz.wagyourtail.manifold

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.PluginAware
import org.gradle.api.tasks.compile.JavaCompile
import xyz.wagyourtail.commons.core.logger.Logger
import xyz.wagyourtail.commons.core.logger.SimpleLogger
import xyz.wagyourtail.commons.core.logger.prefix.LoggingPrefix
import xyz.wagyourtail.commons.gradle.GradleLogWrapper
import xyz.wagyourtail.commonskt.properties.FinalizeOnRead
import xyz.wagyourtail.manifold.plugin.ManifoldExtension
import xyz.wagyourtail.manifold.plugin.manifold
import xyz.wagyourtail.manifold.settings.ManifoldSettingsExtension
import kotlin.jvm.java

class GradlePlugin : Plugin<PluginAware> {

    companion object {
        fun JavaCompile.addManifoldArgs() {
            if (options.compilerArgs.none { "-Xplugin:Manifold" in it }) {
                options.compilerArgs.add("-Xplugin:Manifold ${project.manifold.pluginArgs.get().joinToString(" ")}")
            }
        }

        private val logPrefix = LoggingPrefix.builder()
            .loggerName("Manifold")
            .includeLevel(false)
            .includeThreadName(false)
            .includeTime(false)
            .build()

        val pluginVersion by FinalizeOnRead(GradlePlugin::class.java.`package`.implementationVersion ?: "unknown")

        private var printedMessage = false
    }

    override fun apply(target: PluginAware) {
        when (target) {
            is Project -> apply(target)
            is Settings -> apply(target)
            else -> throw IllegalArgumentException("Unsupported target type: ${target::class.java}")
        }
    }

    fun apply(project: Project) {
        val logger = GradleLogWrapper(logPrefix, project.logger)

        logger.printMessage()

        project.extensions.create("manifold", ManifoldExtension::class.java)

        project.afterEvaluate {
            project.tasks.withType(JavaCompile::class.java).configureEach {
                it.addManifoldArgs()
            }
        }

        project.repositories.exclusiveContent {
            it.forRepository {
                project.repositories.mavenCentral { r ->
                    r.name = "Central (Manifold)"
                }
            }

            it.filter { r ->
                r.includeGroupAndSubgroups("systems.manifold")
            }
        }
    }

    fun apply(settings: Settings) {
        val logger = SimpleLogger.builder().prefix(logPrefix).build()

        logger.printMessage()
        settings.extensions.create("manifold", ManifoldSettingsExtension::class.java, settings)
    }

    private fun Logger.printMessage() {
        if (!printedMessage) {
            this.lifecycle("Loaded Manifold Plugin $pluginVersion")
            printedMessage = true
        }
    }
}
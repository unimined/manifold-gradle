package xyz.wagyourtail.manifold.plugin

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

val Project.manifold: ManifoldExtension
    get() = extensions.getByType(ManifoldExtension::class.java)

val Project.manifoldMaybe: ManifoldExtension?
    get() = extensions.findByType(ManifoldExtension::class.java)

abstract class ManifoldExtension @Inject constructor(val project: Project) {

    abstract val version: Property<String>

    abstract val pluginArgs: ListProperty<String>

    init {
        val parentVersion = project.parent?.manifoldMaybe?.version
        if (parentVersion != null) {
            version.convention(parentVersion)
        }
        version.finalizeValueOnRead()

        pluginArgs.finalizeValueOnRead()

        project.afterEvaluate {
            if (subprojectPreprocessorInitialized) {
                subprojectPreprocessorConfig.apply()
            }
            if (preprocessorInitialized) {
                preprocessorConfig.apply()
            }
        }
    }

    @JvmName("module")
    operator fun invoke(name: String) =
        project.dependencies.create("systems.manifold:manifold-$name:${version.get()}")


    private var preprocessorInitialized = false
    val preprocessorConfig: PreprocessorConfigList by lazy {
        preprocessorInitialized = true
        PreprocessorConfigList(project, this)
    }

    fun preprocessor(config: PreprocessorConfigList.() -> Unit) {
        preprocessorConfig.apply(config)
    }

    fun preprocessor(
        @DelegatesTo(
            strategy = Closure.DELEGATE_FIRST,
            value = PreprocessorConfigList::class
        )
        action: Closure<*>
    ) {
        preprocessor {
            action.delegate = this
            action.resolveStrategy = Closure.DELEGATE_FIRST
            action.call(this)
        }
    }

    private var subprojectPreprocessorInitialized = false
    val subprojectPreprocessorConfig by lazy {
        subprojectPreprocessorInitialized = true
        SubprojectPreprocessorConfig(project)
    }

    fun subprojectPreprocessor(
        config: SubprojectPreprocessorConfig.() -> Unit
    ) {
        subprojectPreprocessorConfig.apply(config)
    }

    fun subprojectPreprocessor(
        @DelegatesTo(
            strategy = Closure.DELEGATE_FIRST,
            value = SubprojectPreprocessorConfig::class
        )
        action: Closure<*>
    ) {
        subprojectPreprocessor {
            action.delegate = this
            action.resolveStrategy = Closure.DELEGATE_FIRST
            action.call(this)
        }
    }

}
## Manifold Gradle Plugin

This is an unofficial gradle plugin for manifold, https://manifold.systems.

### Usage

To use this plugin, since I haven't put it on the gradle plugin portal yet, you will need to add my maven to your `settings.gradle`:

```groovy
pluginManagement {
    repositories {
        maven {
            url = "https://maven.wagyourtail.xyz/releases"
        }
        maven {
            url = "https://maven.wagyourtail.xyz/snapshots"
        }
        gradlePluginPortal()
    }
}
```

#### Basic

```groovy
plugins {
    id 'xyz.wagyourtail.manifold' version "${pluginVersion}"
}

manifold {
    version = "${manifoldVersion}"
    
    // specify arguments to add to the compiler plugin:
    pluginArgs.add("--no-bootstrap")
}

dependencies {
    // you can add runtime components like
    implementation(manifold.module("ext-rt"))
    
    // you can add modules
    annotationProcessor(manifold.module("preprocessor"))
    annotationProcessor(manifold.module("ext"))
    
    // manifold also provides an "all" artifact
    annotationProcessor(manifold.module("all"))

    // in kotlin dsl, call `manifold` like a function:
    // annotationProcessor(manifold("preprocessor"))
}

```

#### Preprocessor

The plugin also provides some helpers for preprocessor configuration.

```groovy

manifold {
    version = "${manifoldVersion}"
    
    preprocessor {
        // default value, can also be set in gradle properties with the `manifold.ideActiveConfig` property
        ideActiveConfig = "" 
        
        // default config, (name ""), this configures the built-in jar/processRresources/compileJava tasks
        config {
            property("DEBUG")
            property("EXAMPLE", "true")
        }
        
        config("release") {
            // will inherit properties from default config
            property("RELEASE")
            // property("DEBUG") is still set, 
            // in a future release, I will allow unsetting if manifold adds support for un-setting properties on the cli
            
            // creates a jar task for this config
            jar {}
        }
    }
}
```


#### Subprojects

there is some helper function for subproject preprocessor configuration. this is where multiple projects share the same source directories.
this can be used to have things like different sets of dependencies or otherwise different configs with seperate outputs.

to configure this, you can either use the helper in `settings.gradle`, or configure it directly in the parent project's
`build.gradle`

`settings.gradle`
```groovy

plugins {
    id "xyz.wagyourtail.manifold" version "${pluginVersion}"
}

manifold {
    subprojectPreprocessor {
        sourceSet("main") // add a sourceSet to share
        // add sourceSet, manually specify shared directory relative to root project's dir
        sourceSet("test", "manifold/test")

        buildFile("debug.gradle.kts") // set a shared build.gradle file for use by the subprojects

        // set the active subproject, can also be set with the `manifold.ideActiveSubproject` gradle property
        // defaults to the first subproject
        ideActiveSubproject = "debug-true"

        // add subprojects
        project("debug-true") {
            // you can directly configure their preprocessor config in the settings, or you can do so in the build.gradle file
            property("DEBUG", true)
        }

        project("debug-false") {
            property("DEBUG", false)
        }

    }
}
```

and/or

`build.gradle`
```groovy

manifold {
    version = "${manifoldVersion}"
    
    subprojectPreprocessor {
        // set the active subproject, can also be set with the `manifold.ideActiveSubproject` gradle property
        // defaults to the first subproject
        ideActiveSubproject = "debug-true"
        
        // add subprojects
        subproject("debug-true") {
            // you can directly configure their preprocessor config in the settings, or you can do so in the build.gradle file
            property("DEBUG", true)
        }
        
        subproject("debug-false") {
            property("DEBUG", true)
        }
        
    }
}

```


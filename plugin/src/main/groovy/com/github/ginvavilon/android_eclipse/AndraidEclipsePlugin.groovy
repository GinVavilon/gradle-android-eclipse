package com.github.ginvavilon.android_eclipse

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.eclipse.model.EclipseProject

public class AndraidEclipsePlugin implements Plugin<Project> {

    Project mProject

    @Override
    void apply(final Project project) {

        mProject=project
        //project.apply(plugin: 'android')
        project.apply(plugin: 'eclipse')
        project.configurations {androidEclipse}
        project.configurations {variantEclipseConfiguration}
        project.configurations {testVariantEclipseConfiguration}
        project.configurations {excludeByVariant}
        updateEclipse(project.eclipse)
        def buildDir=project.buildDir

        AndroidEclipseExtension extension = new AndroidEclipseExtension()
        extension.eclipse = project.eclipse
        extension.generatedDirs+=[
            "$buildDir/generated/source/r",
            "$buildDir/generated/source/buildConfig",
            "$buildDir/generated/source/aidl",
            "$buildDir/generated/source/apt",
            "$buildDir/generated/source/rs"
        ]


        project.extensions.add("androidEclipse", extension)
        def variants

        if(project.android.hasProperty("applicationVariants")){
            variants = project.android.applicationVariants
        } else if(project.android.hasProperty("libraryVariants")){
            variants = project.android.libraryVariants
        } else{
            throw new RuntimeException("Problem with android plugin")
        }

        project.afterEvaluate {
            def varianProperty = new VariantProperty(project)
            def variant=varianProperty.selectOrLoad(variants)

            AndroidEclipseVariantConfigurator configurator=new AndroidEclipseVariantConfigurator()
            configurator.variant = variant
            configurator.eclipse =  project.eclipse
            configurator.androidPlugin = project.android
            configurator.project = project
            configurator.run()

        }

        variants.all { androidVariant ->

            def vname="$androidVariant.name".capitalize()

            def eclipseTask=project.task("eclipse$vname",
            type: AndroidEclipseTask ,
            group : 'IDE',
            description : "Generates all Eclipse files $androidVariant.description "

            )

            eclipseTask.finalizedBy 'eclipse'

            eclipseTask.doFirst {
                variant = androidVariant
                eclipse = project.eclipse
                androidPlugin = project.android
            }

            def fullTask=project.task("eclipseAndroid$vname",
            group : 'IDE',
            description : "Clear and generates all eclipse files $androidVariant.description ",
            dependsOn : [
                project.cleanEclipse,
                androidVariant.hasProperty('javaCompileProvider')?androidVariant.javaCompileProvider:androidVariant.javaCompiler.dependsOn
                ]
            )

            fullTask.finalizedBy eclipseTask
        }

    }

    private updateEclipse(EclipseModel eclipse)
    {

        updateEclipseProject(eclipse.project)
        updateEclipseClasspath(eclipse.classpath)

    }


    private updateEclipseClasspath(EclipseClasspath classpath) {

        classpath.downloadSources = true
        classpath.downloadJavadoc = true
        classpath.containers.clear()
        classpath.sourceSets.clear()
        classpath.defaultOutputDir = new File(mProject.buildDir,'eclipse')
        //      classpath.containers.add('com.android.ide.eclipse.adt.ANDROID_FRAMEWORK')
    }


    private updateEclipseProject(EclipseProject project) {
        project.natures.clear()
        project.natures 'org.eclipse.buildship.core.gradleprojectnature'
        project.buildCommand 'org.eclipse.buildship.core.gradleprojectbuilder'
        //        project.natures 'com.android.ide.eclipse.adt.AndroidNature'
        project.natures 'org.eclipse.jdt.core.javanature'

    }
}

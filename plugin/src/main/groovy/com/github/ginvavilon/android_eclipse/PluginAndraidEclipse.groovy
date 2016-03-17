package com.github.ginvavilon.android_eclipse

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.eclipse.model.EclipseProject

public class PluginAndraidEclipse implements Plugin<Project> {

    Project mProject

    @Override
    void apply(final Project project) {

        mProject=project
        //project.apply(plugin: 'android')
        project.apply(plugin: 'eclipse')
        project.configurations {androidEclipse}
        project.configurations {libsFromVariant}
        updateEclipse(project.eclipse)
        def buildDir=project.buildDir;

        AndroidEclipseExtension extension = new AndroidEclipseExtension();
        extension.generatedDirs+=[
         "$buildDir/generated/source/buildConfig",
         "$buildDir/generated/source/aidl",
         "$buildDir/generated/source/rs"]


        project.extensions.add("androidEclipse", extension)

        def variants;

        if(project.android.hasProperty("applicationVariants")){
              variants = project.android.applicationVariants;
        } else if(project.android.hasProperty("libraryVariants")){
              variants = project.android.libraryVariants;
        } else{
            throw new RuntimeException("Problem with android plugin");
        }


        variants.all { androidVariant ->

                def vname="$androidVariant.name".capitalize()
                def task=project.task("eclipse$vname",
                type: AndroidEclipseTask ,
                group : 'IDE',
                description : "Generates all Eclipse files $androidVariant.description ",
                dependsOn : androidVariant.javaCompiler.dependsOn
                )

                task.finalizedBy 'eclipse'

                task.doFirst {
                    variant = androidVariant
                    eclipse = project.eclipse
                }
        }

    }

    private updateEclipse(EclipseModel eclipse)
    {

        updateEclipseProject(eclipse.project)
        updateEclipseClasspath(eclipse.classpath)


        eclipse.classpath {
            file {
                withXml {
                    Node node = it.asNode()
                    node.appendNode('classpathentry', [kind: 'src', path: 'gen'])
                }
            }
        }

    }


    private updateEclipseClasspath(EclipseClasspath classpath) {

        classpath.downloadSources = true
        classpath.downloadJavadoc = true
        classpath.containers.clear()
        classpath.sourceSets.clear();
        classpath.defaultOutputDir = new File(mProject.buildDir,'eclipse')
        classpath.containers.add('com.android.ide.eclipse.adt.ANDROID_FRAMEWORK')
    }


    private updateEclipseProject(EclipseProject project) {
        project.natures.clear()
        project.natures 'org.eclipse.buildship.core.gradleprojectnature'
        project.natures 'com.android.ide.eclipse.adt.AndroidNature'
        project.natures 'org.eclipse.jdt.core.javanature'

    }
}

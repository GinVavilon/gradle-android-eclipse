package com.github.ginvavilon.android_eclipse

import java.util.HashSet

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ide.eclipse.model.EclipseModel

import com.android.build.gradle.api.ApplicationVariant


class AndroidEclipseTask extends DefaultTask {

    static final String SOURCES_GENERATED = 'generated'

    static final String PREFIX_SOURCESETS = 'android_'


    static final String PROJECT_PROPERTY_COMMENTS='# This file is automatically generated by Building Tools.\n'+
'# Do not modify this file -- YOUR CHANGES WILL BE ERASED!\n'+
'#\n'+
'# This file must not be checked in Version Control Systems.\n'+
'#\n'+
'# To customize properties used by the Ant build system edit\n'+
'# "ant.properties", and override values to adapt the script to your\n'+
'# project structure.\n'+
'#\n'+
'# To enable ProGuard to shrink and obfuscate your code, uncomment this (available properties: sdk.dir, user.home):\n'+
'#proguard.config=${sdk.dir}/tools/proguard/proguard-android.txt:proguard-project.txt\n'+
'\n'+
'# Project target.';

    public EclipseModel eclipse
    public ApplicationVariant variant


    @TaskAction
    void run() {
        updateProjectProperties()
        

        def configurations = project.configurations


        def buildDir=project.buildDir;

        def eclipseProject = eclipse.project
        def pathVariant = variant.dirName
        def manifestFile=new File("$buildDir/intermediates/manifests/full/$pathVariant/AndroidManifest.xml");
        def resFile=new File("$buildDir/intermediates/res/merged/$pathVariant/AndroidManifest.xml");
        def rFile=new File("$buildDir/generated/source/r/$pathVariant");

        eclipseProject.linkedResource(name: 'AndroidManifest.xml', type: '1', location: manifestFile.absolutePath);
        eclipseProject.linkedResource(name: 'gen', type: '2', location: rFile.absolutePath);



        final def eclipseClasspathSourceSets = eclipse.classpath.sourceSets
        def libs=new HashSet()
        def configLibs=new HashSet()
        variant.sourceSets.each { sourceSet ->
            final def name=sourceSet.name.toString()

            SourceSet mainSourceSet = eclipseClasspathSourceSets.findByName(PREFIX_SOURCESETS+name);
            if (mainSourceSet==null){
                mainSourceSet = eclipseClasspathSourceSets.create(PREFIX_SOURCESETS+name);
            }
            sourceSet.javaDirectories.each { dir->
                 mainSourceSet.getJava().srcDir(dir);

            }
            def conf=configurations.findByName(name+'Compile');
            if (conf!=null){
                project.dependencies{
                    libsFromVariant conf
                    configLibs+=conf.files;
                }
            }

        }

            variant.compileLibraries.each{file ->
                if (file.exists()){
                    libs.add(file)
                }

            }


        libs-=configLibs;
        libs-= configurations.compile.files
        libs-= configurations.androidEclipse.files
        project.dependencies{

            libsFromVariant configurations.compile
            libsFromVariant configurations.androidEclipse
            libsFromVariant project.files(libs)
        }




        eclipse.classpath.plusConfigurations.add(configurations.libsFromVariant)

        def generatedSourceSets = eclipseClasspathSourceSets.create(SOURCES_GENERATED)

        AndroidEclipseExtension ext=project.extensions.getByName('androidEclipse')
        ext.generatedDirs.each { dir ->
            generatedSourceSets.getJava().srcDir(project.file("$dir/$pathVariant"));
        }


    }

    private updateProjectProperties() {
        Properties props = new Properties()
        File propsFile = new File(project.projectDir,'project.properties')
        if (!propsFile.exists()){
        	propsFile.createNewFile()
        }
        props.load(propsFile.newDataInputStream())
        props.setProperty('target', project.android.compileSdkVersion)

        def properiesWriter = new PrintWriter(propsFile.newWriter())

        properiesWriter.println(PROJECT_PROPERTY_COMMENTS)

        props.each { k, v ->
            properiesWriter.print(k)
            properiesWriter.print('=')
            properiesWriter.println(v)
        }
        properiesWriter.flush()
        properiesWriter.close()
    }


}
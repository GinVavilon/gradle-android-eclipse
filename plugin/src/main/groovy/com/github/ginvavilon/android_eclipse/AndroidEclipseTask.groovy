package com.github.ginvavilon.android_eclipse

import java.util.HashSet

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.plugins.ide.eclipse.model.EclipseModel

import com.android.build.gradle.api.ApplicationVariant


class AndroidEclipseTask extends DefaultTask {

    static final String MANIFEST = 'AndroidManifest.xml'
    static final String RES = 'res'

    static final String SOURCES_GENERATED = 'generated'
    static final String SOURCES_LINKED = 'linked'

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
    public def variant
    public def androidPlugin
    

    @TaskAction
    void run() {
        updateProjectProperties()
        AndroidEclipseExtension ext=project.extensions.getByName('androidEclipse')

        def configurations = project.configurations
        def library = androidPlugin in com.android.build.gradle.LibraryExtension;

        def buildDir=project.buildDir;

        def eclipseProject = eclipse.project
        def pathVariant = variant.dirName
        //def manifestFile=new File("$buildDir/intermediates/manifests/full/$pathVariant/AndroidManifest.xml");
        def manifestFile
        if (ext.manifest == null){
            manifestFile = null
        } else if (ext.manifest == AndroidEclipseExtension.GENERATED){
            if (library){
                manifestFile=new File("$buildDir/intermediates/manifests/aapt/$pathVariant/AndroidManifest.xml");
            } else {
                manifestFile=new File("$buildDir/intermediates/manifests/full/$pathVariant/AndroidManifest.xml");
            }
        } else {
            manifestFile = project.file(ext.manifest
                        .replaceAll('%buildDir%',"$buildDir")
                        .replaceAll('%pathVariant%',"$pathVariant")
                        )
            if (manifestFile.directory){
                manifestFile= new File(manifestFile,MANIFEST)
            }

        }

        def resFile
        if (ext.resLink == null){
            resFile = null
        } else if (ext.resLink == AndroidEclipseExtension.GENERATED){
            resFile=new File("$buildDir/intermediates/res/$pathVariant");
        } else {
            resFile = project.file(ext.resLink)
        }

        def rFile=new File("$buildDir/generated/source/r/$pathVariant");
        if (manifestFile!=null){
            eclipseProject.linkedResource(name: MANIFEST, type: '1', location: manifestFile.absolutePath);
        }
        if (resFile!=null){
            eclipseProject.linkedResource(name: RES, type: '2', location: resFile.absolutePath);
        }
        eclipseProject.linkedResource(name: 'gen', type: '2', location: rFile.absolutePath);



        final def eclipseClasspathSourceSets = eclipse.classpath.sourceSets
        def linkedSourceSets = eclipseClasspathSourceSets.create(SOURCES_LINKED)
        def libs=new HashSet()
        def configLibs=new HashSet()
        def linkedSources=new HashSet()
        def projectAbsolutePath = project.file('.').absolutePath
        
        variant.sourceSets.each { sourceSet ->
            final def name=sourceSet.name.toString()
            SourceSet mainSourceSet = eclipseClasspathSourceSets.findByName(PREFIX_SOURCESETS+name);
            if (mainSourceSet==null){
                mainSourceSet = eclipseClasspathSourceSets.create(PREFIX_SOURCESETS+name);
            }
            sourceSet.javaDirectories.each { dir->
                boolean areRelated = dir.absolutePath.startsWith(projectAbsolutePath);
                if (areRelated){
                    mainSourceSet.getJava().srcDir(dir);
                } else {
                    if (dir.exists()){
                        def path = String.valueOf("src-$name")
                        eclipseProject.linkedResource(name: path, type: '2', location: dir.absolutePath);
                        linkedSources += path;
                    }
                }

            }
            def conf=configurations.findByName(name+'Compile');
            if (conf!=null){
                project.dependencies{
                    libsFromVariant conf
                    configLibs+=conf.files;
                }
            }

        }


         if(variant.hasProperty('javaCompile')){
            variant.javaCompile.classpath.each{file ->
                if (file.exists()){
                    libs.add(file)
                }

            }
        }

        if(variant.hasProperty('compileLibraries')){
            variant.compileLibraries.each{file ->
                if (file.exists()){
                    libs.add(file)
                }

            }
        }
        libs-= configLibs;
        libs-= configurations.compile.files
        libs-= configurations.androidEclipse.files

        clearProject(libs,variant,variant.productFlavors,0,"")
                        
        project.dependencies{

            libsFromVariant configurations.compile
            libsFromVariant configurations.androidEclipse
            libsFromVariant project.files(libs)
        }

        eclipse.classpath.plusConfigurations.add(configurations.libsFromVariant)

        def generatedSourceSets = eclipseClasspathSourceSets.create(SOURCES_GENERATED)


        ext.generatedDirs.each { dir ->
            generatedSourceSets.getJava().srcDir(project.file("$dir/$pathVariant"));
        }
        
        eclipse.classpath {
            file {
                withXml {
                    def node = it.asNode()
                    for (link in linkedSources) {
                        node.appendNode('classpathentry', [kind: 'src', path: link, exported: true])
                    }
                }
        }
    }

    }
    
    private clearProject(def libs,def variant, def flavors, def current , def prefix ){
        if (current<flavors.size()){
            def flavor=flavors.get(current)
            def name = flavor.name
            clearProject(libs,variant,flavors,current+1, appendCapitalizeSuffix(prefix, flavor.name))
            clearProject(libs,variant,flavors,current+1, prefix)
        } else { 
            def typeName= appendCapitalizeSuffix(prefix, variant.buildType.name) 
            clearProject(libs, prefix)
            clearProject(libs, typeName)
        }
    }
    
    private clearProject(def libs, String name){ 
       def compileName = appendCapitalizeSuffix(name, "compile")
       AndroidEclipseExtension ext=project.extensions.getByName('androidEclipse')
       try{ 
            project.configurations[compileName]?.dependencies?.each { dependency -> 
                if (dependency instanceof ProjectDependency){
                    def libProject = dependency.dependencyProject
                
                    if (!ext.classpathJarProjects.contains(libProject)){ 
                        def dir=libProject.buildDir.absolutePath;
                        libs.removeAll {
                            project.file(it).absolutePath.startsWith(dir)
                        }
                    }

                }
            }
        } catch (Exception e) {
        }
        
    }
    
    
    private appendCapitalizeSuffix(def prefix, def suffix){
        if (prefix.empty){
            return prefix+suffix
        } else {
            return prefix + suffix.capitalize() 
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
package com.github.ginvavilon.android_eclipse

class AndroidEclipseExtension {
     public static final String MAIN="src/main/AndroidManifest.xml"
     public static final String main=MAIN

     public static final String ADT = 'com.android.ide.eclipse.adt'
     public static final String ANDMORE = 'org.eclipse.andmore'
     
     public static final String ADT_PLUGIN = 'com.android.ide.eclipse.adt.AndroidNature'
     public static final String ANDMORE_PLUGIN = 'org.eclipse.andmore.AndroidNature'
     public static final String ADT_ANDROID_CLASS_PATH = 'com.android.ide.eclipse.adt.ANDROID_FRAMEWORK'
     public static final String ANDMORE_ANDROID_CLASS_PATH = 'org.eclipse.andmore.ANDROID_FRAMEWORK'
     
     public static final String DISABLED = null
     public static final String disabled = DISABLED

     public static final int AUTO = 0

     public static final int GENERATED = 1
     public static final int BUILD = GENERATED
     public static final int MERGED = GENERATED
     public static final int merged = MERGED
     public static final int generated = GENERATED
     public static final int build = BUILD

    

     def generatedDirs = new HashSet()
     def manifest = MAIN
     def resLink = DISABLED
     def eclipse = null;
     def classpathJarProjects = new HashSet()

     public static String res(String flavor){
        return "src/$flavor/res"
     }

     public static String generated(String type){
        return "%buildDir%/intermediates/manifests/$type/%pathVariant%/AndroidManifest.xml"
     }
     
     public void setPluginType(String type){
        clearAndroidPlugin();
        eclipse.project.natures+= String.valueOf("${type}.AndroidNature");
        eclipse.classpath.containers.add(String.valueOf("${type}.ANDROID_FRAMEWORK"));
         
     }
     
     private void clearAndroidPlugin(){
         eclipse.project.natures-= ADT_PLUGIN
         eclipse.project.natures-= ANDMORE_PLUGIN
         eclipse.classpath.containers.remove(ADT_ANDROID_CLASS_PATH)
         eclipse.classpath.containers.remove(ANDMORE_ANDROID_CLASS_PATH)
          
      }
}
package com.github.ginvavilon.android_eclipse

class AndroidEclipseExtension {
     public static final String MAIN="src/main/AndroidManifest.xml"
     public static final String main=MAIN

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

     public static String res(String flavor){
        return "src/$flavor/res"
     }
}
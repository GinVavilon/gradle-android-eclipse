This is a plugin for Android Project.

## Adding plugin
Add dependencies

```Gradle
buildscript {
    dependencies {
        classpath 'com.github.ginvavilon:android-eclipse:0.8.+'
    }
}
```

Add plugin `android-eclipse` after android plugin

```Gradle
apply plugin: 'com.android.application'
apply plugin: 'android-eclipse'
```
or
```Gradle
apply plugin: 'com.android.library'
apply plugin: 'android-eclipse'
```


## Usage
Available configuration from ['eclipse' plugin](https://docs.gradle.org/current/dsl/org.gradle.plugins.ide.eclipse.model.EclipseModel.html)

```Gradle
eclipse {

  project {
    //if you don't like the name Gradle has chosen
    name = 'someBetterName'

    //if you want to specify the Eclipse project's comment
    comment = 'Very interesting top secret project'

    //if you want to append some extra referenced projects in a declarative fashion:
    referencedProjects 'someProject', 'someOtherProject'
    //if you want to assign referenced projects
    referencedProjects = ['someProject'] as Set

    //if you want to append some extra natures in a declarative fashion:
    natures 'some.extra.eclipse.nature', 'some.another.interesting.nature'
    //if you want to assign natures in a groovy fashion:
    natures = ['some.extra.eclipse.nature', 'some.another.interesting.nature']

    //if you want to append some extra build command:
    buildCommand 'buildThisLovelyProject'
    //if you want to append a build command with parameters:
    buildCommand 'buildItWithTheArguments', argumentOne: "I'm first", argumentTwo: "I'm second"

    //if you want to create an extra link in the eclipse project,
    //by location uri:
    linkedResource name: 'someLinkByLocationUri', type: 'someLinkType', locationUri: 'file://someUri'
    //by location:
    linkedResource name: 'someLinkByLocation', type: 'someLinkType', location: '/some/location'
  }

 classpath {
    //you can tweak the classpath of the Eclipse project by adding extra configurations:
    plusConfigurations += [ configurations.provided ]

    //you can also remove configurations from the classpath:
    minusConfigurations += [ configurations.someBoringConfig ]

    //if you want to append extra containers:
    containers 'someFriendlyContainer', 'andYetAnotherContainer'

    //customizing the classes output directory:
    defaultOutputDir = file('build-eclipse')

    //default settings for downloading sources and Javadoc:
    downloadSources = true
    downloadJavadoc = false
  }

}

```

Configuration for plugin:
```Gradle
androidEclipse{

    //Type of link of manifest (disabled,main,generated,merged) or path of link
    manifest = merged

    //Directories for generated source
    generatedDirs-="$buildDir/generated/source/rs"
    
    // Link to res folder    
    resLink = res("flavor1")
    //resLink = merged
    
    // Plugin for android ADT, ANDMORE or name (like 'com.android.ide.eclipse.adt')
    pluginType = ANDMORE
}

dependencies {
   //library for eclipse (skip compile)
   androidEclipse 'library'

}
```




## Author And License

Copyright 2016, Vladimir Baraznovsky <ginVavilon@gmail.com>. All rights reserved.

This library may be copied only under the terms of the Apache License 2.0, which may be found in the distribution.

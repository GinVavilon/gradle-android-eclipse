package com.github.ginvavilon.android_eclipse

import org.gradle.api.Project
import org.gradle.plugins.ide.eclipse.model.EclipseProject

class SourceContainer {

    final private Set linkedSources = new HashSet()
    final private String projectAbsolutePath

    final private EclipseProject eclipseProject

    public SourceContainer(Project project, EclipseProject eclipseProject) {
        super()
        this.projectAbsolutePath = project.file('.').canonicalPath
        this.eclipseProject = eclipseProject
    }

    public def getProjectPath(File file, String name) {
        boolean areRelated = file.canonicalPath.startsWith(projectAbsolutePath)
        if (areRelated){
            return file
        } else {
            if (file.exists()){
                def path = String.valueOf("ref-$name")
                eclipseProject.linkedResource(name: path, type: '2', location: file.canonicalPath)
                addLinkedSource(path)
            }
        }
    }
    
    public void addLinkedSource(String path) {
        linkedSources.add(path)
    }

    public boolean isLink(String path) {
        return linkedSources.contains(path)
    }

    public Collection<String> getLinkedSources(){
        return linkedSources
    }
}

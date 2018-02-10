package com.github.ginvavilon.android_eclipse

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ide.eclipse.model.EclipseModel


class AndroidEclipseTask extends DefaultTask {

    public EclipseModel eclipse
    public def variant
    public def androidPlugin


    @TaskAction
    void run() {
        AndroidEclipseVariantConfigurator configurator=new AndroidEclipseVariantConfigurator();
        configurator.variant = variant
        configurator.eclipse =  eclipse
        configurator.androidPlugin = androidPlugin
        configurator.project = project
        configurator.run()
    }
}
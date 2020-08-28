package com.jetbrains.exposed.intellij

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import java.io.File

val connection: ProjectConnection by lazy {
    // TODO replace with some actual working method
    val directory = File("")
    GradleConnector.newConnector()
            .forProjectDirectory(directory)
            .connect()
}

class ExposedPluginBuildActivity : StartupActivity {
    override fun runActivity(project: Project) {
        // service initialization
        val service = service<ExposedPluginConnectionService>()

        ApplicationManager.getApplication().invokeLater {
            ProgressManager.getInstance().run(object : Task.Backgroundable(
                    project,
                    "Building Exposed Gradle plugin",
                    false
            ) {
                override fun run(indicator: ProgressIndicator) {
                    connection.newBuild().run()
                }
            })
        }
    }
}

@Service
class ExposedPluginConnectionService : Disposable {
    override fun dispose() {
        connection.close()
    }
}
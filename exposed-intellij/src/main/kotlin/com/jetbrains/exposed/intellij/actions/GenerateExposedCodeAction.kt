package com.jetbrains.exposed.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import java.io.File
import javax.swing.JComponent


class GenerateExposedCodeAction : AnAction() {
    object UISettings {
        var databaseDriver: String = ""
        var databaseName: String = ""
        var user: String = ""
        var password: String = ""
        var configFilename: String = ""
    }

    private class ExposedCodeGeneratorConfigDialog(project: Project) : DialogWrapper(project) {
        init {
            init()
            title = "Generate Exposed Code"
        }

        // TODO fix width
        override fun createCenterPanel(): JComponent? {
            val panel = panel(LCFlags.fill, title = "     Generate Exposed code for DB connection") {
                row("Database driver") {
                    textField(UISettings::databaseDriver)
                }
                row("Database name") {
                    textField(UISettings::databaseName)
                }
                row("Username") {
                    textField(UISettings::user)
                }
                row("Password") {
                    textField(UISettings::password)
                }
                row("Config file path") {
                    textField(UISettings::configFilename)
                }
            }

            return panel
        }
    }

    override fun actionPerformed(e: AnActionEvent) {
        val currentProject: Project = e.project ?: return

        // TODO replace
        // put the path to your actual build.gradle.kts file for this very exposed-intellij module,
        // or any other module here that has the Kotlin plugin applied.
        // this is Very Bad and will be fixed later
        val directory = File("")

        val connection: ProjectConnection = GradleConnector.newConnector()
                .forProjectDirectory(directory)
                .connect()

        if (ExposedCodeGeneratorConfigDialog(currentProject).showAndGet()) {
            connection.use {
                it.newBuild()
                        .forTasks("generateExposedCode")
                        .setEnvironmentVariables(mapOf(
                                "databaseDriver" to UISettings.databaseDriver,
                                "databaseName" to UISettings.databaseName,
                                "user" to UISettings.user,
                                "password" to UISettings.password,
                                "configFilename" to UISettings.configFilename
                        ))
                        .setStandardOutput(System.out)
                        .run()
            }
        }
    }
}
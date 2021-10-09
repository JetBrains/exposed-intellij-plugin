package com.jetbrains.exposed.gradle.plugin.shadowjar

import com.github.jengelman.gradle.plugins.shadow.relocation.RelocateClassContext
import com.github.jengelman.gradle.plugins.shadow.relocation.RelocatePathContext
import com.github.jengelman.gradle.plugins.shadow.relocation.Relocator
import com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Action
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import org.objectweb.asm.*

private class KotlinRelocator(private val task: ShadowJar, private val delegate: SimpleRelocator) : Relocator by delegate {
    override fun relocatePath(context: RelocatePathContext?): String {
        return delegate.relocatePath(context).also {
            foundRelocatedSubPaths.getOrPut(task) { hashSetOf() }.add(it.substringBeforeLast('/'))
        }
    }

    override fun relocateClass(context: RelocateClassContext?): String {
        return delegate.relocateClass(context).also {
            val packageName = it.substringBeforeLast('.')
            foundRelocatedSubPaths.getOrPut(task) { hashSetOf() }.add(packageName.replace('.', '/'))
        }
    }

    companion object {
        private val foundRelocatedSubPaths: MutableMap<ShadowJar, MutableSet<String>> = hashMapOf()
        private val relocationPaths = mutableMapOf<String, String>()

        internal fun storeRelocationPath(pattern: String, destination: String) {
            val newPattern = pattern.replace('.', '/') + "/"
            val intersections = relocationPaths.keys.filter { it.startsWith(newPattern) }
            require(intersections.isEmpty()) {
                "Can't relocate from $pattern to $destination as it clashes with another paths: ${intersections.joinToString()}"
            }
            relocationPaths[newPattern] = destination.replace('.', '/') + "/"
        }
        private fun patchFile(file: Path) {
            if(Files.isDirectory(file) || !file.toString().endsWith(".class")) return
            Files.newInputStream(file).use { ins ->
                val cr = ClassReader(ins)
                val cw = PatchedClassWriter(cr, 0, relocationPaths)
                val scanner = AnnotationScanner(cw, relocationPaths)
                cr.accept(scanner, 0)
                if (scanner.wasPatched || cw.wasPatched) {
                    ins.close()
                    Files.delete(file)
                    Files.write(file, cw.toByteArray())
                }
            }
        }

        fun patchMetadata(task: ShadowJar) {
            val zip = task.archiveFile.get().asFile.toPath()
            FileSystems.newFileSystem(zip, null).use { fs ->
                foundRelocatedSubPaths[task]?.forEach {
                    val packagePath = fs.getPath(it)
                    if (Files.exists(packagePath) && Files.isDirectory(packagePath)) {
                        Files.list(packagePath).forEach { file ->
                            patchFile(file)
                        }
                    }
                }
            }
        }
    }
}

fun ShadowJar.kotlinRelocate(pattern: String, destination: String, configure: Action<SimpleRelocator>) {
    val delegate = SimpleRelocator(pattern, destination, ArrayList(), ArrayList())
    configure.execute(delegate)
    KotlinRelocator.storeRelocationPath(pattern, destination)
    relocate(KotlinRelocator(this, delegate))
}

fun ShadowJar.kotlinRelocate(pattern: String, destination: String) {
    kotlinRelocate(pattern, destination) {}
}
package com.jetbrains.exposed.gradle.plugin.shadowjar

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

internal class PatchedClassWriter(reader: ClassReader, flags: Int, relocationPaths: MutableMap<String, String>) : ClassWriter(reader, flags) {

    private val patchedPaths = relocationPaths.map { "L${it.key}" to "L${it.value}" }.toMap()
    var wasPatched = false

    init {
        val symbolTable = symbolTableField.get(this)
        val entries = entriesField.get(symbolTable) as Array<Any?>
        entries.forEach { entryObj ->
            if (entryObj != null) {
                (symbolValueField.get(entryObj) as? String)?.let { value ->
                    val newValue = patchedPaths.entries.fold(value) { acc, entry ->
                        acc.replace(entry.key, entry.value)
                    }
                    if (value != newValue) {
                        symbolValueField.set(entryObj, newValue)
                        wasPatched = true
                    }
                }
            }
        }
    }

    companion object {
        private val classWriterClass = Class.forName("org.objectweb.asm.ClassWriter")
        private val symbolTableClass = Class.forName("org.objectweb.asm.SymbolTable")
        private val symbolTableField = classWriterClass.getDeclaredField("symbolTable").apply { isAccessible = true }
        private val entriesField = symbolTableClass.getDeclaredField("entries").apply { isAccessible = true }
        private val symbolClass = Class.forName("org.objectweb.asm.Symbol")
        private val symbolValueField = symbolClass.getDeclaredField("value").apply { isAccessible = true }
    }
}
package com.jetbrains.exposed.gradle.plugin.shadowjar
import org.objectweb.asm.*

internal class AnnotationScanner(val cw: ClassWriter, val patch: Map<String, String>) : ClassVisitor(Opcodes.ASM9, cw) {
    var wasPatched = false
    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return if (descriptor == "Lkotlin/Metadata;")
            MetadataVisitor(cw.visitAnnotation(descriptor, visible))
        else
            cw.visitAnnotation(descriptor, visible)
    }

    inner class MetadataVisitor(av: AnnotationVisitor, val thatArray: Boolean = false) : AnnotationVisitor(Opcodes.ASM9, av) {
        override fun visit(name: String?, value: Any?) {
            val newValue = when {
                thatArray && value is String && value.startsWith("(") -> {
                    patch.entries.fold(value) { n, u ->
                        n.replace(u.key, u.value)
                    }.also {
                        if (it != value) {
                            wasPatched = true
                        }
                    }
                }
                else -> value
            }
            av.visit(name, newValue)
        }

        override fun visitArray(name: String?): AnnotationVisitor? {
            return if (name == "d2") {
                MetadataVisitor(av.visitArray(name), true)
            } else {
                av.visitArray(name)
            }
        }
    }
}
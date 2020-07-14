package org.jetbrains.exposed.gradle

open class MetadataExtractionException(msg: String) : Exception(msg)

class MetadataUnsupportedTypeException(msg: String) : MetadataExtractionException(msg)

class MetadataReferencedColumnNotFoundException(msg: String) : MetadataExtractionException(msg)
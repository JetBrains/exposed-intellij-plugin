package org.jetbrains.exposed.gradle

open class MetadataExtractionException(msg: String) : Exception(msg)

class UnsupportedTypeException(msg: String) : MetadataExtractionException(msg)

class ReferencedColumnNotFoundException(msg: String) : MetadataExtractionException(msg)

class UnparseableExposedCallException(msg: String) : MetadataExtractionException(msg)
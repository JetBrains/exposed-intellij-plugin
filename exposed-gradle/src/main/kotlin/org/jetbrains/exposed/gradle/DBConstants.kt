package org.jetbrains.exposed.gradle

object MaxSize {
    const val MAX_BINARY = 2147483647
    const val MAX_VARCHAR_SIZE = 2147483647

    // the following values are max precision and scale for PostgreSQL
    // they are used as universal constants in this application for the sake of uniformity
    const val MAX_DECIMAL_PRECISION = 131072
    const val MAX_DECIMAL_SCALE = 16383
}


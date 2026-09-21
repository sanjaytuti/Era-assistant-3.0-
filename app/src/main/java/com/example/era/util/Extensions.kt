package com.example.era.util

fun String.containsAny(vararg terms: String): Boolean =
    terms.any { this.contains(it, ignoreCase = true) }

fun String.containsAny(terms: List<String>): Boolean =
    terms.any { this.contains(it, ignoreCase = true) }

package net.otuskotlin.ingredientscan.mappers.v1.internal.exceptions

class UnknownRequestClass(clazz: Class<*>) : RuntimeException("Class $clazz cannot be mapped to InternalContext")
package net.otuskotlin.ingredientscan.core.common.external.exceptions


class TimeoutException(val timeoutMs: Long) : Exception("Context processing timeout after ${timeoutMs}ms")
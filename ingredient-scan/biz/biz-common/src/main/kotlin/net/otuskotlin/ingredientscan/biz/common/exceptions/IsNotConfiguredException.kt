package net.otuskotlin.ingredientscan.biz.common.exceptions

class IsNotConfiguredException(val title: String): Exception(
    "$title is not configured"
)

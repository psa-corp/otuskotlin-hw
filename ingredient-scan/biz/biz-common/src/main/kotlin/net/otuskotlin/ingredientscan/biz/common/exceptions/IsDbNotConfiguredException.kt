package net.otuskotlin.ingredientscan.biz.common.exceptions

class IsDbNotConfiguredException(val title: String): Exception(
    "$title database is not configured"
)

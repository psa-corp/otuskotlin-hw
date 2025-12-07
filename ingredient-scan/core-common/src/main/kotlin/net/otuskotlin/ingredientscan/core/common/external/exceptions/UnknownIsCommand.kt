package net.otuskotlin.ingredientscan.core.common.external.exceptions

import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand

class UnknownIsCommand(command: IsCommand) : Throwable("Wrong command $command at mapping toTransport stage")
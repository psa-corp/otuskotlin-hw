package net.otuskotlin.ingredientscan.core.common.exceptions

import net.otuskotlin.ingredientscan.core.common.models.IsCommand

class UnknownIsCommand(command: IsCommand) : Throwable("Wrong command $command at mapping toTransport stage")
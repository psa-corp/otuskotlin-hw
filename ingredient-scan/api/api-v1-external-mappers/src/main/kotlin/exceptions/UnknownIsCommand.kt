package net.otuskotlin.ingredientscan.mappers.v1.exceptions

import net.otuskotlin.ingredientscan.core.common.models.IsCommand

class UnknownIsCommand(command: IsCommand) : Throwable("Wrong command $command at mapping toTransport stage")
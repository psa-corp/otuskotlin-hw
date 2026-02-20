package net.otuskotlin.ingredientscan.mappers.v1.internal.exceptions

import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand

class UnknownIsCommand(command: InternalCommand) : Throwable("Wrong command $command at mapping toTransport stage")
package net.otuskotlin.ingredientscan.app.common.exceptions

import net.otuskotlin.ingredientscan.app.common.models.IsCommand

class UnknownIsCommand(command: IsCommand) : Throwable("Wrong command $command at mapping toTransport stage")
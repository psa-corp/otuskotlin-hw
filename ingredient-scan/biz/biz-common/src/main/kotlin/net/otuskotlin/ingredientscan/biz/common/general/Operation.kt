package net.otuskotlin.ingredientscan.biz.common.general

import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.InternalCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsCommand
import net.otuskotlin.ingredientscan.core.common.external.models.IsState
import net.otuskotlin.ingredientscan.core.common.external.models.IsSubCommand
import net.otuskotlin.ingredientscan.core.cor.ICorChainDsl
import net.otuskotlin.ingredientscan.core.cor.chain

fun ICorChainDsl<IsContext>.operation(
    title: String,
    command: IsCommand,
    block: ICorChainDsl<IsContext>.() -> Unit
) = chain {
    block()
    this.title = title
    on { this.command == command && state == IsState.RUNNING }
}

fun ICorChainDsl<IsContext>.subOperation(
    title: String,
    command: IsSubCommand,
    block: ICorChainDsl<IsContext>.() -> Unit
) = chain {
    block()
    this.title = title
    on { this.subCommand == command && state == IsState.RUNNING }
}

fun ICorChainDsl<InternalContext>.operation(
    title: String,
    command: InternalCommand,
    block: ICorChainDsl<InternalContext>.() -> Unit
) = chain {
    block()
    this.title = title
    on { this.command == command && state == IsState.RUNNING }
}
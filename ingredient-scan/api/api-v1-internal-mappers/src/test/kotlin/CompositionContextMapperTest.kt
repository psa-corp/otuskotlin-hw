package net.otuskotlin.ingredientscan.mappers.v1.internal

import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalComposition
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalCompositionFindRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalCompositionSaveRequest
import net.otuskotlin.ingredientscan.api.v1.internal.models.InternalResponseResult
import net.otuskotlin.ingredientscan.core.common.external.InternalContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

fun IsComposition.toInternalTransportComposition(): InternalComposition? = this.toInternalTransport()

class InternalCompositionMapperTest {

    private val TEST_INTERNAL_COMPOSITION = IsComposition(
        id = IsCompositionId("comp-123"),
        text = "Water, Sugar",
        createDate = LocalDateTime.of(2025, 12, 1, 12, 0)
    )

    private val TEST_TRANSPORT_COMPOSITION = InternalComposition(
        id = "comp-123",
        text = "Water, Sugar",
        createDate = LocalDateTime.of(2025, 12, 1, 12, 0).atOffset(ZoneOffset.UTC)
    )

    @Test
    fun fromTransportInternalCompositionFind() {
        val req = InternalCompositionFindRequest(
            requestType = "internalCompositionFind",
            text = "Water, Sugar"
        )

        val context = InternalContext()
        context.fromTransportInternal(req)

        assertEquals(InternalCommand.COMPOSITION_FIND, context.command)
        assertEquals("Water, Sugar", context.compositionTextRequest)
    }

    @Test
    fun fromTransportInternalCompositionSave() {
        val req = InternalCompositionSaveRequest(
            requestType = "internalCompositionSave",
            composition = TEST_TRANSPORT_COMPOSITION
        )

        val context = InternalContext()
        context.fromTransportInternal(req)

        assertEquals(InternalCommand.COMPOSITION_SAVE, context.command)
        assertEquals(TEST_INTERNAL_COMPOSITION.id, context.compositionRequest.id)
        assertEquals(TEST_INTERNAL_COMPOSITION.text, context.compositionRequest.text)
    }

    @Test
    fun toTransportInternalCompositionFind() {
        val context = InternalContext(
            command = InternalCommand.COMPOSITION_FIND,
            state = IsState.FINISHING,
            compositionResponse = TEST_INTERNAL_COMPOSITION,
            errors = mutableListOf()
        )

        val response = context.toTransportInternalCompositionFind()

        assertEquals("internalCompositionFind", response.responseType)
        assertEquals(InternalResponseResult.SUCCESS, response.result)

        val composition = response.composition
        assertNotNull(composition)
        assertEquals(TEST_INTERNAL_COMPOSITION.id.asString(), composition.id)
        assertEquals(TEST_INTERNAL_COMPOSITION.text, composition.text)
    }

    @Test
    fun toTransportInternalCompositionSave() {
        val context = InternalContext(
            command = InternalCommand.COMPOSITION_SAVE,
            state = IsState.FAILING,
            compositionResponse = TEST_INTERNAL_COMPOSITION,
            errors = mutableListOf(
                IsError(code = "db-err", message = "Database error")
            )
        )

        val response = context.toTransportInternalCompositionSave()

        assertEquals("internalCompositionSave", response.responseType)

        assertEquals(InternalResponseResult.ERROR, response.result)
        assertEquals(1, response.errors?.size)
        assertEquals("db-err", response.errors?.first()?.code)
    }
}

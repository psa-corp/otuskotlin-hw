package net.otuskotlin.ingredientscan.scanner.services.biz

import net.otuskotlin.ingredientscan.core.common.external.IsContext
import net.otuskotlin.ingredientscan.core.common.external.models.*
import net.otuskotlin.ingredientscan.core.common.mappers.commonContextSerialize
import net.otuskotlin.ingredientscan.mappers.v1.toCompositionContext
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryCompositionRepository
import net.otuskotlin.ingredientscan.scanner.repositories.InMemoryContextRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

/**
 * Бизнес-логика для обработки команд IsContext
 *
 * Архитектура:
 * 1. Контроллер создаёт контекст и вызывает бизнес-сервис
 * 2. Бизнес-сервис определяет команду и отправляет в Kafka Streams
 * 3. Kafka Streams процессор получает контекст, обрабатывает и отправляет дальше
 * 4. Финальный процессор сохраняет в БД
 */
@Service
open class BizService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val compositionRepository: InMemoryCompositionRepository,
    private val contextRepository: InMemoryContextRepository
) {

    private val log = LoggerFactory.getLogger(BizService::class.java)

    companion object {
        const val COMPOSITION_CREATE_TOPIC = "composition-create-input"
        const val OCR_RECOGNITION_TOPIC = "ocr-recognition-input"
        const val COMPOSITION_VALIDATE_TOPIC = "composition-validate-input"
        const val COMPOSITION_SAVE_TOPIC = "composition-save-input"
    }

    fun compositionCreateByManual(context: IsContext): IsContext {
        log.info(
            "=== COMPOSITION_CREATE_MANUAL started ===\n" +
                    "requestId: {}\n" +
                    "compositionText: {}",
            context.requestId.asString(),
            context.compositionRequest.text
        )

        // Проверка: текст состава должен быть заполнен
        if (context.compositionRequest.text.isBlank()) {
            context.errors.add(
                IsError(
                    code = "COMPOSITION_TEXT_EMPTY",
                    group = "VALIDATION",
                    field = "composition.text",
                    message = "Composition text cannot be empty"
                )
            )
            context.state = IsState.FAILING
            log.warn("Composition text is empty")
            return context
        }

        // Устанавливаем команду для валидации
        context.command = IsCommand.COMPOSITION_CREATE_MANUAL
        context.state = IsState.RUNNING

        log.info("Sending context to Kafka topic: {}", COMPOSITION_CREATE_TOPIC)

        // Сериализуем контекст и отправляем в Kafka Streams
        val contextJson = commonContextSerialize(context)
        kafkaTemplate.send(COMPOSITION_CREATE_TOPIC, context.requestId.asString(), contextJson)

        log.info("=== COMPOSITION_CREATE_MANUAL sent to Kafka ===")

        // Возвращаем контекст с состоянием RUNNING
        // Финальный результат придёт через Kafka
        return context
    }

    fun compositionCreateByPhotos(context: IsContext): IsContext {
        log.info(
            "=== COMPOSITION_CREATE_PHOTOS started ===\n" +
                    "requestId: {}\n" +
                    "photoCount: {}",
            context.requestId.asString(),
            context.scanRequest.files.count()
        )

        // Проверка: должны быть фото
        if (context.scanRequest.files.isEmpty()) {
            context.errors.add(
                IsError(
                    code = "PHOTOS_EMPTY",
                    group = "VALIDATION",
                    field = "photos",
                    message = "Photos list cannot be empty"
                )
            )
            context.state = IsState.FAILING
            return context
        }

        // Устанавливаем команду для OCR распознавания
        context.command = IsCommand.COMPOSITION_CREATE_PHOTOS
        context.state = IsState.RUNNING

        log.info("Sending context to Kafka topic for OCR: {}", OCR_RECOGNITION_TOPIC)

        // Сериализуем контекст и отправляем в Kafka Streams для OCR
        val contextJson = commonContextSerialize(context)
        kafkaTemplate.send(OCR_RECOGNITION_TOPIC, context.requestId.asString(), contextJson)

        log.info("=== COMPOSITION_CREATE_PHOTOS sent to Kafka for OCR ===")

        // Возвращаем контекст с состоянием RUNNING
        // Финальный результат придёт после OCR обработки
        return context
    }

    fun compositionGet(context: IsContext): IsContext {
        log.info(
            "=== COMPOSITION_GET started ===\n" +
                    "requestId: {}\n" +
                    "compositionIdRequest: {}",
            context.requestId.asString(),
            context.compositionIdRequest.asString()
        )
        // Поиск состава по ID
        context.compositionResponse = findComposition(context.compositionIdRequest)

        if (context.compositionResponse.isEmpty()) {
            context.state = IsState.FAILING
            context.errors.add(
                IsError(
                    code = "COMPOSITION_NOT_FOUND",
                    group = "REPOSITORY",
                    field = "database",
                    message = "Composition not found: ${context.compositionIdRequest.asString()}"
                )
            )
            return context
        }
        context.state = IsState.FINISHING
        return context
    }

    fun compositionContextGet(context: IsContext): IsContext {
        log.info(
            "=== COMPOSITION_CONTEXT_GET started ===\n" +
                    "requestId: {}\n" +
                    "contextIdRequest: {}",
            context.requestId.asString(),
            context.contextIdRequest.asString()
        )
        // Поиск состава по ID
        context.compositionContextResponse = findCompositionContext(context.contextIdRequest)

        if (context.compositionContextResponse.isEmpty()) {
            context.state = IsState.FAILING
            context.errors.add(
                IsError(
                    code = "CONTEXT_NOT_FOUND",
                    group = "REPOSITORY",
                    field = "database",
                    message = "Context not found: ${context.contextIdRequest.asString()}"
                )
            )
            return context
        }
        context.state = IsState.FINISHING
        return context
    }

    private fun findComposition(id: IsCompositionId): IsComposition {
        var composition = compositionRepository.findById(id.asString())
        log.info("Find Composition: id:{}, composition:{}",id, composition)
        return composition?: IsComposition.NONE
    }

    private fun findCompositionContext(id: IsContextId): IsCompositionContext {
        var context = contextRepository.findById(id.asString())
        log.info("Find Composition: id:{}, composition:{}",id, context)

        if (context == null) {
            return IsCompositionContext.NONE
        }

        return context.toCompositionContext()
    }
}
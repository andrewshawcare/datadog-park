package com.andrewshawcare.datadog_park.producer

import net.logstash.logback.argument.StructuredArguments.entries
import org.apache.kafka.clients.admin.NewTopic
import org.milyn.edi.unedifact.d08b.D08BInterchangeFactory
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchangeFactory
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactInterchange41
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.kafka.core.KafkaTemplate
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.random.Random.Default.nextInt

enum class Topic(val value: String) {
    EVENT("Event")
}

class StreamClient(private val kafkaTemplate: KafkaTemplate<String, String>) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val teamIds = listOf(
        "com.andrewshawcare.team.batch",
        "com.andrewshawcare.team.api",
        "com.andrewshawcare.team.portal"
    )
    private val systemIds = listOf(
        "com.andrewshawcare.system.batch",
        "com.andrewshawcare.system.api",
        "com.andrewshawcare.system.portal"
    )
    private val classificationNames = listOf(
        "RESTRICTED",
        "HIGHLY SENSITIVE",
        "MODERATELY SENSITIVE",
        "MINIMALLY SENSITIVE",
        "NON-SENSITIVE"
    )
    private val documentFormats = listOf(
        "JSON",
        "EDI",
        "CSV"
    )
    private val documentStandards = listOf(
        "EDIFACT",
        "X12"
    )
    private val documentReleases = listOf(
        "008020"
    )
    private val documentTypes = listOf(
        "ORDERS",
        "INVRPT",
        "ORDRSP",
        "ORDCHG",
        "INVOIC",
        "DESADV",
        "INSDES",
        "RECADV",
        "SLSRPT"
    )

    private fun <E> pickRandomListItem(list: List<E>): E = list[nextInt(list.size)]

    fun sendDataToTopic(topic: Topic, data: String) {
        kafkaTemplate.send(topic.value, data)
        logger.info("{}", entries(
            mapOf("event" to
                mapOf(
                "id" to UUID.randomUUID(),
                "type" to "DocumentReceived",
                "meta" to mapOf(
                    "team" to mapOf(
                        "id" to pickRandomListItem(teamIds)
                    ),
                    "system" to mapOf(
                        "id" to pickRandomListItem(systemIds)
                    ),
                    "classification" to mapOf(
                        "name" to pickRandomListItem(classificationNames)
                    ),
                    "document" to mapOf(
                        "format" to pickRandomListItem(documentFormats),
                        "standard" to pickRandomListItem(documentStandards),
                        "release" to pickRandomListItem(documentReleases),
                        "type" to pickRandomListItem(documentTypes)
                    )
                )
            ))))
    }
}

@SpringBootApplication
class Application {
    @Bean
    fun unEdifactInterchangeFactory(): UNEdifactInterchangeFactory = D08BInterchangeFactory.getInstance()

    @Bean
    fun eventTopic() = NewTopic(Topic.EVENT.value, 10, 1)

    @Bean
    fun streamClient(kafkaTemplate: KafkaTemplate<String, String>) = StreamClient(kafkaTemplate = kafkaTemplate)

    @Bean
    fun runner(
        unEdifactInterchangeFactory: UNEdifactInterchangeFactory,
        streamClient: StreamClient
    ) = ApplicationRunner {
        val unEdifactInterchange41 = UNEdifactInterchange41()

        val byteArrayOutputStream = ByteArrayOutputStream()
        unEdifactInterchangeFactory.toUNEdifact(unEdifactInterchange41, byteArrayOutputStream.writer())

        while (true) {
            streamClient.sendDataToTopic(topic=Topic.EVENT, data=byteArrayOutputStream.toString())
            Thread.sleep(1000)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args)
        }
    }
}
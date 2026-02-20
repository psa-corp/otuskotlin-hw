package net.otuskotlin.ingredientscan.analyzer.services.kafka.streams.config

import org.apache.kafka.streams.StreamsBuilder

interface TopologyConfigurer {
    fun configure(streamsBuilder: StreamsBuilder)
}
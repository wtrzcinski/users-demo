package com.codete.user.periphery.kafka

data class KafkaTopicConfig(
        var topic: String? = null,
        var numPartitions: Int? = null,
        var replicationFactor: Short? = null
)
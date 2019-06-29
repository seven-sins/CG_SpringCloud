package com.cg.flink.kafka.listener;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;

public class KafkaCustomListener {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaCustomListener.class);

	@KafkaListener(topics = {"testTopic"})
	public void listen(ConsumerRecord<?, ?> record) {
		LOG.info("=============key: " + record.key());
		LOG.info("=============value: " + record.value());
	}
}

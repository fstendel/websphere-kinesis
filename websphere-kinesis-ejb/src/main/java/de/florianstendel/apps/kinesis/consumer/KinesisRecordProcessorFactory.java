package de.florianstendel.apps.kinesis.consumer;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

public class KinesisRecordProcessorFactory implements ShardRecordProcessorFactory {

    public ShardRecordProcessor shardRecordProcessor() {
        return new KinesisRecordProcessor();
    }
}

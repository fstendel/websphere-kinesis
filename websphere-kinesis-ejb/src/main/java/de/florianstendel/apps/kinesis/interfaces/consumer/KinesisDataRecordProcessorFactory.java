package de.florianstendel.apps.kinesis.interfaces.consumer;

import software.amazon.kinesis.processor.ShardRecordProcessor;
import software.amazon.kinesis.processor.ShardRecordProcessorFactory;

public class KinesisDataRecordProcessorFactory implements ShardRecordProcessorFactory {

    public ShardRecordProcessor shardRecordProcessor() {
        return new KinesisDataRecordProcessor();
    }
}

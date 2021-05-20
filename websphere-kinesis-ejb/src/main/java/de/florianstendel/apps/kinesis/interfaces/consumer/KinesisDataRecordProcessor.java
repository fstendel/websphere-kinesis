package de.florianstendel.apps.kinesis.interfaces.consumer;

import de.florianstendel.apps.kinesis.business.BusinessLogicProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.kinesis.exceptions.InvalidStateException;
import software.amazon.kinesis.exceptions.ShutdownException;
import software.amazon.kinesis.lifecycle.events.*;
import software.amazon.kinesis.processor.ShardRecordProcessor;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class KinesisDataRecordProcessor implements ShardRecordProcessor {


    private static final String SHARD_ID_MDC_KEY = "ShardId";

    private static final Logger log = LoggerFactory.getLogger(KinesisDataRecordProcessor.class);

    private BusinessLogicProcessor businessLogicProcessor;

    private String shardId;


    /**
     * Create an instance of @{@link KinesisDataRecordProcessor} and injects/lookups
     * and instance of @{@link BusinessLogicProcessor}
     * as this class is initiated/used by a non EJB-container-managed framework (AWS KCL/KPL)
     *
     */
    public KinesisDataRecordProcessor() {
        try {
            businessLogicProcessor = (BusinessLogicProcessor)
                        new InitialContext().lookup("java:module/BusinessLogicProcessor");
        } catch (NamingException e) {
            throw new IllegalStateException("BusinessLogicProcess could not be retrieved from EjbContainer",e);
        }
    }


    @Override
    public void initialize(InitializationInput initializationInput) {
        shardId = initializationInput.shardId();
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Initializing @ Sequence: {}", initializationInput.extendedSequenceNumber());
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Processing {} record(s)", processRecordsInput.records().size());
            processRecordsInput.records()
                    .forEach(r -> {
                        log.info("Processing record pk: {} -- Seq: {}", r.partitionKey(), r.sequenceNumber());
                        SdkBytes data = SdkBytes.fromByteBuffer(r.data());
                        log.info("Record content is: " + data.asUtf8String());
                        businessLogicProcessor.processData(data.asUtf8String());

                    });
        } catch (Throwable t) {
            log.error("Caught throwable while processing records. Aborting.");
            Runtime.getRuntime().halt(1);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    @Override
    public void leaseLost(LeaseLostInput leaseLostInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Lost lease, so terminating.");
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    @Override
    public void shardEnded(ShardEndedInput shardEndedInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Reached shard end checkpointing.");
            shardEndedInput.checkpointer().checkpoint();
        } catch (ShutdownException | InvalidStateException e) {
            log.error("Exception while checkpointing at shard end. Giving up.", e);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }

    @Override
    public void shutdownRequested(ShutdownRequestedInput shutdownRequestedInput) {
        MDC.put(SHARD_ID_MDC_KEY, shardId);
        try {
            log.info("Scheduler is shutting down, checkpointing.");
            shutdownRequestedInput.checkpointer().checkpoint();
        } catch (ShutdownException | InvalidStateException e) {
            log.error("Exception while checkpointing at requested shutdown. Giving up.", e);
        } finally {
            MDC.remove(SHARD_ID_MDC_KEY);
        }
    }
}

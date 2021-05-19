package de.florianstendel.apps.kinesis.consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.kinesis.common.ConfigsBuilder;
import software.amazon.kinesis.common.InitialPositionInStream;
import software.amazon.kinesis.common.InitialPositionInStreamExtended;
import software.amazon.kinesis.common.KinesisClientUtil;
import software.amazon.kinesis.coordinator.Scheduler;
import software.amazon.kinesis.retrieval.polling.PollingConfig;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

public class KinesisConsumer {

    private static final String APPLICATION_NAME = "CoolKinesisConsumer";
    private static final String STREAM_NAME = "Cool-Stream";
    private static final Region region = Region.EU_CENTRAL_1;
    private static final String KINESIS_URL = "http://localhost:4566";

    private Thread schedulerThread = null;
    private Scheduler scheduler = null;


    private static final Logger log = LoggerFactory.getLogger(KinesisConsumer.class);

    @PostConstruct
    public void startup() {

        try {
            KinesisAsyncClient kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(
                    KinesisAsyncClient.builder()
                            .region(region)
                            .endpointOverride(new URI(KINESIS_URL))
                            .credentialsProvider(
                                    StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
            );

            DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder()
                    .region(region)
                    .endpointOverride(new URI(KINESIS_URL))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
                    .build();
            CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().
                    region(region)
                    .endpointOverride(new URI(KINESIS_URL))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
                    .build();
            ConfigsBuilder configsBuilder = new ConfigsBuilder(STREAM_NAME, APPLICATION_NAME, kinesisAsyncClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new KinesisRecordProcessorFactory());


            scheduler = new Scheduler(
                    configsBuilder.checkpointConfig(),
                    configsBuilder.coordinatorConfig(),
                    configsBuilder.leaseManagementConfig(),
                    configsBuilder.lifecycleConfig(),
                    configsBuilder.metricsConfig(),
                    configsBuilder.processorConfig(),
                    configsBuilder.retrievalConfig().initialPositionInStreamExtended(InitialPositionInStreamExtended.newInitialPosition(InitialPositionInStream.TRIM_HORIZON))
                            .retrievalSpecificConfig(new PollingConfig(STREAM_NAME, kinesisAsyncClient)
                                    .kinesisRequestTimeout(Duration.ofSeconds(60))));

            log.info("Start consuming from stream: " + STREAM_NAME);

            start();


        } catch (Throwable t) {
            log.error("Exited kinesis consumer");
        }
    }

    private void start() throws NamingException {

        ManagedThreadFactory managedThreadFactory =
                (ManagedThreadFactory) new InitialContext().lookup(
                        "java:comp/DefaultManagedThreadFactory");

        Thread schedulerThread = managedThreadFactory.newThread(scheduler);

        schedulerThread.setDaemon(true);
        schedulerThread.start();

        log.info("Kinesis Scheduler Thread started:" + schedulerThread.getName());
    }


    @PreDestroy
    public void stop() {
       log.info("Stopping kinesis consumer");

        scheduler.shutdown();

    }
}

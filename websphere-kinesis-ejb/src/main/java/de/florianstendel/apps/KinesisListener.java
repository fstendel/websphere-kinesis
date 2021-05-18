package de.florianstendel.apps;


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
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

public class KinesisListener {

    private static final String STREAM_NAME = "Cool-Stream";
    private static final String KINESIS_URL = "http://localhost:4566";

    @PostConstruct
    public void startup() {

        try {
            String streamName = STREAM_NAME;
            Region region = Region.EU_CENTRAL_1;
            KinesisAsyncClient kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(
                    KinesisAsyncClient.builder()
                            .region(region)
                            .endpointOverride(new URI(KINESIS_URL))
                            .credentialsProvider(
                                    StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack","localstack")))
            );

            DynamoDbAsyncClient dynamoClient = DynamoDbAsyncClient.builder()
                    .region(region)
                    .endpointOverride(new URI(KINESIS_URL))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack","localstack")))
                    .build();
            CloudWatchAsyncClient cloudWatchClient = CloudWatchAsyncClient.builder().
                    region(region)
                    .endpointOverride(new URI(KINESIS_URL))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack","localstack")))
                    .build();
            ConfigsBuilder configsBuilder = new ConfigsBuilder(streamName, streamName, kinesisAsyncClient, dynamoClient, cloudWatchClient, UUID.randomUUID().toString(), new KinesisRecordProcessorFactory());


            Scheduler scheduler = new Scheduler(
                    configsBuilder.checkpointConfig(),
                    configsBuilder.coordinatorConfig(),
                    configsBuilder.leaseManagementConfig(),
                    configsBuilder.lifecycleConfig(),
                    configsBuilder.metricsConfig(),
                    configsBuilder.processorConfig(),
                    configsBuilder.retrievalConfig().initialPositionInStreamExtended(InitialPositionInStreamExtended.newInitialPosition(InitialPositionInStream.TRIM_HORIZON))
                            .retrievalSpecificConfig(new PollingConfig(streamName, kinesisAsyncClient)
                                    .kinesisRequestTimeout(Duration.ofSeconds(60))));

            System.out.println("Start consuming");

            ManagedThreadFactory managedThreadFactory =
                    (ManagedThreadFactory) new InitialContext().lookup(
                            "java:comp/DefaultManagedThreadFactory");

            Thread schedulerThread = managedThreadFactory.newThread(scheduler);

            schedulerThread.setDaemon(true);
            schedulerThread.start();

            System.out.println("Kinesis Scheduler Thread started:" + schedulerThread.getName());


        } catch (Throwable t) {
            System.out.println("Exited");
        }
    }
}

package de.florianstendel.apps.kinesis.interfaces.producer;

import de.florianstendel.apps.kinesis.infrastructure.StreamsConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

import java.net.URI;
import java.net.URISyntaxException;

public class KinesisDataProducer {

    private static final String STREAM_NAME = StreamsConfiguration.getOutboundStreamName();
    private static final Region region = StreamsConfiguration.getRegion();
    private static final String KINESIS_URL = StreamsConfiguration.getKinesisUrl();

    public void startup(final String data) throws URISyntaxException {

        KinesisAsyncClient kinesisAsyncClient = KinesisClientUtil.createKinesisAsyncClient(
                KinesisAsyncClient.builder()
                        .region(region)
                        .endpointOverride(new URI(KINESIS_URL))
                        .credentialsProvider(
                                StaticCredentialsProvider.create(AwsBasicCredentials.create("localstack", "localstack")))
        );


        PutRecordRequest putRecordRequest = PutRecordRequest.builder()
                .streamName(STREAM_NAME)
                .partitionKey("1")
                .data(SdkBytes.fromUtf8String(data)).build();


        kinesisAsyncClient.putRecord(putRecordRequest);
    }
}

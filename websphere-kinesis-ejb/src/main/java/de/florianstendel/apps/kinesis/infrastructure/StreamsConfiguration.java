package de.florianstendel.apps.kinesis.infrastructure;

import software.amazon.awssdk.regions.Region;

public class StreamsConfiguration {


    private static final String APPLICATION_NAME = "CoolKinesisConsumer";
    private static final String INBOUND_STREAM_NAME = "Cool-Stream";
    private static final String OUTBOUND_STREAM_NAME = "Cool-Response-Stream";
    private static final Region REGION = Region.EU_CENTRAL_1;
    private static final String KINESIS_URL = "http://localhost:4566";


    public static String getApplicationName() {
        return APPLICATION_NAME;
    }

    public static String getInboundStreamName() {
        return INBOUND_STREAM_NAME;
    }

    public static String getOutboundStreamName() {
        return OUTBOUND_STREAM_NAME;
    }

    public static Region getRegion() {
        return REGION;
    }

    public static String getKinesisUrl() {
        return KINESIS_URL;
    }
}

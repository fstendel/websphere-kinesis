package de.florianstendel.apps.kinesis.business;

import de.florianstendel.apps.kinesis.interfaces.producer.KinesisDataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;

public class BusinessLogicProcessor {


    private static final Logger log = LoggerFactory.getLogger(BusinessLogicProcessor.class);
    private KinesisDataProducer kinesisDataProducer;

    @EJB
    public void setKinesisDataProducer(final KinesisDataProducer kinesisDataProducer) {
        this.kinesisDataProducer = kinesisDataProducer;
    }

    public void processData(final String data) {
        log.info("Doing highly sophisticated processing here...");
        try {
            String processedData = data + " extended with additional processing information.";
            kinesisDataProducer.startup(processedData);
        } catch (Throwable e) {
            log.error("Error pushing data back to kinesis",e);
        }
        log.info("Finished highly sophisticated processing!");
    }
}

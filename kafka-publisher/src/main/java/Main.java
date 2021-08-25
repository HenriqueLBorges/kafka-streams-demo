import DataModels.Boleto;
import DataModels.BoletoValidation;
import Factories.BoletoFactory;
import Factories.BoletoValidationFactory;
import Serdes.BoletoSerializer;
import Serdes.BoletoValidationSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;

import java.time.Duration;
import java.util.Properties;

public class Main {
    private final static String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS") != null ? System.getenv("BOOTSTRAP_SERVERS") : "localhost:9092";
    private final static String LEFT_TOPIC = System.getenv("LEFT_TOPIC") != null ? System.getenv("LEFT_TOPIC") : "left-topic";
    private final static String RIGHT_TOPIC = System.getenv("RIGHT_TOPIC") != null ? System.getenv("RIGHT_TOPIC") : "right-topic";
    private final static String CLIENT_ID_CONFIG_BOLETO = System.getenv("BOLETO_PRODUCER_CLIENT_ID_CONFIG") != null ? System.getenv("BOLETO_PRODUCER_CLIENT_ID_CONFIG") : "kafka-streams-boleto-producer";
    private final static String CLIENT_ID_CONFIG_BOLETO_VALIDATION = System.getenv("BOLETO_VALIDATION_PRODUCER_CLIENT_ID_CONFIG") != null ? System.getenv("BOLETO_VALIDATION_PRODUCER_CLIENT_ID_CONFIG") : "kafka-streams-boleto-validation-producer";

    /**
     * Kafka Publisher Application Main method.
     *
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting Kafka publisher...");

            BoletoFactory boletoFactory = new BoletoFactory();
            BoletoValidationFactory boletoValidationFactory = new BoletoValidationFactory();

            System.out.println("Publishing 3 messages on each topic with 10ms difference");
            Boleto[] boletos = boletoFactory.createBoletos(3);
            Main.createBoletos(0, 3, LEFT_TOPIC, boletos);
            Thread.sleep(Duration.ofMillis(10).toMillis());
            Main.createValidations(0, 3, RIGHT_TOPIC, boletoValidationFactory.validate(boletos));
            System.out.println("----------------------------------------------------------");
            System.out.println("Publishing 1 message on each topic with 10s difference");
            boletos = boletoFactory.createBoletos(1);
            Main.createBoletos(3, 4, LEFT_TOPIC, boletos);
            Thread.sleep(Duration.ofMillis(Duration.ofSeconds(10).toMillis()).toMillis());
            Main.createValidations(3, 4, RIGHT_TOPIC, boletoValidationFactory.validate(boletos));
        } catch (Exception e){
            System.out.println(e);
        }

    }

    /**
     * Create fake boletos and insert them on topic
     *
     * @param start - Start key
     * @param end - End key
     * @param topic - Destination kafka topic
     * @param boletos -  Boletos that will be inserted
     */
    private static void createBoletos(final int start, final int end, String topic, Boleto [] boletos) throws Exception {
        final Producer<Long, Boleto> producer = new KafkaProducer<>(generateBoletoProps());
        System.out.println("----------------------------------------------------------");
        System.out.println("Producer from boletos created");
        long time = System.currentTimeMillis();

        try {
            int index = 0;
            for (long i = start; i < end; i++) {
                System.out.println("----------------------------------------------------------");
                final ProducerRecord<Long, Boleto> record =
                        new ProducerRecord<>(topic, i, boletos[index]);

                RecordMetadata metadata = producer.send(record).get();

                long elapsedTime = System.currentTimeMillis() - time;
                System.out.println("Sent record(key=" + record.key() + " value=" + record.value().toString() + ") " +
                        "meta(partition= " + metadata.partition() + ", offset=" + metadata.offset() + ") time=" + elapsedTime);
                Thread.sleep(Duration.ofSeconds(1).toMillis());
                index += 1;
            }
        } catch (Exception e) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Exception occurred in Producer from boletos:" + e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    /**
     * Create fake validations and insert them on topic
     *
     * @param start - Start key
     * @param end - End key
     * @param topic - Destination kafka topic
     * @param boletoValidations -  Validations that will be inserted
     */
    private static void createValidations(final int start, final int end, String topic, BoletoValidation [] boletoValidations) throws Exception {
        final Producer<Long, BoletoValidation> producer = new KafkaProducer<>(generateBoletoValidationProps());
        System.out.println("----------------------------------------------------------");
        System.out.println("Producer from validation validations created");
        long time = System.currentTimeMillis();

        try {
            int index = 0;
            for (long i = start; i < end; i++) {
                System.out.println("----------------------------------------------------------");
                final ProducerRecord<Long, BoletoValidation> record =
                        new ProducerRecord<>(topic, i, boletoValidations [index]);

                RecordMetadata metadata = producer.send(record).get();

                long elapsedTime = System.currentTimeMillis() - time;
                System.out.println("Sent record(key=" + record.key() + " value=" + record.value().toString() + ") " +
                        "meta(partition= " + metadata.partition() + ", offset=" + metadata.offset() + ") time=" + elapsedTime);
                Thread.sleep(Duration.ofSeconds(1).toMillis());
                index += 1;
            }
        } catch (Exception e) {
            System.out.println("----------------------------------------------------------");
            System.out.println("Exception occurred in Producer from validations:" + e);
        } finally {
            producer.flush();
            producer.close();
        }
    }

    /**
     * A Kafka Publisher Application needs a set of properties in order to work. This method returns the properties in order to our Kafka publisher to work publishing boletos.
     *
     * @return Properties - Kafka Application Properties
     */
    private static Properties generateBoletoProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG_BOLETO);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                BoletoSerializer.class.getName());

        return props;
    }

    /**
     * A Kafka Publisher Application needs a set of properties in order to work. This method returns the properties in order to our Kafka publisher to work publishing validations.
     *
     * @return Properties - Kafka Application Properties
     */
    private static Properties generateBoletoValidationProps() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG_BOLETO_VALIDATION);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                BoletoValidationSerializer.class.getName());

        return props;
    }
}

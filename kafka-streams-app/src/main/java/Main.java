import java.util.Properties;
import java.util.concurrent.TimeUnit;
import DataModels.Boleto;
import DataModels.BoletoValidation;
import Serdes.BoletoSerde;
import Serdes.BoletoValidationSerde;
import Transformers.BoletoReceivedTransformer;
import Transformers.ValidationBoletoTransformer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class Main {
    private final static String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS") != null ? System.getenv("BOOTSTRAP_SERVERS") : "localhost:9092";
    private final static String LEFT_TOPIC = System.getenv("LEFT_TOPIC") != null ? System.getenv("LEFT_TOPIC") : "left-topic";
    private final static String RIGHT_TOPIC = System.getenv("RIGHT_TOPIC") != null ? System.getenv("RIGHT_TOPIC") : "right-topic";
    private final static String OUTPUT_TOPIC = System.getenv("OUTPUT_TOPIC") != null ? System.getenv("OUTPUT_TOPIC") : "output-topic";
    private final static String STORE_NAME = System.getenv("STORE_NAME") != null ? System.getenv("STORE_NAME") : "boletos-to-validade";
    private final static String CLIENT_ID_CONFIG = System.getenv("CLIENT_ID_CONFIG") != null ? System.getenv("CLIENT_ID_CONFIG") : "kafka-streams-app";
    private final static String APPLICATION_ID_CONFIG = System.getenv("APPLICATION_ID_CONFIG") != null ? System.getenv("APPLICATION_ID_CONFIG") : "kafka-streams-app-client";
    private final static int COMMIT_INTERVAL_MS_CONFIG = System.getenv("COMMIT_INTERVAL_MS_CONFIG") != null ? Integer.parseInt(System.getenv("COMMIT_INTERVAL_MS_CONFIG")) : 10;

    private static final StoreBuilder<KeyValueStore<Long, Boleto>> storeBuilder =
            Stores.keyValueStoreBuilder(
                    Stores.persistentKeyValueStore(STORE_NAME),
                    Serdes.Long(),
                    new BoletoSerde());

    /**
     * Kafka Streams Application Main method.
     *
     */
    public static void main(final String[] args) {
        try {
            System.out.println("Kafka Streams Application");
            KafkaStreams streams = new KafkaStreams(getTopology(), getStreamsConfiguration());

            streams.setUncaughtExceptionHandler((Thread thread, Throwable throwable) -> {
                System.out.println("EXITTING");
                System.out.println(throwable.getMessage());
                System.out.println(throwable.getStackTrace().toString());
                streams.close(5 , TimeUnit.SECONDS);
                System.exit(-1);
            });

            streams.cleanUp();
            streams.start();

            // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * A Kafka Streams Application needs a set of properties in order to work. This method returns the properties in order to our Kafka Streams to work.
     *
     * @return Properties - Kafka Streams Properties
     */
    public static Properties getStreamsConfiguration() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.Long().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                BoletoSerde.class.getName());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID_CONFIG);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, COMMIT_INTERVAL_MS_CONFIG);

        return props;
    }

    /**
     * A collection of processors forms a Processor Topology, which is often referred to as simply the topology. This method returns the topology for the stream application.
     *
     * @return      Kafka Streams Application Topology
     */
    public static Topology getTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // To support stateful operations, we need a way of storing and retrieving the remembered data, or state, required by each stateful operator in our application
        builder.addStateStore(storeBuilder);

        // Declaring our Kafka streams- An Abstraction for modeling data

        // The left stream receives boletos for validation
        final KStream<Long, Boleto> streams1 =
                builder.stream(LEFT_TOPIC, Consumed.with(Serdes.Long(), new BoletoSerde()));

        // The right stream receives validations for boletos
        final KStream<Long, BoletoValidation> streams2 =
                builder.stream(RIGHT_TOPIC, Consumed.with(Serdes.Long(), new BoletoValidationSerde()));

        // We use the transform operator here to combinate the Kafka streams DLS (Domain Specific Language) methods with processor API (low level control of the streams)

        // Send every boleto to the first transformer where it will be saved and processed
        streams1.transform(BoletoReceivedTransformer::new, STORE_NAME);

        // Send every validation to the second transform where it will be processed and copy the result
        final KStream<Long, BoletoValidation> streams3 = streams2.transform(ValidationBoletoTransformer::new, STORE_NAME);

        // The original stream will be sent to the output topic where the results can be consumed by other applications
        streams2.to(OUTPUT_TOPIC, Produced.with(Serdes.Long(), new BoletoValidationSerde()));

        // The copy stream will be used to demonstrate what other applications consuming the output topic will receive
        // The for each operator here is only used to demonstrate what events we are receiving in our output topic
        streams3.foreach((key, value) -> {
            System.out.println("----------------------------------------------------------");

            if (value != null) {
                // Verifies which type of result we received on the output topic
                if (value.getExpired() && value.getValidationDate() == null) { // Expired boletos without validations
                    System.out.println("OUTPUT TOPIC - Received a new event - Boleto: " + key + " - expired - " + value);
                } else if (value.getExpired()) { // Late validations for boletos that were already expired
                    System.out.println("OUTPUT TOPIC - Received a new event - Boleto: " + key + " - late validation arrived - " + value);
                } else { // Boletos that were not expired and their corresponding validations
                    System.out.println("OUTPUT TOPIC - Received a new event - Boleto: " + key + " - " + value);
                }
            } else {
                System.out.println("OUTPUT TOPIC - Received a null value for key: " + key);
            }

        });

        return builder.build();
    }
}

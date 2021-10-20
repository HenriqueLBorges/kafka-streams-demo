import java.time.Duration;
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
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.kstream.internals.SessionWindow;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

public class Main {
    private final static String BOOTSTRAP_SERVERS = System.getenv("BOOTSTRAP_SERVERS") != null ? System.getenv("BOOTSTRAP_SERVERS") : "localhost:9093";
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
     * Método principal da aplicação de Kafka Streams.
     *
     */
    public static void main(final String[] args) {
        try {

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

            // Encerra a aplicação
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * Um aplicação de Kafka Streams precisa de um conjunto de propriedades para funcionar. Esse método retorna essas propriedades.
     *
     * @return Properties - Propriedades Kafka Streams
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
     * Uma coleção de Processor ligados entre si forma uma topologia de processamento (ProcessorTopology). Esse método retorna a topologia para a aplicação de Streams.
     *
     * @return      Kafka Streams Application Topology
     */
    public static Topology getTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        // Operacões que guardam estado (Stateful) necessitam de uma forma para guardar e recuperar dados, ou estado. Para isso utilizaremos uma state store na memória.
        builder.addStateStore(storeBuilder);

        // Declarando nossos objetos de KStream (uma abstração de stream para os dados provenientes dos tópicos Kafka).

        // A stream da esquerda recebe os boletos para validação
        final KStream<Long, Boleto> streams1 =
                builder.stream(LEFT_TOPIC, Consumed.with(Serdes.Long(), new BoletoSerde()));

        // A stream da direita rebe as validações de boletos
        final KStream<Long, BoletoValidation> streams2 =
                builder.stream(RIGHT_TOPIC, Consumed.with(Serdes.Long(), new BoletoValidationSerde()));


        /*streams1.leftJoin(streams2,
                (leftValue, rightValue) -> "left =" + leftValue + ", right =" + rightValue,
                JoinWindows.of(Duration.ofSeconds(5)),
                Joined.with(
                    Serdes.Long(),
                    new BoletoSerde(),
                    new BoletoValidationSerde()
                ),
                streams3
        );

        // Java 8+ example, using lambda expressions
        KStream<Long, String> joined = streams1.join(streams2,
                (leftValue, rightValue) -> "left=" + leftValue + ", right=" + rightValue, // ValueJoiner
                JoinWindows.of(Duration.ofMinutes(2)),
                Joined.with(
                        Serdes.Long(), // key
                        Serdes.String(), // left value
                        Serdes.String()) // right value
        );*/



        // We use the transform operator here to combinate the Kafka streams DLS (Domain Specific Language) methods with processor API (low level control of the streams)

        // Envia cada boleto recebido na stream para o primeiro Transformer, onde o boleto será armazenado e processado.
        streams1.transform(BoletoReceivedTransformer::new, STORE_NAME);

        // Envia cada validação para o segundo Transformer, onde a validação será processada.
        // Copia a stream
        final KStream<Long, BoletoValidation> streams3 = streams2.transform(ValidationBoletoTransformer::new, STORE_NAME);

        // A stream original será enviada para o tópico de saída onde os resultados serão consumidos por outras aplicações
        streams2.to(OUTPUT_TOPIC, Produced.with(Serdes.Long(), new BoletoValidationSerde()));

        // A cópia da stream será usada para demonstrar o que outras aplicações consumiriam do tópico de saída.
        streams3.foreach((key, value) -> {
            System.out.println("----------------------------------------------------------");

            if (value != null) {
                // Verifica o tipo de resultado recebido no tópico de saída
                if (value.getExpired() && value.getValidationDate() == null) { // Boletos expirados
                    System.out.println("OUTPUT TOPIC - Received a new event - Boleto: " + key + " - expired - " + value);
                } else if (value.getExpired()) { // Validações atrasadas para boletos já expirados
                    System.out.println("OUTPUT TOPIC - Received a new event - Boleto: " + key + " - late validation arrived - " + value);
                } else { // Boletos que receberam a valdiação a tempo
                    System.out.println("OUTPUT TOPIC - Received a new event - Boleto: " + key + " - " + value);
                }
            } else {
                System.out.println("OUTPUT TOPIC - Received a null value for key: " + key);
            }

        });

        return builder.build();
    }
}

package Transformers;

import DataModels.Boleto;
import DataModels.BoletoValidation;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.Cancellable;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import java.time.Duration;
import java.time.Instant;

public class ValidationBoletoTransformer implements Transformer<Long, BoletoValidation, KeyValue<Long, BoletoValidation>> {
    private ProcessorContext context;
    private KeyValueStore<Long, Boleto> kvStore;
    private Cancellable punctuator;
    private static String STORE_NAME = System.getenv("STORE_NAME") != null ? System.getenv("STORE_NAME") : "boletos-to-validade";

    /**
     * Inicia o Transformer
     *
     * @param context - Contexto do processor
     */
    @Override
    public void init(ProcessorContext context) {

        this.context = context;

        // retrieve the key-value store
        this.kvStore = context.getStateStore(STORE_NAME);

        // schedule a punctuate() method every 5 seconds based on wall_clock_time
        this.punctuator = this.context.schedule(Duration.ofSeconds(5), PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            System.out.println("----------------------------------------------------------");
            System.out.println("Ponctuation time!");
            this.enforceTtl(timestamp);
        });

        this.context.schedule(
                Duration.ofSeconds(1), PunctuationType.STREAM_TIME, (ts) -> context.commit());
    }

    /**
     * Nosso Transformer é encadeado no fluxo de processamento de validações, cada validacão recebida no processor invoca esse método. Esse método processa cada validação recebida para um boleto.
     *
     * @param key - Chave do evento
     * @param value - Validação de um determinado boleto
     */
    @Override
    public KeyValue<Long, BoletoValidation> transform(Long key, BoletoValidation value) {
        System.out.println("----------------------------------------------------------");
        System.out.println("Processing validation: " + key);

        // Search the corresponding boleto on the local store
        Boleto boleto = kvStore.get(key);


        if (boleto == null) { // The boleto is not on the local store anymore, so it was already processed by the TTL
            System.out.println("----------------------------------------------------------");
            System.out.println("Boleto " + key + " was already processed, setting it as expired");

            value.setExpired(true); // Marking this validation as expired
        } else { // The boleto is on the local store, so we can still process it
            System.out.println("----------------------------------------------------------");
            System.out.println("Boleto " + key + " received it's validation: " + value.toString());
            kvStore.delete(key); // Removing the boleto from the local store since it's validation just arrived
        }
        return KeyValue.pair(key, value); //Forwarding the validation
    }

    /**
     * Esse método verifica cada boleto com validacão pendente na store local
     *
     * @param timestamp - timestamp
     */
    public void enforceTtl(Long timestamp) {
        // create a iterator from the local store to get all boletos with pending validation
        try (KeyValueIterator<Long, Boleto> iter = this.kvStore.all()) {

            // loop through the iterator
            while (iter.hasNext()) {
                KeyValue<Long, Boleto> entry = iter.next();

                System.out.println("----------------------------------------------------------");
                System.out.println("Checking if the validation period for the boleto " + entry.key + " has expired");

                Instant created = Instant.ofEpochMilli(entry.value.getReceivedDate());

                long secondsSinceCreated = Duration.between(created, Instant.now()).toSeconds();

                System.out.println("Boleto " + entry.key + " - seconds since created: " + secondsSinceCreated);

                // Verifies since the creation of a boleto in order to determine which ones are expired
                if (secondsSinceCreated >= 8) {
                    System.out.println("----------------------------------------------------------");
                    System.out.println("Boleto " + entry.key + " expired!");
                    this.kvStore.delete(entry.key);

                    // Foward the boleto without validation
                    this.context.forward(entry.key, new BoletoValidation(entry.value, null, null, null, true));
                }
            }
        } catch (Exception e){
            System.out.println("----------------------------------------------------------");
            System.out.println("Exception on method enforceTTL =" + e);
        }
    }

    /**
     * Encerra o Transformer
     *
     */
    @Override
    public void close() {
        this.punctuator.cancel();
    }
}
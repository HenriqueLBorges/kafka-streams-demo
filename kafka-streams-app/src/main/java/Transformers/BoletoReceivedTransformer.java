package Transformers;

import DataModels.Boleto;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class BoletoReceivedTransformer implements Transformer<Long, Boleto, KeyValue<Long, Boleto>> {
    private ProcessorContext context;
    private KeyValueStore<Long, Boleto> kvStore;
    private static String STORE_NAME = System.getenv("STORE_NAME") != null ? System.getenv("STORE_NAME") : "boletos-to-validade";

    /**
     * Init the Transformer
     *
     * @param context - Processor context
     */
    @Override
    public void init(ProcessorContext context) {

        this.context = context;

        // retrieve the key-value store
        this.kvStore = (KeyValueStore) context.getStateStore(STORE_NAME);
    }

    /**
     * Our transform is chained on the processor flow for validations that arrive, every boleto received trigger this transform method. This method inserts every boleto on local store.
     *
     * @param key - Event key
     * @param value - Boleto received for validate
     */
    @Override
    public KeyValue<Long, Boleto> transform(Long key, Boleto value) {
        System.out.println("----------------------------------------------------------");
        System.out.println("Processing boleto: " + key);

        System.out.println("Boleto " + key + " received, saving it on local store");

        // Saves every new boleto received on the local store
        kvStore.put(key, value);

        return KeyValue.pair(key, value);
    }

    /**
     * Close the Transformer
     *
     */
    @Override
    public void close() {
    }
}
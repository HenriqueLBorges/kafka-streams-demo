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
     * Inicia o Transformer
     *
     * @param context - Contexto do processor
     */
    @Override
    public void init(ProcessorContext context) {

        this.context = context;

        // retrieve the key-value store
        this.kvStore = (KeyValueStore) context.getStateStore(STORE_NAME);
    }

    /**
     * Nosso Transformer é encadeado no fluxo de processamento de boletos, cada boleto recebido invoca esse método. Esse método insere cada boleto na store local.
     *
     * @param key - Chave do evento
     * @param value - Boleto recebido
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
     * Encerra o Transformer
     *
     */
    @Override
    public void close() {
    }
}
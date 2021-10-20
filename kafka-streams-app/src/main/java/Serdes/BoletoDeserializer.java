package Serdes;

import DataModels.Boleto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

public class BoletoDeserializer implements Deserializer <Boleto> {

    /**
     * Encerra
     *
     */
    @Override
    public void close() {
    }

    /**
     * Configura
     *
     */
    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
    }

    /**
     * Desserializa o array de bytes em um objeto Boleto
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public Boleto deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        Boleto boleto = null;
        try {
            boleto = mapper.readValue(arg1, Boleto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boleto;
    }
}

package Serdes;

import DataModels.Boleto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class BoletoSerializer implements Serializer <Boleto> {
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Configura
     *
     */
    @Override
    public void configure(final Map<String, ?> settings, final boolean isKey) {

    }

    /**
     * Serializa o objeto de Boleto em um array de bytes
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public byte[] serialize(final String arg0, final Boleto arg1) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(arg1).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * Encerra
     *
     */
    @Override
    public void close() {
    }
}

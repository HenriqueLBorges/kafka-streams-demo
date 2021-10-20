package Serdes;

import DataModels.BoletoValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class BoletoValidationSerializer implements Serializer <BoletoValidation> {

    /**
     * Confira
     *
     */
    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    /**
     * Serializa um objeto BoletoValidation em um array de bytes
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public byte[] serialize(String arg0, BoletoValidation arg1) {
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

package Serdes;

import DataModels.BoletoValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class BoletoValidationSerializer implements Serializer <BoletoValidation> {

    /**
     * configure
     *
     */
    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    /**
     * Serialize the BoletoValidation object
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
     * close
     *
     */
    @Override
    public void close() {
    }
}

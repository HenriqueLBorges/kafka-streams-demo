package Serdes;

import DataModels.BoletoValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class BoletoValidationDeserializer implements Deserializer <BoletoValidation> {

    /**
     * close
     *
     */
    @Override
    public void close() {
    }

    /**
     * configure
     *
     */
    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
    }

    /**
     * Deserialize the byte array into the BoletoValidation object
     *
     * @param arg0
     * @param arg1
     */
    @Override
    public BoletoValidation deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        BoletoValidation boletoValidation = null;
        try {
            boletoValidation = mapper.readValue(arg1, BoletoValidation.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boletoValidation;
    }
}

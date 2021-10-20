package Serdes;

import DataModels.BoletoValidation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class BoletoValidationDeserializer implements Deserializer <BoletoValidation> {

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
     * Desserializa o array de bytes em um objeto BoletoValidation
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

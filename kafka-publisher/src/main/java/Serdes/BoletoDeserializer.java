package Serdes;

import DataModels.Boleto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import java.util.Map;

public class BoletoDeserializer implements Deserializer <Boleto> {

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
     * Deserialize the byte array into the Boleto object
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

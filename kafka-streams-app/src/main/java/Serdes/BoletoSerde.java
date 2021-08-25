package Serdes;

import DataModels.Boleto;
import org.apache.kafka.common.serialization.Serdes;

public class BoletoSerde extends Serdes.WrapperSerde<Boleto> {

    /**
     * Create a BoletoSerde
     *
     */
    public BoletoSerde() {
        super(new BoletoSerializer(), new BoletoDeserializer());
    }

}

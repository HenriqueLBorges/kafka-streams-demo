package Serdes;

import DataModels.BoletoValidation;
import org.apache.kafka.common.serialization.Serdes;

public class BoletoValidationSerde extends Serdes.WrapperSerde<BoletoValidation> {

    /**
     * Cria um BoletoValidationSerde
     *
     */
    public BoletoValidationSerde() {
        super(new BoletoValidationSerializer(), new BoletoValidationDeserializer());
    }
    
}

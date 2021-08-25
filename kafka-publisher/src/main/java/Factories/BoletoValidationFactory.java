package Factories;

import DataModels.Boleto;
import DataModels.BoletoValidation;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class BoletoValidationFactory {
    final private Random random = new Random();

    public BoletoValidation validate(Boleto boleto) {
        return new BoletoValidation(boleto, Instant.now().getEpochSecond(), this.generateID(), this.generateValidation(), false);
    }

    public BoletoValidation[] validate(Boleto[] boletos) {
        BoletoValidation[] boletoValidations = new BoletoValidation[boletos.length];

        for (int i = 0; i < boletos.length; i++) {
            boletoValidations[i] = validate(boletos[i]);
        }

        return boletoValidations;
    }

    private Boolean generateValidation() {
        return this.random.nextBoolean();
    }

    private String generateID() {
        return UUID.randomUUID().toString();
    }
}

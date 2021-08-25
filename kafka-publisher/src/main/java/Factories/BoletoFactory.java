package Factories;

import DataModels.Boleto;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Random;

public class BoletoFactory {
    final private Random random = new Random();
    final private static int minValue = 50;
    final private static int maxValue = 20000;
    final private static String[] banks = {"Itaú Unibanco", "Bradesco", "Santander", "Banco do Brasil"};

    public Boleto createBoleto () {
        final Double total = generateValue();

        return new Boleto(total, this.generateBank(), ZonedDateTime.now().toInstant().toEpochMilli(), ZonedDateTime.now().toInstant().toEpochMilli());
    }

    public Boleto[] createBoletos (int size) {
        Boleto[] boletos = new Boleto[size];

        for (int i = 0; i < size; i++) {
            boletos[i] = createBoleto();
        }

        return boletos;
    }

    private Double generateValue() {
        return this.generateRandomNumber(minValue, maxValue);
    }

    private Double generateRandomNumber(int minValue, int maxValue) {
        double theRandomValue = 0.0;

        // Checking for a valid range
        if(Double.valueOf(maxValue - minValue).isInfinite() == false) {
            theRandomValue = minValue + (maxValue - minValue) * random.nextDouble();
        }

        return theRandomValue;
    }

    private String generateBank () {
        int index = generateRandomNumber(0, 5).intValue();

        if (index < 0 || index > 3) {
            return banks[0];
        }
        return banks[index];
    }

}

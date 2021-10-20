package DataModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;

@JsonIgnoreProperties({"validationDateFormatted"})
public class BoletoValidation {
    @JsonProperty("boleto")
    private final Boleto boleto;
    @JsonProperty("validationDate")
    private final Long validationDate;
    @JsonProperty("validationID")
    private final String validationID;
    @JsonProperty("valid")
    private final Boolean valid;
    @JsonProperty("expired")
    private Boolean expired;

    /**
     * Cria uma validação de boleto com todas as propriedades como null
     *
     * @return BoletoValidation - Objeto de validação de boleto com propriedades null
     */
    public BoletoValidation() {
        this.boleto = null;
        this.validationDate = null;
        this.validationID = null;
        this.valid = null;
        this.expired = null;
    }

    /**
     * Cria uma validação de boleto
     *
     * @param Boleto - Boleto que foi validado
     * @param Long - Timestamp da validação
     * @param String - Identificador da validação
     * @param Boolean - Resultado da validação
     * @param Boolean - Status da expiração
     * @return BoletoValidation - Objeto de validação de boleto
     */
    public BoletoValidation(Boleto boleto, Long validationDate, String validationID, Boolean valid, Boolean expired) {
        this.boleto = boleto;
        this.validationDate = validationDate;
        this.validationID = validationID;
        this.valid = valid;
        this.expired = expired;
    }

    /**
     * Retorna o boleto que foi validado
     *
     * @return Boleto - Boleto validado
     */
    public Boleto getBoleto() {
        return this.boleto;
    }

    /**
     * Retorna a data de validação
     *
     * @return Long - Timestamp da data de validação
     */
    public Long getValidationDate() {
        return this.validationDate;
    }

    /**
     * Retorna o identificador da validação
     *
     * @return String - Identificador da validação
     */
    public String getValidationID() {
        return this.validationID;
    }

    /**
     * Retorna o status da validação
     *
     * @return Boolean - Resultado da validação
     */
    public Boolean getValid() {
        return this.valid;
    }

    /**
     * Seta a expiração
     *
     * @param Boolean - Status da expiração
     */
    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    /**
     * Retorna o status da expiração
     *
     * @return Boolean - Status da expiração
     */
    public Boolean getExpired() {
        return this.expired;
    }

    /**
     * Retorna a data de validação formatada como: DD/MM/YYYY hh:mm:ss.mmm
     *
     * @return String - Data de validação formatada
     */
    public String getValidationDateFormatted() {
        return this.getDateFormatted(this.getValidationDate());
    }

    /**
     * Converte uma data para DD/MM/YYYY hh:mm:ss.mmm
     * @param Long - Timestamp in millis
     * @return String - Data de validação formatada
     */
    private String getDateFormatted(Long milli) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milli);
        return calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH)
                + "/" + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR)
                + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND)
                + "." + calendar.get(Calendar.MILLISECOND) + " " + (calendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
    }

    /**
     * Converte o objeto para String
     * @return String - Objeto convertido para String
     */
    public String toString() {
        if(this.getValidationDate() == null || this.getValidationID() == null || this.getValid() == null){
            return "Boleto: " + this.getBoleto().toString() + "\nValidation date: null\nValidation ID: null\nValid: null\nExpired: " + this.expired;
        }
        return "Boleto: " + this.getBoleto().toString() + "\nValidation date: " + this.getValidationDateFormatted()
                + "\nValidation ID: " + this.getValidationID() + "\nValid: " + this.getValid() + "\nExpired: " + this.getExpired();
    }
}

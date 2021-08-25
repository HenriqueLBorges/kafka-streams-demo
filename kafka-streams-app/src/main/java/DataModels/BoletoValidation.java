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
     * Create a Boleto Validation with every property null
     *
     * @return BoletoValidation - the boleto validation object
     */
    public BoletoValidation() {
        this.boleto = null;
        this.validationDate = null;
        this.validationID = null;
        this.valid = null;
        this.expired = null;
    }

    /**
     * Create a Boleto Validation
     *
     * @param Boleto - The boleto that was validated
     * @param Long - The validation date timestamp
     * @param String - The validation identification
     * @param Boolean - The validation result
     * @param Boolean - Expired status
     * @return BoletoValidation - the boleto validation object
     */
    public BoletoValidation(Boleto boleto, Long validationDate, String validationID, Boolean valid, Boolean expired) {
        this.boleto = boleto;
        this.validationDate = validationDate;
        this.validationID = validationID;
        this.valid = valid;
        this.expired = expired;
    }

    /**
     * Get the original boleto from validation
     *
     * @return Boleto - The boleto that was validated
     */
    public Boleto getBoleto() {
        return this.boleto;
    }

    /**
     * Get the validation date
     *
     * @return Long - The validation date timestamp
     */
    public Long getValidationDate() {
        return this.validationDate;
    }

    /**
     * Get the validation identification
     *
     * @return String - The validation identification
     */
    public String getValidationID() {
        return this.validationID;
    }

    /**
     * Get the validation status
     *
     * @return Boolean - The validation result
     */
    public Boolean getValid() {
        return this.valid;
    }

    /**
     * Set expired
     *
     * @param Boolean - The expired status
     */
    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    /**
     * Get the expired status
     *
     * @return Boolean - The expired status
     */
    public Boolean getExpired() {
        return this.expired;
    }

    /**
     * Return the validation date formatted as DD/MM/YYYY hh:mm:ss.mmm
     *
     * @return String - Received date formatted
     */
    public String getValidationDateFormatted() {
        return this.getDateFormatted(this.getValidationDate());
    }

    /**
     * Return the received date formatted as DD/MM/YYYY hh:mm:ss.mmm
     * @param Long - Timestamp in millis
     * @return String - Received date formatted
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
     * Convert the object to String
     * @return String - Object converted
     */
    public String toString() {
        if(this.getValidationDate() == null || this.getValidationID() == null || this.getValid() == null){
            return "Boleto: " + this.getBoleto().toString() + "\nValidation date: null\nValidation ID: null\nValid: null\nExpired: " + this.expired;
        }
        return "Boleto: " + this.getBoleto().toString() + "\nValidation date: " + this.getValidationDateFormatted()
                + "\nValidation ID: " + this.getValidationID() + "\nValid: " + this.getValid() + "\nExpired: " + this.getExpired();
    }
}

package DataModels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;

@JsonIgnoreProperties({"dueDateFormatted", "receivedDateFormatted"})
public class Boleto {
    @JsonProperty("total")
    private Double total;
    @JsonProperty("bank")
    private String bank;
    @JsonProperty("dueDate")
    private Long dueDate;
    @JsonProperty("receivedDate")
    private Long receivedDate;

    /**
     * Create a Boleto with every property null
     *
     * @return Boleto - the boleto object
     */
    public Boleto() {
        this.total = null;
        this.bank = null;
        this.dueDate = null;
        this.receivedDate = null;
    }

    /**
     * Create a Boleto
     *
     * @param total - total value of the boleto
     * @param bank - The bank that emitted the boleto
     * @param dueDate - due date of the boleto
     * @param receivedDate - received date of the boleto
     * @return Boleto - the boleto object
     */
    public Boleto(Double total, String bank, Long dueDate, Long receivedDate) {
        this.total = total;
        this.bank = bank;
        this.dueDate = dueDate;
        this.receivedDate = receivedDate;
    }

    /**
     * Return the total value of the boleto
     *
     * @return Double - total value of the boleto
     */
    public Double getTotal() {
        return this.total;
    }

    /**
     * Return the name of the bank that emitted the boleto
     *
     * @return String - Name of the bank
     */
    public String getBank() {
        return this.bank;
    }

    /**
     * Return the due date timestamp
     *
     * @return Long - Due date timestamp
     */
    public Long getDueDate() {
        return this.dueDate;
    }

    /**
     * Return the received date timestamp
     *
     * @return Long - Received date timestamp
     */
    public Long getReceivedDate() {
        return this.receivedDate;
    }

    /**
     * Return the due date formatted as DD/MM/YYYY hh:mm:ss.mmm
     *
     * @return String - Due date formatted
     */
    public String getDueDateFormatted() {
        return this.getDateFormatted(this.getDueDate());
    }

    /**
     * Return the received date formatted as DD/MM/YYYY hh:mm:ss.mmm
     *
     * @return String - Received date formatted
     */
    public String getReceivedDateFormatted() {
        return this.getDateFormatted(this.getReceivedDate());
    }

    /**
     * Return the received date formatted as DD/MM/YYYY hh:mm:ss.mmm
     * @param milli - Timestamp in millis
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
        return "Bank: " + this.getBank() + "\nTotal: " + this.getTotal() + "\nDue date: " + this.getDueDateFormatted() + "\nReceived date: " + this.getReceivedDateFormatted();
    }
}
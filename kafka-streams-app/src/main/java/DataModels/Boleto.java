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
     * Cria um Boleto com todas as propriedades como null
     *
     * @return Boleto - Objeto de Boleto com propriedades null
     */
    public Boleto() {
        this.total = null;
        this.bank = null;
        this.dueDate = null;
        this.receivedDate = null;
    }

    /**
     * Cria um boleto
     *
     * @param total - Valor total do boleto
     * @param bank - Banco que emitiu o boleto
     * @param dueDate - Data de vencimento do boleto
     * @param receivedDate - Data de recebimento do boleto
     * @return Boleto - Objeto de boleto
     */
    public Boleto(Double total, String bank, Long dueDate, Long receivedDate) {
        this.total = total;
        this.bank = bank;
        this.dueDate = dueDate;
        this.receivedDate = receivedDate;
    }

    /**
     * Retorna o valor total do boleto
     *
     * @return Double - Valor total do boleto
     */
    public Double getTotal() {
        return this.total;
    }

    /**
     * Retorna o nome do banco que emitiu o boleto
     *
     * @return String - Nome do banco
     */
    public String getBank() {
        return this.bank;
    }

    /**
     * Retorna a data de vencimento do boleto
     *
     * @return Long - Timestamp da Data de vencimento do boleto
     */
    public Long getDueDate() {
        return this.dueDate;
    }

    /**
     * Retorna a data de recebimento do boleto
     *
     * @return Long - Timestamp da Data de recebimento do boleto
     */
    public Long getReceivedDate() {
        return this.receivedDate;
    }

    /**
     * Retorna a data de vencimento formatada como: DD/MM/YYYY hh:mm:ss.mmm
     *
     * @return String - Data de vencimento formatada
     */
    public String getDueDateFormatted() {
        return this.getDateFormatted(this.getDueDate());
    }

    /**
     * Retorna a data de recebimento formatada como: DD/MM/YYYY hh:mm:ss.mmm
     *
     * @return String - Data de recebimento formatada
     */
    public String getReceivedDateFormatted() {
        return this.getDateFormatted(this.getReceivedDate());
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
        return "Bank: " + this.getBank() + "\nTotal: " + this.getTotal() + "\nDue date: " + this.getDueDateFormatted() + "\nReceived date: " + this.getReceivedDateFormatted();
    }
}
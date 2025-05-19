package fcu.app.distributionapp.model;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class Transaction  implements Serializable {
    @DocumentId
    private String id;
    private String payer;
    private double amount;
    private String currency;
    private String note;
    private String date;
    private List<String> beneficiaries;

    public Transaction() {} // Firestore 需要空建構子

    public Transaction(String payer, double amount, String currency, String note, String date, List<String> beneficiaries) {
        this.payer = payer;
        this.amount = amount;
        this.currency = currency;
        this.note = note;
        this.date = date;
        this.beneficiaries = beneficiaries;
    }

    public String getPayer() {
        return payer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(List<String> beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

}

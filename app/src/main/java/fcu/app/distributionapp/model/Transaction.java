package fcu.app.distributionapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

public class Transaction implements Parcelable {
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

    // ===== Parcelable 實作區 =====
    protected Transaction(Parcel in) {
        id = in.readString();
        payer = in.readString();
        amount = in.readDouble();
        currency = in.readString();
        note = in.readString();
        date = in.readString();
        beneficiaries = in.createStringArrayList();
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(payer);
        dest.writeDouble(amount);
        dest.writeString(currency);
        dest.writeString(note);
        dest.writeString(date);
        dest.writeStringList(beneficiaries);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // ===== Getter / Setter =====
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
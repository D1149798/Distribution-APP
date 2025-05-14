package fcu.app.distributionapp.model;

public class DetailGroup {
    public String payer;       // 付款方
    public String receiver;    // 被付款方（可為 ALL 或個人）
    public String note;        // 備註說明
    public int amount;         // 金額
    public String date;        // 日期（例如 2025.2.28）

    public DetailGroup(String payer, String receiver, String note, int amount, String date) {
        this.payer = payer;
        this.receiver = receiver;
        this.note = note;
        this.amount = amount;
        this.date = date;
    }

}

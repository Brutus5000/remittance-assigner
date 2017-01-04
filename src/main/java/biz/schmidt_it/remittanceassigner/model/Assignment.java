package biz.schmidt_it.remittanceassigner.model;

import javafx.beans.property.SimpleStringProperty;

import javax.money.MonetaryAmount;

public class Assignment {
    private Invoice invoice;
    private double cashback;

    public Assignment(Invoice invoice, double cashback){
        this.invoice = invoice;
        this.cashback = cashback;
    }

    public String getInvoiceId() { return invoice.getId();}

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public double getCashback() {
        return cashback;
    }

    public void setCashback(int cashback) {
        this.cashback = cashback;
    }

    public MonetaryAmount getAppliedAmount() {
        return invoice.getDeductedAmount(cashback);
    }

}

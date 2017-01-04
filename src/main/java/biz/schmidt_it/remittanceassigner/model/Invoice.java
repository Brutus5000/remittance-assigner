package biz.schmidt_it.remittanceassigner.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.money.MonetaryAmount;

public class Invoice {
    private SimpleStringProperty id;
    private MonetaryAmount amount;

    public Invoice(String id, MonetaryAmount amount) {
        this.id = new SimpleStringProperty(id);
        this.amount = amount;
    }

    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() { return id; }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public void setAmount(MonetaryAmount amount) {
        this.amount = amount;
    }

    public MonetaryAmount getDeductedAmount(double cashback){
        return amount.multiply(1.0d-cashback);
    }
}

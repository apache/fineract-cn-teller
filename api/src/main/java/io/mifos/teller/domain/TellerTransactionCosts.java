package io.mifos.teller.domain;

import java.util.List;

public class TellerTransactionCosts {

  private String tellerTransactionIdentifier;
  private Double totalAmount;
  private List<Charge> charges;

  public TellerTransactionCosts() {
    super();
  }

  public String getTellerTransactionIdentifier() {
    return this.tellerTransactionIdentifier;
  }

  public void setTellerTransactionIdentifier(final String tellerTransactionIdentifier) {
    this.tellerTransactionIdentifier = tellerTransactionIdentifier;
  }

  public Double getTotalAmount() {
    return this.totalAmount;
  }

  public void setTotalAmount(final Double totalAmount) {
    this.totalAmount = totalAmount;
  }

  public List<Charge> getCharges() {
    return this.charges;
  }

  public void setCharges(final List<Charge> charges) {
    this.charges = charges;
  }
}

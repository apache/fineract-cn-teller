package io.mifos.teller.domain;

import io.mifos.core.lang.validation.constraints.ValidIdentifier;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class TellerTransaction {

  public enum State {
    PENDING,
    CANCELED,
    CONFIRMED
  }

  @ValidIdentifier(optional = true)
  private String identifier;
  @NotNull
  private String transactionType;
  @NotNull
  private String transactionDate;
  @ValidIdentifier
  private String productIdentifier;
  @ValidIdentifier
  private String customerAccountIdentifier;
  @ValidIdentifier
  private String clerk;
  @NotNull
  @DecimalMin("0.00")
  private Double amount;
  private State state;

  public TellerTransaction() {
    super();
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setIdentifier(final String identifier) {
    this.identifier = identifier;
  }

  public String getTransactionType() {
    return this.transactionType;
  }

  public void setTransactionType(final String transactionType) {
    this.transactionType = transactionType;
  }

  public String getTransactionDate() {
    return this.transactionDate;
  }

  public void setTransactionDate(final String transactionDate) {
    this.transactionDate = transactionDate;
  }

  public String getProductIdentifier() {
    return this.productIdentifier;
  }

  public void setProductIdentifier(final String productIdentifier) {
    this.productIdentifier = productIdentifier;
  }

  public String getCustomerAccountIdentifier() {
    return this.customerAccountIdentifier;
  }

  public void setCustomerAccountIdentifier(final String customerAccountIdentifier) {
    this.customerAccountIdentifier = customerAccountIdentifier;
  }

  public String getClerk() {
    return this.clerk;
  }

  public void setClerk(final String clerk) {
    this.clerk = clerk;
  }

  public Double getAmount() {
    return this.amount;
  }

  public void setAmount(final Double amount) {
    this.amount = amount;
  }

  public String getState() {
    return this.state.name();
  }

  public void setState(final String state) {
    this.state = State.valueOf(state);
  }
}

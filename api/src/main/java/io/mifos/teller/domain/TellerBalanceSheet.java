package io.mifos.teller.domain;

import java.util.List;

public class TellerBalanceSheet {

  private String day;
  private Double balance;
  private List<TellerEntry> entries;

  public TellerBalanceSheet() {
    super();
  }

  public String getDay() {
    return this.day;
  }

  public void setDay(final String day) {
    this.day = day;
  }

  public Double getBalance() {
    return this.balance;
  }

  public void setBalance(final Double balance) {
    this.balance = balance;
  }

  public List<TellerEntry> getEntries() {
    return this.entries;
  }

  public void setEntries(final List<TellerEntry> entries) {
    this.entries = entries;
  }
}

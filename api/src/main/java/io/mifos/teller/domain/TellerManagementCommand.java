package io.mifos.teller.domain;

import io.mifos.core.lang.validation.constraints.ValidIdentifier;

import javax.validation.constraints.NotNull;

public class TellerManagementCommand {

  public enum Action {
    OPEN,
    CLOSE
  }

  public enum Adjustment {
    DEBIT,
    CREDIT
  }

  @NotNull
  private Action action;
  private Adjustment adjustment;
  private Double amount;
  @ValidIdentifier(optional = true)
  private String assignedEmployeeIdentifier;

  public TellerManagementCommand() {
    super();
  }

  public String getAction() {
    return this.action.name();
  }

  public void setAction(final String action) {
    this.action = Action.valueOf(action);
  }

  public String getAdjustment() {
    return this.adjustment.name();
  }

  public void setAdjustment(final String adjustment) {
    this.adjustment = Adjustment.valueOf(adjustment);
  }

  public Double getAmount() {
    return this.amount;
  }

  public void setAmount(final Double amount) {
    this.amount = amount;
  }

  public String getAssignedEmployeeIdentifier() {
    return this.assignedEmployeeIdentifier;
  }

  public void setAssignedEmployeeIdentifier(final String assignedEmployeeIdentifier) {
    this.assignedEmployeeIdentifier = assignedEmployeeIdentifier;
  }
}

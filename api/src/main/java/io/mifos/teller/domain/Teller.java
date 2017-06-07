package io.mifos.teller.domain;

import io.mifos.core.lang.validation.constraints.ValidIdentifier;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

public class Teller {

  public enum Status {
    ACTIVE,
    CLOSED,
    OPEN,
    PAUSED
  }

  @ValidIdentifier
  private String code;
  @NotEmpty
  private String password;
  @NotNull
  @DecimalMin("0.00")
  private Double cashdrawLimit;
  @ValidIdentifier
  private String tellerAccountIdentifier;
  @ValidIdentifier
  private String vaultAccountIdentifier;
  private String status;
  private String createdBy;
  private String createdOn;
  private String lastModifiedBy;
  private String lastModifiedOn;

  public Teller() {
    super();
  }

  public String getCode() {
    return this.code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public Double getCashdrawLimit() {
    return this.cashdrawLimit;
  }

  public void setCashdrawLimit(final Double cashdrawLimit) {
    this.cashdrawLimit = cashdrawLimit;
  }

  public String getTellerAccountIdentifier() {
    return this.tellerAccountIdentifier;
  }

  public void setTellerAccountIdentifier(final String tellerAccountIdentifier) {
    this.tellerAccountIdentifier = tellerAccountIdentifier;
  }

  public String getVaultAccountIdentifier() {
    return this.vaultAccountIdentifier;
  }

  public void setVaultAccountIdentifier(final String vaultAccountIdentifier) {
    this.vaultAccountIdentifier = vaultAccountIdentifier;
  }

  public String getStatus() {
    return this.status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public String getCreatedOn() {
    return this.createdOn;
  }

  public void setCreatedOn(final String createdOn) {
    this.createdOn = createdOn;
  }

  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  public void setLastModifiedBy(final String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public String getLastModifiedOn() {
    return this.lastModifiedOn;
  }

  public void setLastModifiedOn(final String lastModifiedOn) {
    this.lastModifiedOn = lastModifiedOn;
  }
}

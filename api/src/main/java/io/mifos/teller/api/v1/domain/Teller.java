/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.teller.api.v1.domain;

import io.mifos.core.lang.validation.constraints.ValidIdentifier;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class Teller {

  public enum State {
    ACTIVE,
    CLOSED,
    OPEN,
    PAUSED
  }

  @ValidIdentifier
  private String code;
  @NotEmpty
  private String password;
  @DecimalMin("0.00")
  @DecimalMax("1000000000.00")
  private BigDecimal cashdrawLimit;
  @ValidIdentifier
  private String tellerAccountIdentifier;
  @ValidIdentifier
  private String vaultAccountIdentifier;
  private String assignedEmployee;
  private State state;
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

  public BigDecimal getCashdrawLimit() {
    return this.cashdrawLimit;
  }

  public void setCashdrawLimit(final BigDecimal cashdrawLimit) {
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

  public String getAssignedEmployee() {
    return this.assignedEmployee;
  }

  public void setAssignedEmployee(final String assignedEmployee) {
    this.assignedEmployee = assignedEmployee;
  }

  public String getState() {
    if (this.state != null) {
      return this.state.name();
    } else {
      return null;
    }
  }

  public void setState(final String state) {
    this.state = State.valueOf(state);
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

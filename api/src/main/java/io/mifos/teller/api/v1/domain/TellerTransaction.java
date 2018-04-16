/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mifos.teller.api.v1.domain;

import java.math.BigDecimal;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import org.apache.fineract.cn.lang.validation.constraints.ValidIdentifier;

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
  private String customerIdentifier;
  @ValidIdentifier
  private String productIdentifier;
  @ValidIdentifier(optional = true)
  private String productCaseIdentifier;
  @ValidIdentifier(maxLength = 34)
  private String customerAccountIdentifier;
  @ValidIdentifier(maxLength = 34, optional = true)
  private String targetAccountIdentifier;
  @ValidIdentifier
  private String clerk;
  @NotNull
  @DecimalMin(value = "0.000")
  @DecimalMax(value = "9999999999.99999")
  private BigDecimal amount;
  private State state;
  @Valid
  private Cheque cheque;

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

  public String getCustomerIdentifier() {
    return this.customerIdentifier;
  }

  public void setCustomerIdentifier(final String customerIdentifier) {
    this.customerIdentifier = customerIdentifier;
  }

  public String getProductIdentifier() {
    return this.productIdentifier;
  }

  public void setProductIdentifier(final String productIdentifier) {
    this.productIdentifier = productIdentifier;
  }

  public String getProductCaseIdentifier() {
    return this.productCaseIdentifier;
  }

  public void setProductCaseIdentifier(final String productCaseIdentifier) {
    this.productCaseIdentifier = productCaseIdentifier;
  }

  public String getCustomerAccountIdentifier() {
    return this.customerAccountIdentifier;
  }

  public void setCustomerAccountIdentifier(final String customerAccountIdentifier) {
    this.customerAccountIdentifier = customerAccountIdentifier;
  }

  public String getTargetAccountIdentifier() {
    return this.targetAccountIdentifier;
  }

  public void setTargetAccountIdentifier(final String targetAccountIdentifier) {
    this.targetAccountIdentifier = targetAccountIdentifier;
  }

  public String getClerk() {
    return this.clerk;
  }

  public void setClerk(final String clerk) {
    this.clerk = clerk;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public void setAmount(final BigDecimal amount) {
    this.amount = amount;
  }

  public String getState() {
    return this.state.name();
  }

  public void setState(final String state) {
    this.state = State.valueOf(state);
  }

  public Cheque getCheque() {
    return this.cheque;
  }

  public void setCheque(final Cheque cheque) {
    this.cheque = cheque;
  }
}

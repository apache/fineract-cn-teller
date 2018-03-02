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

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

public class TellerBalanceSheet {

  private String day;
  @DecimalMin(value = "0.001")
  @DecimalMax(value = "9999999999.99999")
  private BigDecimal cashOnHand;
  private BigDecimal cashReceivedTotal;
  private BigDecimal cashDisbursedTotal;
  private BigDecimal chequesReceivedTotal;
  private List<TellerEntry> cashEntries;
  private List<TellerEntry> chequeEntries;

  public TellerBalanceSheet() {
    super();
  }

  public String getDay() {
    return this.day;
  }

  public void setDay(final String day) {
    this.day = day;
  }

  public BigDecimal getCashOnHand() {
    return this.cashOnHand;
  }

  public void setCashOnHand(final BigDecimal cashOnHand) {
    this.cashOnHand = cashOnHand;
  }

  public BigDecimal getCashReceivedTotal() {
    return this.cashReceivedTotal;
  }

  public void setCashReceivedTotal(final BigDecimal cashReceivedTotal) {
    this.cashReceivedTotal = cashReceivedTotal;
  }

  public BigDecimal getCashDisbursedTotal() {
    return this.cashDisbursedTotal;
  }

  public void setCashDisbursedTotal(final BigDecimal cashDisbursedTotal) {
    this.cashDisbursedTotal = cashDisbursedTotal;
  }

  public BigDecimal getChequesReceivedTotal() {
    return this.chequesReceivedTotal;
  }

  public void setChequesReceivedTotal(final BigDecimal chequesReceivedTotal) {
    this.chequesReceivedTotal = chequesReceivedTotal;
  }

  public List<TellerEntry> getCashEntries() {
    return this.cashEntries;
  }

  public void setCashEntries(final List<TellerEntry> cashEntries) {
    this.cashEntries = cashEntries;
  }

  public List<TellerEntry> getChequeEntries() {
    return this.chequeEntries;
  }

  public void setChequeEntries(final List<TellerEntry> chequeEntries) {
    this.chequeEntries = chequeEntries;
  }
}

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

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

public class TellerBalanceSheet {

  private String day;
  @DecimalMin("0.00")
  private BigDecimal balance;
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

  public BigDecimal getBalance() {
    return this.balance;
  }

  public void setBalance(final BigDecimal balance) {
    this.balance = balance;
  }

  public List<TellerEntry> getEntries() {
    return this.entries;
  }

  public void setEntries(final List<TellerEntry> entries) {
    this.entries = entries;
  }
}

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

public class TellerTransactionCosts {

  private String tellerTransactionIdentifier;
  @DecimalMin("0.00")
  private BigDecimal totalAmount;
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

  public BigDecimal getTotalAmount() {
    return this.totalAmount;
  }

  public void setTotalAmount(final BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public List<Charge> getCharges() {
    return this.charges;
  }

  public void setCharges(final List<Charge> charges) {
    this.charges = charges;
  }
}

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

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Cheque {
  @Valid
  private MICR micr;
  @NotEmpty
  private String drawee;
  @NotEmpty
  private String drawer;
  @NotEmpty
  private String payee;
  @NotNull
  @DecimalMin(value = "0.00", inclusive = false)
  private BigDecimal amount;
  @NotEmpty
  private String dateIssued;
  private Boolean openCheque;

  public Cheque() {
    super();
  }

  public MICR getMicr() {
    return this.micr;
  }

  public void setMicr(final MICR micr) {
    this.micr = micr;
  }

  public String getDrawee() {
    return this.drawee;
  }

  public void setDrawee(final String drawee) {
    this.drawee = drawee;
  }

  public String getDrawer() {
    return this.drawer;
  }

  public void setDrawer(final String drawer) {
    this.drawer = drawer;
  }

  public String getPayee() {
    return this.payee;
  }

  public void setPayee(final String payee) {
    this.payee = payee;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }

  public void setAmount(final BigDecimal amount) {
    this.amount = amount;
  }

  public String getDateIssued() {
    return this.dateIssued;
  }

  public void setDateIssued(final String dateIssued) {
    this.dateIssued = dateIssued;
  }

  public Boolean isOpenCheque() {
    return this.openCheque;
  }

  public void setOpenCheque(final Boolean openCheque) {
    this.openCheque = openCheque;
  }
}

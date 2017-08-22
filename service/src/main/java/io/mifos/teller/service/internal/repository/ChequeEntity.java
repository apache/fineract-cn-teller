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
package io.mifos.teller.service.internal.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

@Entity
@Table(name = "tajet_cheques")
public class ChequeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @Column(name = "teller_transaction_id", nullable = false)
  private Long tellerTransactionId;
  @Column(name = "cheque_number", nullable = false, length = 8)
  private String chequeNumber;
  @Column(name = "branch_sort_code", nullable = false, length = 11)
  private String branchSortCode;
  @Column(name = "account_number", nullable = false, length = 34)
  private String accountNumber;
  @Column(name = "drawee", nullable = false, length = 2048)
  private String drawee;
  @Column(name = "drawer", nullable = false, length = 256)
  private String drawer;
  @Column(name = "payee", nullable = false, length = 256)
  private String payee;
  @Column(name = "amount", nullable = false)
  private Double amount;
  @Column(name = "date_issued", nullable = false)
  private Date dateIssued;
  @Column(name = "open_cheque", nullable = true)
  private Boolean openCheque;

  public ChequeEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public Long getTellerTransactionId() {
    return this.tellerTransactionId;
  }

  public void setTellerTransactionId(final Long tellerTransactionId) {
    this.tellerTransactionId = tellerTransactionId;
  }

  public String getChequeNumber() {
    return this.chequeNumber;
  }

  public void setChequeNumber(final String chequeNumber) {
    this.chequeNumber = chequeNumber;
  }

  public String getBranchSortCode() {
    return this.branchSortCode;
  }

  public void setBranchSortCode(final String branchSortCode) {
    this.branchSortCode = branchSortCode;
  }

  public String getAccountNumber() {
    return this.accountNumber;
  }

  public void setAccountNumber(final String accountNumber) {
    this.accountNumber = accountNumber;
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

  public Double getAmount() {
    return this.amount;
  }

  public void setAmount(final Double amount) {
    this.amount = amount;
  }

  public Date getDateIssued() {
    return this.dateIssued;
  }

  public void setDateIssued(final Date dateIssued) {
    this.dateIssued = dateIssued;
  }

  public Boolean getOpenCheque() {
    return this.openCheque;
  }

  public void setOpenCheque(final Boolean openCheque) {
    this.openCheque = openCheque;
  }
}

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
package org.apache.fineract.cn.teller.service.internal.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.cn.postgresql.util.LocalDateTimeConverter;

@Entity
@Table(name = "tajet_teller_transactions")
public class TellerTransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;
  @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "teller_id")
  private TellerEntity teller;
  @Column(name = "identifier", nullable = false, length = 32)
  private String identifier;
  @Column(name = "transaction_type", nullable = false, length = 32)
  private String transactionType;
  @Column(name = "transaction_date", nullable = false)
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime transactionDate;
  @Column(name = "customer_identifier", nullable = false, length = 32)
  private String customerIdentifier;
  @Column(name = "product_identifier", nullable = false, length = 32)
  private String productIdentifier;
  @Column(name = "product_case_identifier", nullable = true, length = 32)
  private String productCaseIdentifier;
  @Column(name = "customer_account_identifier", nullable = false, length = 32)
  private String customerAccountIdentifier;
  @Column(name = "target_account_identifier", nullable = true, length = 32)
  private String targetAccountIdentifier;
  @Column(name = "clerk", nullable = false, length = 32)
  private String clerk;
  @Column(name = "amount", nullable = false)
  private BigDecimal amount;
  @Column(name = "a_state", nullable = false, length = 256)
  private String state;

  public TellerTransactionEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public TellerEntity getTeller() {
    return this.teller;
  }

  public void setTeller(final TellerEntity teller) {
    this.teller = teller;
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

  public LocalDateTime getTransactionDate() {
    return this.transactionDate;
  }

  public void setTransactionDate(final LocalDateTime transactionDate) {
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
    return this.state;
  }

  public void setState(final String state) {
    this.state = state;
  }
}

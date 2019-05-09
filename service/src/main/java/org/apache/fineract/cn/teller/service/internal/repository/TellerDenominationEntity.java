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
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.cn.postgresql.util.LocalDateTimeConverter;

@Entity
@Table(name = "tajet_teller_denominations")
public class TellerDenominationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @ManyToOne
  @JoinColumn(name = "teller_id")
  private TellerEntity teller;
  @Column(name = "counted_total", nullable = false)
  private BigDecimal countedTotal;
  @Column(name = "note", nullable = true)
  private String note;
  @Column(name = "adjusting_journal_entry", nullable = true)
  private String adjustingJournalEntry;
  @Convert(converter = LocalDateTimeConverter.class)
  @Column(name = "created_on", nullable = false)
  private LocalDateTime createdOn;
  @Column(name = "created_by")
  private String createdBy;

  public TellerDenominationEntity() {
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

  public BigDecimal getCountedTotal() {
    return this.countedTotal;
  }

  public void setCountedTotal(final BigDecimal countedTotal) {
    this.countedTotal = countedTotal;
  }

  public String getNote() {
    return this.note;
  }

  public void setNote(final String note) {
    this.note = note;
  }

  public String getAdjustingJournalEntry() {
    return this.adjustingJournalEntry;
  }

  public void setAdjustingJournalEntry(final String adjustingJournalEntry) {
    this.adjustingJournalEntry = adjustingJournalEntry;
  }

  public LocalDateTime getCreatedOn() {
    return this.createdOn;
  }

  public void setCreatedOn(final LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }
}

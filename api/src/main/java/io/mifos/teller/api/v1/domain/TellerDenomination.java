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

import io.mifos.core.lang.validation.constraints.ValidIdentifier;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TellerDenomination {

  @NotNull
  @Range(min = -9999999999L, max = 9999999999L)
  private BigDecimal countedTotal;
  private String note;
  @ValidIdentifier(optional = true)
  private String adjustingJournalEntry;
  private String createdOn;
  private String createdBy;

  public TellerDenomination() {
    super();
  }

  public String getCreatedOn() {
    return this.createdOn;
  }

  public void setCreatedOn(final String createdOn) {
    this.createdOn = createdOn;
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

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }
}

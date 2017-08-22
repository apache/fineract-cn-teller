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

public class MICR {
  @ValidIdentifier(maxLength = 8)
  private String chequeNumber;
  @ValidIdentifier(maxLength = 11)
  private String branchSortCode;
  @ValidIdentifier(maxLength = 34)
  private String accountNumber;

  public MICR() {
    super();
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
}

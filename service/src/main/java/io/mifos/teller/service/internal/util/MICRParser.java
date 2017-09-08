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
package io.mifos.teller.service.internal.util;

import io.mifos.teller.api.v1.domain.MICR;

public class MICRParser {
  private static final String DELIMITER = "~";

  private MICRParser() {
    super();
  }

  public static String toIdentifier(final MICR micr) {
    if (micr == null
        || micr.getChequeNumber() == null || micr.getChequeNumber().isEmpty()
        || micr.getBranchSortCode() == null || micr.getBranchSortCode().isEmpty()
        || micr.getAccountNumber() == null || micr.getAccountNumber().isEmpty()) {
      throw new IllegalArgumentException("MICR must be given and all values need to be set.");
    }

    return micr.getChequeNumber()
        + MICRParser.DELIMITER
        + micr.getBranchSortCode()
        + MICRParser.DELIMITER
        + micr.getAccountNumber();
  }
}

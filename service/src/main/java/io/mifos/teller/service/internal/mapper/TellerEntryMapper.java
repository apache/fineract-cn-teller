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
package io.mifos.teller.service.internal.mapper;

import io.mifos.teller.api.v1.domain.TellerEntry;
import java.math.BigDecimal;
import org.apache.fineract.cn.accounting.api.v1.domain.AccountEntry;

public class TellerEntryMapper {

  private TellerEntryMapper() {
    super();
  }

  public static TellerEntry map(final AccountEntry accountEntry) {
    final TellerEntry tellerEntry = new TellerEntry();
    tellerEntry.setType(TellerEntry.Type.valueOf(accountEntry.getType()).name());
    tellerEntry.setTransactionDate(accountEntry.getTransactionDate());
    tellerEntry.setMessage(accountEntry.getMessage());
    tellerEntry.setAmount(BigDecimal.valueOf(accountEntry.getAmount()));

    return tellerEntry;
  }
}

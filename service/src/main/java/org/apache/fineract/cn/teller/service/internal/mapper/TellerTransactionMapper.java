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
package org.apache.fineract.cn.teller.service.internal.mapper;

import org.apache.fineract.cn.teller.api.v1.domain.TellerTransaction;
import org.apache.fineract.cn.teller.service.internal.repository.TellerTransactionEntity;
import org.apache.fineract.cn.lang.DateConverter;

public class TellerTransactionMapper {

  private TellerTransactionMapper() {
    super();
  }

  public static TellerTransaction map(final TellerTransactionEntity tellerTransactionEntity) {
    final TellerTransaction tellerTransaction = new TellerTransaction();
    tellerTransaction.setIdentifier(tellerTransactionEntity.getIdentifier());
    tellerTransaction.setTransactionType(tellerTransactionEntity.getTransactionType());
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(tellerTransactionEntity.getTransactionDate()));
    tellerTransaction.setCustomerIdentifier(tellerTransactionEntity.getCustomerIdentifier());
    tellerTransaction.setProductIdentifier(tellerTransactionEntity.getProductIdentifier());
    tellerTransaction.setProductCaseIdentifier(tellerTransactionEntity.getProductCaseIdentifier());
    tellerTransaction.setCustomerAccountIdentifier(tellerTransactionEntity.getCustomerAccountIdentifier());
    tellerTransaction.setTargetAccountIdentifier(tellerTransactionEntity.getTargetAccountIdentifier());
    tellerTransaction.setAmount(tellerTransactionEntity.getAmount());
    tellerTransaction.setClerk(tellerTransactionEntity.getClerk());
    tellerTransaction.setState(tellerTransactionEntity.getState());

    return tellerTransaction;
  }

  public static TellerTransactionEntity map(final TellerTransaction tellerTransaction) {
    final TellerTransactionEntity tellerTransactionEntity = new TellerTransactionEntity();
    tellerTransactionEntity.setIdentifier(tellerTransaction.getIdentifier());
    tellerTransactionEntity.setTransactionType(tellerTransaction.getTransactionType());
    tellerTransactionEntity.setTransactionDate(DateConverter.fromIsoString(tellerTransaction.getTransactionDate()));
    tellerTransactionEntity.setCustomerIdentifier(tellerTransaction.getCustomerIdentifier());
    tellerTransactionEntity.setProductIdentifier(tellerTransaction.getProductIdentifier());
    tellerTransactionEntity.setProductCaseIdentifier(tellerTransaction.getProductCaseIdentifier());
    tellerTransactionEntity.setCustomerAccountIdentifier(tellerTransaction.getCustomerAccountIdentifier());
    tellerTransactionEntity.setTargetAccountIdentifier(tellerTransaction.getTargetAccountIdentifier());
    tellerTransactionEntity.setAmount(tellerTransaction.getAmount());
    tellerTransactionEntity.setClerk(tellerTransaction.getClerk());
    tellerTransactionEntity.setState(tellerTransaction.getState());

    return tellerTransactionEntity;
  }
}

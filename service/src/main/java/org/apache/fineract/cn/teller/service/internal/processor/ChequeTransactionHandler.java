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
package org.apache.fineract.cn.teller.service.internal.processor;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.teller.api.v1.domain.TellerTransaction;
import org.apache.fineract.cn.teller.service.internal.mapper.ChequeMapper;
import org.apache.fineract.cn.teller.service.internal.repository.TellerEntity;
import org.apache.fineract.cn.teller.service.internal.repository.TellerRepository;
import org.apache.fineract.cn.teller.service.internal.service.helper.AccountingService;
import org.apache.fineract.cn.teller.service.internal.service.helper.ChequeService;
import org.apache.fineract.cn.teller.service.internal.service.helper.DepositAccountManagementService;
import java.util.Optional;
import org.apache.fineract.cn.cheque.api.v1.domain.ChequeTransaction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ChequeTransactionHandler {

  private final Logger logger;
  private final ChequeService chequeService;
  private final TellerRepository tellerRepository;
  private final AccountingService accountingService;
  private final DepositAccountManagementService depositAccountManagementService;

  @Autowired
  public ChequeTransactionHandler(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                  final ChequeService chequeService,
                                  final TellerRepository tellerRepository,
                                  final AccountingService accountingService,
                                  final DepositAccountManagementService depositAccountManagementService) {
    super();
    this.logger = logger;this.chequeService = chequeService;
    this.tellerRepository = tellerRepository;
    this.accountingService = accountingService;
    this.depositAccountManagementService = depositAccountManagementService;
  }

  public void processCheque(final String tellerCode, final TellerTransaction tellerTransaction) {
    final Optional<TellerEntity> optionalTeller = this.tellerRepository.findByIdentifier(tellerCode);
    final ChequeTransaction chequeTransaction = new ChequeTransaction();
    optionalTeller.ifPresent(tellerEntity ->
        chequeTransaction.setChequesReceivableAccount(tellerEntity.getChequesReceivableAccount()));
    chequeTransaction.setCreditorAccountNumber(
        this.accountingService.resolveAccountIdentifier(tellerTransaction.getCustomerAccountIdentifier())
    );
    chequeTransaction.setCheque(ChequeMapper.map(tellerTransaction.getCheque()));

    this.chequeService.process(chequeTransaction);
    this.depositAccountManagementService.transactedProductInstance(tellerTransaction.getCustomerAccountIdentifier());
  }
}

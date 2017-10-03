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
package io.mifos.teller.service.internal.processor;

import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.service.internal.mapper.ChequeMapper;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import io.mifos.teller.service.internal.service.helper.ChequeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ChequeTransactionHandler {

  private final Logger logger;
  private final ChequeService chequeService;
  private final TellerRepository tellerRepository;
  private final AccountingService accountingService;

  @Autowired
  public ChequeTransactionHandler(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                  final ChequeService chequeService,
                                  final TellerRepository tellerRepository,
                                  final AccountingService accountingService) {
    super();
    this.logger = logger;this.chequeService = chequeService;
    this.tellerRepository = tellerRepository;
    this.accountingService = accountingService;
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
  }
}

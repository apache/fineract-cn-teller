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
package org.apache.fineract.cn.teller.service.internal.command.handler;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.teller.api.v1.EventConstants;
import org.apache.fineract.cn.teller.api.v1.domain.Cheque;
import org.apache.fineract.cn.teller.api.v1.domain.TellerTransaction;
import org.apache.fineract.cn.teller.api.v1.domain.TellerTransactionCosts;
import org.apache.fineract.cn.teller.service.internal.command.CancelTellerTransactionCommand;
import org.apache.fineract.cn.teller.service.internal.command.ConfirmTellerTransactionCommand;
import org.apache.fineract.cn.teller.service.internal.command.InitializeTellerTransactionCommand;
import org.apache.fineract.cn.teller.service.internal.mapper.ChequeMapper;
import org.apache.fineract.cn.teller.service.internal.mapper.TellerTransactionMapper;
import org.apache.fineract.cn.teller.service.internal.processor.TellerTransactionProcessor;
import org.apache.fineract.cn.teller.service.internal.repository.ChequeEntity;
import org.apache.fineract.cn.teller.service.internal.repository.ChequeRepository;
import org.apache.fineract.cn.teller.service.internal.repository.TellerEntity;
import org.apache.fineract.cn.teller.service.internal.repository.TellerRepository;
import org.apache.fineract.cn.teller.service.internal.repository.TellerTransactionEntity;
import org.apache.fineract.cn.teller.service.internal.repository.TellerTransactionRepository;
import java.sql.Date;
import java.util.Optional;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.lang.DateConverter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

@Aggregate
public class TellerTransactionAggregate {

  private final Logger logger;
  private final TellerTransactionRepository tellerTransactionRepository;
  private final TellerTransactionProcessor tellerTransactionProcessor;
  private final TellerRepository tellerRepository;
  private final ChequeRepository chequeRepository;

  @Autowired
  public TellerTransactionAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                    final TellerTransactionRepository tellerTransactionRepository,
                                    final TellerTransactionProcessor tellerTransactionProcessor,
                                    final TellerRepository tellerRepository,
                                    final ChequeRepository chequeRepository) {
    super();
    this.logger = logger;
    this.tellerTransactionRepository = tellerTransactionRepository;
    this.tellerTransactionProcessor = tellerTransactionProcessor;
    this.tellerRepository = tellerRepository;
    this.chequeRepository = chequeRepository;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.INIT_TRANSACTION)
  public TellerTransactionCosts process(final InitializeTellerTransactionCommand initializeTellerTransactionCommand) {
    final String tellerCode = initializeTellerTransactionCommand.tellerCode();
    final TellerTransaction tellerTransaction = initializeTellerTransactionCommand.tellerTransaction();

    final Optional<TellerEntity> optionalTeller = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTeller.isPresent()) {
      tellerTransaction.setIdentifier(RandomStringUtils.randomAlphanumeric(32));
      tellerTransaction.setState(TellerTransaction.State.PENDING.name());
      final TellerTransactionEntity tellerTransactionEntity = TellerTransactionMapper.map(tellerTransaction);
      tellerTransactionEntity.setTeller(optionalTeller.get());
      final TellerTransactionEntity savedTellerTransaction = this.tellerTransactionRepository.save(tellerTransactionEntity);

      if (tellerTransaction.getTransactionType().equals(ServiceConstants.TX_CHEQUE)) {
        final Cheque cheque = tellerTransaction.getCheque();
        final ChequeEntity chequeEntity = new ChequeEntity();
        chequeEntity.setTellerTransactionId(savedTellerTransaction.getId());
        chequeEntity.setChequeNumber(cheque.getMicr().getChequeNumber());
        chequeEntity.setBranchSortCode(cheque.getMicr().getBranchSortCode());
        chequeEntity.setAccountNumber(cheque.getMicr().getAccountNumber());
        chequeEntity.setDrawee(cheque.getDrawee());
        chequeEntity.setDrawer(cheque.getDrawer());
        chequeEntity.setPayee(cheque.getPayee());
        chequeEntity.setDateIssued(Date.valueOf(DateConverter.dateFromIsoString(cheque.getDateIssued())));
        chequeEntity.setAmount(cheque.getAmount());
        chequeEntity.setOpenCheque(cheque.isOpenCheque());
        this.chequeRepository.save(chequeEntity);
      }

      return this.tellerTransactionProcessor.getCosts(tellerTransaction);
    } else {
      this.logger.warn("Teller {} not found.", tellerCode);
    }
    return null;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.CONFIRM_TRANSACTION)
  public String process(final ConfirmTellerTransactionCommand confirmTellerTransactionCommand) {
    final Optional<TellerTransactionEntity> optionalTellerTransaction =
        this.tellerTransactionRepository.findByIdentifier(confirmTellerTransactionCommand.tellerTransactionIdentifier());

    if (optionalTellerTransaction.isPresent()) {
      final TellerTransactionEntity tellerTransactionEntity = optionalTellerTransaction.get();
      final TellerTransaction tellerTransaction = TellerTransactionMapper.map(tellerTransactionEntity);

      if (tellerTransactionEntity.getTransactionType().equals(ServiceConstants.TX_CHEQUE)) {
        final Optional<ChequeEntity> optionalCheque =
            this.chequeRepository.findByTellerTransactionId(tellerTransactionEntity.getId());

        optionalCheque.ifPresent(chequeEntity -> tellerTransaction.setCheque(ChequeMapper.map(chequeEntity)));
      }

      this.tellerTransactionProcessor.process(tellerTransactionEntity.getTeller().getIdentifier(),
          tellerTransaction, confirmTellerTransactionCommand.chargesIncluded());

      tellerTransactionEntity.setState(TellerTransaction.State.CONFIRMED.name());
      this.tellerTransactionRepository.save(tellerTransactionEntity);

      return confirmTellerTransactionCommand.tellerTransactionIdentifier();
    } else {
      this.logger.warn("Teller transaction {} not found", confirmTellerTransactionCommand.tellerTransactionIdentifier());
    }
    return null;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.CANCEL_TRANSACTION)
  public String process(final CancelTellerTransactionCommand cancelTellerTransactionCommand) {
    final Optional<TellerTransactionEntity> optionalTellerTransaction =
        this.tellerTransactionRepository.findByIdentifier(cancelTellerTransactionCommand.tellerTransactionIdentifier());

    if (optionalTellerTransaction.isPresent()) {
      final TellerTransactionEntity tellerTransactionEntity = optionalTellerTransaction.get();
      tellerTransactionEntity.setState(TellerTransaction.State.CANCELED.name());
      this.tellerTransactionRepository.save(tellerTransactionEntity);

      return cancelTellerTransactionCommand.tellerTransactionIdentifier();
    } else {
      this.logger.warn("Teller transaction {} not found", cancelTellerTransactionCommand.tellerTransactionIdentifier());
    }
    return null;
  }
}

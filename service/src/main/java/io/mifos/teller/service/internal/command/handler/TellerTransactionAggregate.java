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
package io.mifos.teller.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import io.mifos.teller.service.internal.command.CancelTellerTransactionCommand;
import io.mifos.teller.service.internal.command.ConfirmTellerTransactionCommand;
import io.mifos.teller.service.internal.command.InitializeTellerTransactionCommand;
import io.mifos.teller.service.internal.mapper.TellerTransactionMapper;
import io.mifos.teller.service.internal.processor.TellerTransactionProcessor;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.repository.TellerTransactionEntity;
import io.mifos.teller.service.internal.repository.TellerTransactionRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Aggregate
public class TellerTransactionAggregate {

  private final Logger logger;
  private final TellerTransactionRepository tellerTransactionRepository;
  private final TellerTransactionProcessor tellerTransactionProcessor;
  private final TellerRepository tellerRepository;

  @Autowired
  public TellerTransactionAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                    final TellerTransactionRepository tellerTransactionRepository,
                                    final TellerTransactionProcessor tellerTransactionProcessor,
                                    final TellerRepository tellerRepository) {
    super();
    this.logger = logger;
    this.tellerTransactionRepository = tellerTransactionRepository;
    this.tellerTransactionProcessor = tellerTransactionProcessor;
    this.tellerRepository = tellerRepository;
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
      this.tellerTransactionRepository.save(tellerTransactionEntity);
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

      this.tellerTransactionProcessor.process(tellerTransactionEntity.getTeller().getIdentifier(),
          TellerTransactionMapper.map(tellerTransactionEntity),
          confirmTellerTransactionCommand.chargesIncluded());

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

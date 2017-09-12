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

import com.google.common.collect.Sets;
import io.mifos.accounting.api.v1.domain.Creditor;
import io.mifos.accounting.api.v1.domain.Debtor;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.DateConverter;
import io.mifos.core.lang.ServiceException;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerManagementCommand;
import io.mifos.teller.api.v1.domain.UnlockDrawerCommand;
import io.mifos.teller.service.internal.command.ChangeTellerCommand;
import io.mifos.teller.service.internal.command.CloseTellerCommand;
import io.mifos.teller.service.internal.command.CreateTellerCommand;
import io.mifos.teller.service.internal.command.DrawerUnlockCommand;
import io.mifos.teller.service.internal.command.OpenTellerCommand;
import io.mifos.teller.service.internal.command.PauseTellerCommand;
import io.mifos.teller.service.internal.mapper.TellerMapper;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import io.mifos.teller.service.internal.service.helper.OrganizationService;
import io.mifos.tool.crypto.HashGenerator;
import io.mifos.tool.crypto.SaltGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Aggregate
public class TellerAggregate {

  private final Logger logger;
  private final TellerRepository tellerRepository;
  private final OrganizationService organizationService;
  private final AccountingService accountingService;
  private final HashGenerator hashGenerator;
  private final SaltGenerator saltGenerator;

  @Autowired
  public TellerAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                         final TellerRepository tellerRepository,
                         final OrganizationService organizationService,
                         final AccountingService accountingService,
                         final HashGenerator hashGenerator,
                         final SaltGenerator saltGenerator) {
    super();
    this.logger = logger;
    this.tellerRepository = tellerRepository;
    this.organizationService = organizationService;
    this.accountingService = accountingService;
    this.hashGenerator = hashGenerator;
    this.saltGenerator = saltGenerator;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.POST_TELLER)
  public String process(final CreateTellerCommand createTellerCommand) {
    final String officeIdentifier = createTellerCommand.officeIdentifier();
    final Teller teller = createTellerCommand.teller();

    if (this.checkPreconditions(officeIdentifier, teller)) {

      final TellerEntity tellerEntity = TellerMapper.map(officeIdentifier, teller);

      this.encryptPassword(teller, tellerEntity);

      tellerEntity.setState(Teller.State.CLOSED.name());
      tellerEntity.setCreatedBy(UserContextHolder.checkedGetUser());
      tellerEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));

      this.tellerRepository.save(tellerEntity);

      return teller.getCode();
    } else {
      throw new IllegalStateException("Preconditions not met, see log file for further information.");
    }
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.PUT_TELLER)
  public String process(final ChangeTellerCommand changeTellerCommand) {
    final String officeIdentifier = changeTellerCommand.officeIdentifier();
    final Teller teller = changeTellerCommand.teller();

    if (this.checkPreconditions(officeIdentifier, teller)) {
      final Optional<TellerEntity> optionalTellerEntity = this.tellerRepository.findByIdentifier(teller.getCode());
      if (optionalTellerEntity.isPresent()) {
        final TellerEntity tellerEntity = optionalTellerEntity.get();

        if (teller.getPassword() != null) {
          this.encryptPassword(teller, tellerEntity);
        }

        tellerEntity.setTellerAccountIdentifier(teller.getTellerAccountIdentifier());
        tellerEntity.setVaultAccountIdentifier(teller.getVaultAccountIdentifier());
        tellerEntity.setChequesReceivableAccount(teller.getChequesReceivableAccount());
        tellerEntity.setCashdrawLimit(teller.getCashdrawLimit());
        tellerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
        tellerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

        this.tellerRepository.save(tellerEntity);

        return teller.getCode();
      } else {
        throw new IllegalStateException("Teller " + teller.getCode() + " not found.");
      }
    } else {
      throw new IllegalStateException("Preconditions not met, see log file for further information.");
    }
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.OPEN_TELLER)
  public String process(final OpenTellerCommand openTellerCommand) {
    final String tellerCode = openTellerCommand.tellerCode();
    final TellerManagementCommand tellerManagementCommand = openTellerCommand.tellerManagementCommand();

    final Optional<TellerEntity> optionalTellerEntity = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTellerEntity.isPresent()) {
      final TellerEntity tellerEntity = optionalTellerEntity.get();

      if (this.checkPreconditions(tellerEntity.getOfficeIdentifier(), TellerMapper.map(tellerEntity))) {

        if (!tellerManagementCommand.getAdjustment().equals(TellerManagementCommand.Adjustment.NONE.name())
            && tellerManagementCommand.getAmount() != null
            && tellerManagementCommand.getAmount().compareTo(BigDecimal.ZERO) > 0) {
          this.accountingService.postJournalEntry(this.createJournalEntry(tellerEntity, tellerManagementCommand));
        }

        tellerEntity.setAssignedEmployeeIdentifier(tellerManagementCommand.getAssignedEmployeeIdentifier());
        tellerEntity.setState(Teller.State.OPEN.name());
        tellerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
        tellerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

        this.tellerRepository.save(tellerEntity);
        return tellerCode;
      } else {
        throw new IllegalStateException("Preconditions not met, see log file for further information.");
      }
    } else {
      throw new IllegalStateException("Teller " + tellerCode + " not found.");
    }
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.CLOSE_TELLER)
  public String process(final CloseTellerCommand closeTellerCommand) {
    final String tellerCode = closeTellerCommand.tellerCode();
    final TellerManagementCommand tellerManagementCommand = closeTellerCommand.tellerManagementCommand();

    final Optional<TellerEntity> optionalTellerEntity = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTellerEntity.isPresent()) {
      final TellerEntity tellerEntity = optionalTellerEntity.get();

      if (this.checkPreconditions(tellerEntity.getOfficeIdentifier(), TellerMapper.map(tellerEntity))) {

        if (!tellerManagementCommand.getAdjustment().equals(TellerManagementCommand.Adjustment.NONE.name())
            && tellerManagementCommand.getAmount() != null
            && tellerManagementCommand.getAmount().compareTo(BigDecimal.ZERO) > 0) {
          this.accountingService.postJournalEntry(this.createJournalEntry(tellerEntity, tellerManagementCommand));
        }
        tellerEntity.setAssignedEmployeeIdentifier(null);
        tellerEntity.setState(Teller.State.CLOSED.name());
        tellerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
        tellerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

        this.tellerRepository.save(tellerEntity);
        return tellerCode;
      } else {
        throw new IllegalStateException("Preconditions not met, see log file for further information.");
      }
    } else {
      throw new IllegalStateException("Teller " + tellerCode + " not found.");
    }
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.AUTHENTICATE_TELLER)
  public String process(final DrawerUnlockCommand drawerUnlockCommand) {
    final String tellerCode = drawerUnlockCommand.tellerCode();
    final UnlockDrawerCommand unlockDrawerCommand = drawerUnlockCommand.tellerAuthentication();

    final Optional<TellerEntity> optionalTeller = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTeller.isPresent()) {
      final TellerEntity tellerEntity = optionalTeller.get();
      if (tellerEntity.getState().equals(Teller.State.CLOSED.name())) {
        throw ServiceException.notFound("Teller {0} not found.", tellerCode);
      }

      if (!UserContextHolder.checkedGetUser().equals(tellerEntity.getAssignedEmployeeIdentifier())) {
        throw ServiceException.notFound("Teller {0} not found.", tellerCode);
      }

      final byte[] givenPassword = this.hashGenerator.hash(unlockDrawerCommand.getPassword(),
          Base64Utils.decodeFromString(tellerEntity.getSalt()), ServiceConstants.ITERATION_COUNT, ServiceConstants.LENGTH);

      if (!tellerEntity.getPassword().equals(Base64Utils.encodeToString(givenPassword))) {
        throw ServiceException.notFound("Teller {0} not found.", tellerCode);
      }

      tellerEntity.setState(Teller.State.ACTIVE.name());
      tellerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
      tellerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));
      this.tellerRepository.save(tellerEntity);

      return tellerCode;
    } else {
      throw ServiceException.notFound("Teller {0} not found.", tellerCode);
    }
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = EventConstants.SELECTOR_NAME, selectorValue = EventConstants.PAUSE_TELLER)
  public String process(final PauseTellerCommand pauseTellerCommand) {
    final String tellerCode = pauseTellerCommand.tellerCode();

    final Optional<TellerEntity> optionalTeller = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTeller.isPresent()) {
      final TellerEntity tellerEntity = optionalTeller.get();
      if (UserContextHolder.checkedGetUser().equals(tellerEntity.getAssignedEmployeeIdentifier())
          && tellerEntity.getState().equals(Teller.State.ACTIVE.name())) {
        tellerEntity.setState(Teller.State.PAUSED.name());
        tellerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
        tellerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));
        this.tellerRepository.save(tellerEntity);

        return tellerCode;
      } else {
        this.logger.warn("Unable to pause teller {}.", tellerCode);
      }
    } else {
      this.logger.warn("Teller {} not found.", tellerCode);
    }
    return null;
  }

  private boolean checkPreconditions(final String officeIdentifier, final Teller teller) {
    boolean pass = true;

    if (!this.organizationService.officeExists(officeIdentifier)) {
      this.logger.warn("Office {} not found.", officeIdentifier);
      pass = false;
    }

    if (!this.accountingService.findAccount(teller.getTellerAccountIdentifier()).isPresent()) {
      this.logger.warn("Teller account {} not found.", teller.getTellerAccountIdentifier());
      pass = false;
    }

    if (!this.accountingService.findAccount(teller.getVaultAccountIdentifier()).isPresent()) {
      this.logger.warn("Vault account {} not found.", teller.getVaultAccountIdentifier());
      pass = false;
    }

    return pass;
  }

  private void encryptPassword(final Teller teller, final TellerEntity tellerEntity) {
    final byte[] salt = this.saltGenerator.createRandomSalt();
    final byte[] newPassword = this.hashGenerator.hash(teller.getPassword(), salt, ServiceConstants.ITERATION_COUNT,
        ServiceConstants.LENGTH);
    tellerEntity.setSalt(Base64Utils.encodeToString(salt));
    tellerEntity.setPassword(Base64Utils.encodeToString(newPassword));
  }

  private JournalEntry createJournalEntry(final TellerEntity tellerEntity, final TellerManagementCommand tellerManagementCommand) {
    final JournalEntry journalEntry = new JournalEntry();
    journalEntry.setTransactionIdentifier(RandomStringUtils.randomNumeric(32));
    journalEntry.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    journalEntry.setClerk(UserContextHolder.checkedGetUser());
    journalEntry.setMessage("Teller adjustment.");

    final TellerManagementCommand.Adjustment adjustment =
        TellerManagementCommand.Adjustment.valueOf(tellerManagementCommand.getAdjustment());

    final Debtor debtor = new Debtor();
    final Creditor creditor = new Creditor();
    switch (adjustment) {
      case DEBIT:
        journalEntry.setTransactionType(ServiceConstants.TX_DEPOSIT_ADJUSTMENT);

        debtor.setAccountNumber(tellerEntity.getTellerAccountIdentifier());
        debtor.setAmount(tellerManagementCommand.getAmount().toString());
        journalEntry.setDebtors(Sets.newHashSet(debtor));

        creditor.setAccountNumber(tellerEntity.getVaultAccountIdentifier());
        creditor.setAmount(tellerManagementCommand.getAmount().toString());
        journalEntry.setCreditors(Sets.newHashSet(creditor));

        break;
      case CREDIT:
        journalEntry.setTransactionType(ServiceConstants.TX_CREDIT_ADJUSTMENT);

        debtor.setAccountNumber(tellerEntity.getVaultAccountIdentifier());
        debtor.setAmount(tellerManagementCommand.getAmount().toString());
        journalEntry.setDebtors(Sets.newHashSet(debtor));

        creditor.setAccountNumber(tellerEntity.getTellerAccountIdentifier());
        creditor.setAmount(tellerManagementCommand.getAmount().toString());
        journalEntry.setCreditors(Sets.newHashSet(creditor));

        break;
      default:
        this.logger.warn("Unsupported adjustment type {}.", tellerManagementCommand.getAdjustment());
    }

    return journalEntry;
  }
}


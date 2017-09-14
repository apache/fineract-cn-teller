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
package io.mifos.teller.service.rest;

import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.DateConverter;
import io.mifos.core.lang.ServiceException;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.PermittableGroupIds;
import io.mifos.teller.api.v1.domain.MICR;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import io.mifos.teller.api.v1.domain.UnlockDrawerCommand;
import io.mifos.teller.service.internal.command.CancelTellerTransactionCommand;
import io.mifos.teller.service.internal.command.ConfirmTellerTransactionCommand;
import io.mifos.teller.service.internal.command.DrawerUnlockCommand;
import io.mifos.teller.service.internal.command.InitializeTellerTransactionCommand;
import io.mifos.teller.service.internal.command.PauseTellerCommand;
import io.mifos.teller.service.internal.processor.TellerTransactionProcessor;
import io.mifos.teller.service.internal.service.TellerManagementService;
import io.mifos.teller.service.internal.service.TellerOperationService;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import io.mifos.teller.service.internal.service.helper.ChequeService;
import io.mifos.teller.service.internal.util.MICRParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teller/{tellerCode}")
public class TellerOperationRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final TellerOperationService tellerOperationService;
  private final TellerManagementService tellerManagementService;
  private final AccountingService accountingService;
  private final ChequeService chequeService;
  private final TellerTransactionProcessor tellerTransactionProcessor;

  @Autowired
  public TellerOperationRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                       final CommandGateway commandGateway,
                                       final TellerOperationService tellerOperationService,
                                       final TellerManagementService tellerManagementService,
                                       final AccountingService accountingService,
                                       final ChequeService chequeService,
                                       final TellerTransactionProcessor tellerTransactionProcessor) {
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.tellerOperationService = tellerOperationService;
    this.tellerManagementService = tellerManagementService;
    this.accountingService = accountingService;
    this.chequeService = chequeService;
    this.tellerTransactionProcessor = tellerTransactionProcessor;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "/drawer",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Teller> unlockDrawer(@PathVariable("tellerCode") final String tellerCode,
                                      @RequestBody @Valid final UnlockDrawerCommand unlockDrawerCommand) {
    final Teller teller = this.verifyTeller(tellerCode);

    if (teller.getState().equals(Teller.State.CLOSED.name())) {
      throw ServiceException.badRequest("Teller {0} is closed.", teller.getCode());
    }

    if (!teller.getAssignedEmployee().equals(unlockDrawerCommand.getEmployeeIdentifier())) {
      throw ServiceException.badRequest("User {0} is not assigned to teller.", unlockDrawerCommand.getEmployeeIdentifier());
    }

    this.verifyEmployee(teller);

    try {
      final String unlockedTeller =
          this.commandGateway.process(new DrawerUnlockCommand(tellerCode, unlockDrawerCommand), String.class).get();

      this.logger.debug("Drawer {0} unlocked", unlockedTeller);

      return ResponseEntity.ok(teller);
    } catch (final Exception e) {
      throw ServiceException.notFound("Teller {0} not found.", teller.getCode());
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> post(@PathVariable("tellerCode") final String tellerCode,
                            @RequestParam(value = "command", required = true) final String command) {

    final Teller teller = this.verifyTeller(tellerCode);

    this.verifyEmployee(teller);

    switch (command.toUpperCase()) {
      case "PAUSE":
        if (!teller.getState().equals(Teller.State.ACTIVE.name())) {
          throw ServiceException.badRequest("Teller {0} is not active.", tellerCode);
        }

        this.commandGateway.process(new PauseTellerCommand(tellerCode));
        break;
      default :
        throw ServiceException.badRequest("Unknonw command {0}", command);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "/transactions",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<TellerTransactionCosts> post(@PathVariable("tellerCode") final String tellerCode,
                                              @RequestBody @Valid final TellerTransaction tellerTransaction) {
    final Teller teller = this.verifyTeller(tellerCode);

    this.verifyEmployee(teller);

    if (!teller.getState().equals(Teller.State.ACTIVE.name())) {
      throw ServiceException.conflict("Teller {0} ist not active.", tellerCode);
    }

    final String transactionType = tellerTransaction.getTransactionType();

    if (transactionType.equals(ServiceConstants.TX_CASH_WITHDRAWAL)
        || transactionType.equals(ServiceConstants.TX_CLOSE_ACCOUNT)) {
      if (tellerTransaction.getAmount().compareTo(teller.getCashdrawLimit()) > 0) {
        throw ServiceException.conflict("Amount exceeds cash drawl limit.");
      }
    }

    this.verifyAccounts(tellerTransaction);

    if (transactionType.equals(ServiceConstants.TX_CHEQUE)) {
      final LocalDate dateIssued = DateConverter.dateFromIsoString(tellerTransaction.getCheque().getDateIssued());
      final LocalDate sixMonth = LocalDate.now(Clock.systemUTC()).minusMonths(6);
      if (dateIssued.isBefore(sixMonth)) {
        throw ServiceException.conflict("Cheque is older than 6 months.");
      }

      final MICR micr = tellerTransaction.getCheque().getMicr();
      final String chequeIdentifier = MICRParser.toIdentifier(micr);
      if (this.chequeService.chequeExists(chequeIdentifier)) {
        throw ServiceException.conflict("Cheque {0} already used.", chequeIdentifier);
      }
    }

    try {
      return ResponseEntity.ok(
          this.commandGateway.process(
              new InitializeTellerTransactionCommand(tellerCode, tellerTransaction), TellerTransactionCosts.class).get()
      );
    } catch (final Exception e) {
      throw ServiceException.badRequest("Transaction for teller {0} not valid.", tellerCode);
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "/transactions/{identifier}",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> confirm(@PathVariable("tellerCode") final String tellerCode,
                               @PathVariable("identifier") final String tellerTransactionIdentifier,
                               @RequestParam(value = "command", required = true) final String command,
                               @RequestParam(value = "charges", required = false, defaultValue = "excluded") final String charges) {
    final Teller teller = this.verifyTeller(tellerCode);

    this.verifyEmployee(teller);

    switch (command.toUpperCase()) {
      case "CONFIRM" :
        final ConfirmTellerTransactionCommand confirmTellerTransactionCommand =
            new ConfirmTellerTransactionCommand(tellerTransactionIdentifier, charges);

        final TellerTransaction tellerTransaction =
            this.tellerOperationService.getTellerTransaction(tellerTransactionIdentifier)
                .orElseThrow(() -> ServiceException.notFound("Transaction {0} not found.", tellerTransactionIdentifier));

        this.verifyAccounts(tellerTransaction);
        this.verifyWithdrawalTransaction(tellerTransactionIdentifier, confirmTellerTransactionCommand);

        this.commandGateway.process(confirmTellerTransactionCommand);
        break;
      case "CANCEL" :
        this.commandGateway.process(new CancelTellerTransactionCommand(tellerTransactionIdentifier));
        break;
      default :
        throw ServiceException.badRequest("Unsupported teller transaction command {0}.", command);
    }

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "/transactions",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<List<TellerTransaction>> fetch(@PathVariable("tellerCode") final String tellerCode,
                                                @RequestParam(value = "status", required = false) final String status) {
    this.verifyTeller(tellerCode);
    return ResponseEntity.ok(
        this.tellerOperationService.fetchTellerTransactions(tellerCode, status)
    );
  }

  private Teller verifyTeller(final String tellerCode) {
    final Optional<Teller> optionalTeller = this.tellerManagementService.findByIdentifier(tellerCode);
    if (!optionalTeller.isPresent()) {
      throw ServiceException.notFound("Teller {0} not found.", tellerCode);
    } else {
      return optionalTeller.get();
    }
  }

  private void verifyEmployee(final Teller teller) {
    final String currentUser = UserContextHolder.checkedGetUser();
    if (!currentUser.equals(teller.getAssignedEmployee())) {
      throw ServiceException.badRequest("User {0} is not assigned to teller {1}", currentUser, teller.getCode());
    }
  }

  private void verifyAccounts(final TellerTransaction tellerTransaction) {
    this.verifyAccount(tellerTransaction.getCustomerAccountIdentifier());

    if (tellerTransaction.getTargetAccountIdentifier() != null) {
      this.verifyAccount(tellerTransaction.getTargetAccountIdentifier());
    }

    if (tellerTransaction.getTransactionType().equals(ServiceConstants.TX_CHEQUE)) {
      final MICR micr = tellerTransaction.getCheque().getMicr();
      this.verifyAccount(micr.getAccountNumber());
    }
  }

  private void verifyAccount(final String accountIdentifier) {
    final Account account = this.accountingService.findAccount(accountIdentifier).orElseThrow(
        () -> ServiceException.conflict("Account {0} not found."));

    if (!account.getState().equals(Account.State.OPEN.name())) {
      throw ServiceException.conflict("Account {0} is not open.", account.getIdentifier());
    }
  }

  private void verifyWithdrawalTransaction(final String tellerTransactionIdentifier,
                                           final ConfirmTellerTransactionCommand confirmTellerTransactionCommand) {

    final TellerTransaction tellerTransaction =
        this.tellerOperationService.getTellerTransaction(tellerTransactionIdentifier)
            .orElseThrow(() -> ServiceException.notFound("Transaction {0} not found.", tellerTransactionIdentifier));

    final String transactionType = tellerTransaction.getTransactionType();

    if (transactionType.equals(ServiceConstants.TX_ACCOUNT_TRANSFER)
        || transactionType.equals(ServiceConstants.TX_CASH_WITHDRAWAL)
        || transactionType.equals(ServiceConstants.TX_CLOSE_ACCOUNT)) {

      final Account account = this.accountingService.findAccount(tellerTransaction.getCustomerAccountIdentifier()).orElseThrow(
          () -> ServiceException.notFound("Customer account {0} not found.", tellerTransaction.getCustomerAccountIdentifier()));
      final BigDecimal currentBalance = BigDecimal.valueOf(account.getBalance());

      final TellerTransactionCosts tellerTransactionCosts =
          this.tellerTransactionProcessor.getCosts(tellerTransaction);
      final BigDecimal transactionAmount = confirmTellerTransactionCommand.chargesIncluded()
          ? tellerTransactionCosts.getTotalAmount()
          : tellerTransaction.getAmount();

      if (transactionAmount.compareTo(currentBalance) > 0) {
        throw ServiceException.conflict("Account has not enough balance.");
      }

      if (transactionType.equals(ServiceConstants.TX_CLOSE_ACCOUNT)) {
        if (currentBalance.compareTo(transactionAmount) > 0) {
          throw ServiceException.conflict("Account has remaining balance.");
        }
      }
    }
  }
}

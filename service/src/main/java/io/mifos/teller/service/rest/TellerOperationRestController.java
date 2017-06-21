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

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.PermittableGroupIds;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.UnlockDrawerCommand;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import io.mifos.teller.service.internal.command.DrawerUnlockCommand;
import io.mifos.teller.service.internal.command.CancelTellerTransactionCommand;
import io.mifos.teller.service.internal.command.ConfirmTellerTransactionCommand;
import io.mifos.teller.service.internal.command.InitializeTellerTransactionCommand;
import io.mifos.teller.service.internal.command.PauseTellerCommand;
import io.mifos.teller.service.internal.service.TellerManagementService;
import io.mifos.teller.service.internal.service.TellerOperationService;
import io.mifos.teller.service.internal.service.helper.AccountingService;
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

  @Autowired
  public TellerOperationRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                       final CommandGateway commandGateway,
                                       final TellerOperationService tellerOperationService,
                                       final TellerManagementService tellerManagementService,
                                       final AccountingService accountingService) {
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.tellerOperationService = tellerOperationService;
    this.tellerManagementService = tellerManagementService;
    this.accountingService = accountingService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "/drawer",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> unlockDrawer(@PathVariable("tellerCode") final String tellerCode,
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

      return ResponseEntity.ok().build();
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

    if (!this.accountingService.accountExists(tellerTransaction.getCustomerAccountIdentifier())) {
      throw ServiceException.badRequest("Customer account {0} not found.");
    }

    if (tellerTransaction.getTargetAccountIdentifier() != null &&
        !this.accountingService.accountExists(tellerTransaction.getTargetAccountIdentifier())) {
      throw ServiceException.badRequest("Target account {0} not found.");
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
                               @RequestParam(value = "command", required = true) final String command) {
    final Teller teller = this.verifyTeller(tellerCode);

    this.verifyEmployee(teller);

    if (!this.tellerOperationService.tellerTransactionExists(tellerTransactionIdentifier)) {
      throw ServiceException.notFound("Transaction {0} not found.", tellerTransactionIdentifier);
    }

    switch (command.toUpperCase()) {
      case "CONFIRM" :
        this.commandGateway.process(new ConfirmTellerTransactionCommand(tellerTransactionIdentifier));
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
}

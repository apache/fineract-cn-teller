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

import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import io.mifos.deposit.api.v1.client.DepositAccountManager;
import io.mifos.portfolio.api.v1.client.PortfolioManager;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.PermittableGroupIds;
import io.mifos.teller.api.v1.domain.TellerAuthentication;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import io.mifos.teller.service.internal.service.TellerManagementService;
import io.mifos.teller.service.internal.service.TellerOperationService;
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
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/teller/{tellerCode}")
public class TellerOperationRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final TellerOperationService tellerOperationService;
  private final TellerManagementService tellerManagementService;
  private final LedgerManager ledgerManager;
  private final DepositAccountManager depositAccountManager;
  private final PortfolioManager portfolioManager;

  @Autowired
  public TellerOperationRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                       final CommandGateway commandGateway,
                                       final TellerOperationService tellerOperationService,
                                       final TellerManagementService tellerManagementService,
                                       final LedgerManager ledgerManager,
                                       final DepositAccountManager depositAccountManager,
                                       final PortfolioManager portfolioManager) {
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.tellerOperationService = tellerOperationService;
    this.tellerManagementService = tellerManagementService;
    this.ledgerManager = ledgerManager;
    this.depositAccountManager = depositAccountManager;
    this.portfolioManager = portfolioManager;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_OPERATION)
  @RequestMapping(
      value = "/auth",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> auth(@PathVariable("tellerCode") final String tellerCode,
                            @RequestBody @Valid final TellerAuthentication tellerAuthentication) {
    return ResponseEntity.accepted().build();
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
    throw ServiceException.notFound("Teller {0} not found.", tellerCode);
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
    return ResponseEntity.ok(Collections.emptyList());
  }
}

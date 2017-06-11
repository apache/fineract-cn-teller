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
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.PermittableGroupIds;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerBalanceSheet;
import io.mifos.teller.api.v1.domain.TellerManagementCommand;
import io.mifos.teller.service.internal.command.ChangeTellerCommand;
import io.mifos.teller.service.internal.command.CloseTellerCommand;
import io.mifos.teller.service.internal.command.CreateTellerCommand;
import io.mifos.teller.service.internal.command.OpenTellerCommand;
import io.mifos.teller.service.internal.service.TellerManagementService;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import io.mifos.teller.service.internal.service.helper.OrganizationService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/offices/{officeIdentifier}/teller")
public class TellerManagementRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final TellerManagementService tellerManagementService;
  private final OrganizationService organizationService;
  private final AccountingService accountingService;

  @Autowired
  public TellerManagementRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                        final CommandGateway commandGateway,
                                        final TellerManagementService tellerManagementService,
                                        final OrganizationService organizationService,
                                        final AccountingService accountingService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.tellerManagementService = tellerManagementService;
    this.organizationService = organizationService;
    this.accountingService = accountingService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> create(@PathVariable("officeIdentifier") final String officeIdentifier,
                              @RequestBody @Valid final Teller teller) {
    if (this.tellerManagementService.findByIdentifier(teller.getCode()).isPresent()) {
      throw ServiceException.conflict("Teller {0} already exists.", teller.getCode());
    }

    this.verifyOffice(officeIdentifier);
    this.verifyAccount(teller.getTellerAccountIdentifier());
    this.verifyAccount(teller.getVaultAccountIdentifier());

    this.commandGateway.process(new CreateTellerCommand(officeIdentifier, teller));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Teller> find(@PathVariable("officeIdentifier") final String officeIdentifier,
                              @PathVariable("tellerCode") final String tellerCode) {
    this.verifyOffice(officeIdentifier);

    return ResponseEntity.ok(
        this.tellerManagementService.findByIdentifier(tellerCode)
            .orElseThrow(() -> ServiceException.notFound("Teller {0} not found.", tellerCode))
    );
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<List<Teller>> fetch(@PathVariable("officeIdentifier") final String officeIdentifier) {
    this.verifyOffice(officeIdentifier);

    return ResponseEntity.ok(
        this.tellerManagementService.findByOfficeIdentifier(officeIdentifier)
    );
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> change(@PathVariable("officeIdentifier") final String officeIdentifier,
                              @PathVariable("tellerCode") final String tellerCode,
                              @RequestBody @Valid final Teller teller) {
    if (!tellerCode.equals(teller.getCode())) {
      throw ServiceException.badRequest("Teller code {0} must much given teller.", tellerCode);
    }

    this.verifyTeller(tellerCode);
    this.verifyOffice(officeIdentifier);
    this.verifyAccount(teller.getTellerAccountIdentifier());
    this.verifyAccount(teller.getVaultAccountIdentifier());

    this.commandGateway.process(new ChangeTellerCommand(officeIdentifier, teller));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}/commands",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<Void> post(@PathVariable("officeIdentifier") final String officeIdentifier,
                            @PathVariable("tellerCode") final String tellerCode,
                            @RequestBody @Valid final TellerManagementCommand tellerManagementCommand) {
    final Teller teller = this.verifyTeller(tellerCode);

    if (tellerManagementCommand.getAmount() != null && tellerManagementCommand.getAmount() > teller.getCashdrawLimit()) {
      throw ServiceException.badRequest("Adjustment exceeds cashdraw limit.");
    }

    final TellerManagementCommand.Action action = TellerManagementCommand.Action.valueOf(tellerManagementCommand.getAction());
    switch (action) {
      case OPEN:
        if (!teller.getState().equals(Teller.State.CLOSED.name())) {
          throw ServiceException.badRequest("Teller {0} is already active.", tellerCode);
        }
        this.verifyEmployee(tellerManagementCommand.getAssignedEmployeeIdentifier());
        this.commandGateway.process(new OpenTellerCommand(tellerCode, tellerManagementCommand));
        break;
      case CLOSE:
        if (teller.getState().equals(Teller.State.CLOSED.name())) {
          throw ServiceException.badRequest("Teller {0} is already closed.", tellerCode);
        }
        this.commandGateway.process(new CloseTellerCommand(tellerCode, tellerManagementCommand));
        break;
      default:
        throw ServiceException.badRequest("Unsupported teller command {0}.", action.name());
    }

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TELLER_MANAGEMENT)
  @RequestMapping(
      value = "/{tellerCode}/balance",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseBody
  ResponseEntity<TellerBalanceSheet> getBalance(@PathVariable("officeIdentifier") final String officeIdentifier,
                                                @PathVariable("tellerCode") final String tellerCode) {
    this.verifyOffice(officeIdentifier);
    this.verifyTeller(tellerCode);

    return ResponseEntity.ok(this.tellerManagementService.getBalance(tellerCode));
  }

  private void verifyAccount(final String accountIdentifier) {
    if (!this.accountingService.accountExists(accountIdentifier)) {
      throw ServiceException.badRequest("Account {0} not found.", accountIdentifier);
    }
  }

  private void verifyEmployee(final String employeeIdentifier) {
    if (!this.organizationService.employeeExists(employeeIdentifier)) {
      throw ServiceException.badRequest("Employee {0} not found.", employeeIdentifier);
    }
  }

  private void verifyOffice(final String officeIdentifier) {
    if (!this.organizationService.officeExists(officeIdentifier)) {
      throw ServiceException.badRequest("Office {0} not found.", officeIdentifier);
    }
  }

  private Teller verifyTeller(final String tellerCode) {
    final Optional<Teller> optionalTeller = this.tellerManagementService.findByIdentifier(tellerCode);
    if (!optionalTeller.isPresent()) {
      throw ServiceException.notFound("Teller {0} not found.", tellerCode);
    }
    return optionalTeller.get();
  }
}

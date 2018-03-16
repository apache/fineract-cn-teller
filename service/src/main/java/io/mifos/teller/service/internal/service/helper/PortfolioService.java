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
package io.mifos.teller.service.internal.service.helper;

import com.google.common.collect.Sets;
import io.mifos.core.api.util.NotFoundException;
import io.mifos.core.lang.DateConverter;
import io.mifos.core.lang.ServiceException;
import io.mifos.individuallending.api.v1.domain.product.AccountDesignators;
import io.mifos.individuallending.api.v1.domain.workflow.Action;
import io.mifos.office.api.v1.client.BadRequestException;
import io.mifos.portfolio.api.v1.client.PortfolioManager;
import io.mifos.portfolio.api.v1.domain.AccountAssignment;
import io.mifos.portfolio.api.v1.domain.ChargeDefinition;
import io.mifos.portfolio.api.v1.domain.Command;
import io.mifos.portfolio.api.v1.domain.CostComponent;
import io.mifos.portfolio.api.v1.domain.Payment;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Charge;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PortfolioService {

  private final Logger logger;
  private final PortfolioManager portfolioManager;

  @Autowired
  public PortfolioService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                          final PortfolioManager portfolioManager) {
    super();
    this.logger = logger;
    this.portfolioManager = portfolioManager;
  }

  public List<Charge> getCharges(final String productIdentifier, final String caseIdentifier, final BigDecimal paymentSize) {

    final List<Charge> charges =  new ArrayList<>();

    try {
      final Payment payment = this.portfolioManager.getCostComponentsForAction(
          productIdentifier, caseIdentifier, Action.ACCEPT_PAYMENT.name(), Sets.newHashSet(), paymentSize);

      final List<CostComponent> costComponents = payment.getCostComponents();

      costComponents.forEach(costComponent -> {
        if (costComponent.getAmount() != null
            && costComponent.getAmount().compareTo(BigDecimal.ZERO) > 0) {
          final Charge charge = new Charge();
          charge.setCode(costComponent.getChargeIdentifier());
          charge.setAmount(costComponent.getAmount());
          try {
            final ChargeDefinition chargeDefinition =
                this.portfolioManager.getChargeDefinition(productIdentifier, costComponent.getChargeIdentifier());
            charge.setName(chargeDefinition.getName());
          } catch (final NotFoundException nfex) {
            this.logger.warn("Charge {} not found.", costComponent.getChargeIdentifier());
            charge.setName(costComponent.getChargeIdentifier());
          }
          charges.add(charge);
        }
      });
    } catch (final NotFoundException |  BadRequestException ex) {
      throw ServiceException.internalError(
          "Could not fetch portfolio information, reason: {0}", ex.getCause() + " - " + ex.getMessage()
      );
    }

    return charges;
  }

  public void processRepayment(final String productIdentifier, final String caseIdentifier, final String tellerAccount,
                               final BigDecimal paymentSize) {

    try {
      final Command repaymentCommand = new Command();
      repaymentCommand.setOneTimeAccountAssignments(this.getAccountAssignments(tellerAccount));
      repaymentCommand.setPaymentSize(paymentSize);
      repaymentCommand.setCreatedOn(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));

      portfolioManager.executeCaseCommand(productIdentifier, caseIdentifier, Action.ACCEPT_PAYMENT.name(),
          repaymentCommand);
    } catch (final NotFoundException |  BadRequestException ex) {
      throw ServiceException.internalError(
          "Could not process repayment, reason: {0}", ex.getCause() + " - " + ex.getMessage()
      );
    }
  }

  private List<AccountAssignment> getAccountAssignments(final String tellerAccount) {
    final AccountAssignment accountAssignment = new AccountAssignment();
    accountAssignment.setAccountIdentifier(tellerAccount);
    accountAssignment.setDesignator(AccountDesignators.ENTRY);
    return Collections.singletonList(accountAssignment);
  }
}

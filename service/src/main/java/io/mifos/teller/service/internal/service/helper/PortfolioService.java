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
package io.mifos.teller.service.internal.service.helper;

import io.mifos.individuallending.api.v1.domain.product.AccountDesignators;
import io.mifos.individuallending.api.v1.domain.workflow.Action;
import io.mifos.portfolio.api.v1.client.PortfolioManager;
import io.mifos.portfolio.api.v1.domain.AccountAssignment;
import io.mifos.portfolio.api.v1.domain.ChargeDefinition;
import io.mifos.portfolio.api.v1.domain.Command;
import io.mifos.portfolio.api.v1.domain.CostComponent;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Charge;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

  public List<Charge> getCharges(final String productIdentifier, final String caseIdentifier) {

    final List<Charge> charges =  new ArrayList<>();

    final List<CostComponent> costComponentsForAction =
        this.portfolioManager.getCostComponentsForAction(productIdentifier, caseIdentifier, Action.ACCEPT_PAYMENT.name());

    costComponentsForAction.forEach(costComponent -> {
      final ChargeDefinition chargeDefinition =
          this.portfolioManager.getChargeDefinition(productIdentifier, costComponent.getChargeIdentifier());

      final Charge charge = new Charge();
      charge.setCode(chargeDefinition.getIdentifier());
      charge.setName(chargeDefinition.getName());
      charge.setAmount(costComponent.getAmount().doubleValue());
      charges.add(charge);
    });

    return charges;
  }

  public void processRepayment(final String productIdentifier, final String caseIdentifier, final String tellerAccount) {

    final List<CostComponent> costComponentsForAction =
        this.portfolioManager.getCostComponentsForAction(productIdentifier, caseIdentifier, Action.ACCEPT_PAYMENT.name());

    final Command repaymentCommand = new Command();
    repaymentCommand.setOneTimeAccountAssignments(this.getAccountAssignments(tellerAccount));
    repaymentCommand.setPaymentSize(BigDecimal.valueOf(
        costComponentsForAction.stream().mapToDouble(value -> value.getAmount().doubleValue()).sum())
    );

    portfolioManager.executeCaseCommand(productIdentifier, caseIdentifier, Action.ACCEPT_PAYMENT.name(), repaymentCommand);
  }

  private List<AccountAssignment> getAccountAssignments(final String tellerAccount) {
    final AccountAssignment accountAssignment = new AccountAssignment();
    accountAssignment.setAccountIdentifier(tellerAccount);
    accountAssignment.setDesignator(AccountDesignators.ENTRY);
    return Collections.singletonList(accountAssignment);
  }
}

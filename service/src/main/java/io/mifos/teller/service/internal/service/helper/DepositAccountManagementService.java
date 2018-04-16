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

import org.apache.fineract.cn.deposit.api.v1.definition.domain.Action;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Charge;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.fineract.cn.deposit.api.v1.EventConstants;
import org.apache.fineract.cn.deposit.api.v1.client.DepositAccountManager;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.ProductDefinition;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.ProductInstance;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DepositAccountManagementService {

  private final Logger logger;
  private final DepositAccountManager depositAccountManager;

  @Autowired
  public DepositAccountManagementService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                         final DepositAccountManager depositAccountManager) {
    super();
    this.logger = logger;
    this.depositAccountManager = depositAccountManager;
  }

  public List<ProductInstance> fetchProductInstances(final String customerIdentifier) {
    return this.depositAccountManager.fetchProductInstances(customerIdentifier);
  }

  public ProductInstance findProductInstance(final String accountIdentifier) {
    return this.depositAccountManager.findProductInstance(accountIdentifier);
  }

  public List<Charge> getCharges(final TellerTransaction tellerTransaction) {
    final List<Charge> charges = new ArrayList<>();
    final ProductDefinition productDefinition =
        this.depositAccountManager.findProductDefinition(tellerTransaction.getProductIdentifier());
    final List<Action> actions = this.depositAccountManager.fetchActions();

    final HashMap<String, Action> mappedActions = new HashMap<>(actions.size());
    actions.forEach(action -> mappedActions.put(action.getIdentifier(), action));

    final MathContext mathContext = new MathContext(2, RoundingMode.HALF_EVEN);
    final Set<org.apache.fineract.cn.deposit.api.v1.definition.domain.Charge> productCharges = productDefinition.getCharges();
    productCharges.forEach(productCharge -> {
      if (productCharge.getAmount() > 0.00D) {
        final Action action = mappedActions.get(productCharge.getActionIdentifier());
        if (action != null
            && action.getTransactionType().equals(tellerTransaction.getTransactionType())) {
          final Charge charge = new Charge();
          charge.setCode(productCharge.getActionIdentifier());
          charge.setIncomeAccountIdentifier(productCharge.getIncomeAccountIdentifier());
          charge.setName(productCharge.getName());
          if (productCharge.getProportional()) {
            charge.setAmount(
                tellerTransaction.getAmount().multiply(
                    BigDecimal.valueOf(productCharge.getAmount()).divide(BigDecimal.valueOf(100.00D), mathContext)
                )
            );
          } else {
            charge.setAmount(BigDecimal.valueOf(productCharge.getAmount()));
          }
          charges.add(charge);
        }
      }
    });
    return charges;
  }

  public void activateProductInstance(final String customerAccountIdentifier) {
    this.depositAccountManager.postProductInstanceCommand(customerAccountIdentifier, EventConstants.ACTIVATE_PRODUCT_INSTANCE_COMMAND);
  }

  public void closeProductInstance(final String customerAccountIdentifier) {
    this.depositAccountManager.postProductInstanceCommand(customerAccountIdentifier, EventConstants.CLOSE_PRODUCT_INSTANCE_COMMAND);
  }

  public void transactedProductInstance(final String customerAccountIdentifier) {
    this.depositAccountManager.postProductInstanceCommand(customerAccountIdentifier, EventConstants.PRODUCT_INSTANCE_TRANSACTION);
  }

  public ProductDefinition findProductDefinition(final String productIdentifier) {
    return this.depositAccountManager.findProductDefinition(productIdentifier);
  }
}

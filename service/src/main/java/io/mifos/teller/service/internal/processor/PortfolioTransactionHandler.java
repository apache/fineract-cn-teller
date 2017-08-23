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
package io.mifos.teller.service.internal.processor;

import io.mifos.core.lang.ServiceException;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Charge;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.service.helper.PortfolioService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class PortfolioTransactionHandler {

  private final Logger logger;
  private final PortfolioService portfolioService;
  private final TellerRepository tellerRepository;

  public PortfolioTransactionHandler(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                     final PortfolioService portfolioService,
                                     final TellerRepository tellerRepository) {
    super();
    this.logger = logger;
    this.portfolioService = portfolioService;
    this.tellerRepository = tellerRepository;
  }

  public TellerTransactionCosts getTellerTransactionCosts(final TellerTransaction tellerTransaction) {
    final List<Charge> charges =
        this.portfolioService.getCharges(
            tellerTransaction.getProductIdentifier(),
            tellerTransaction.getProductCaseIdentifier(),
            tellerTransaction.getAmount()
        );

    final TellerTransactionCosts tellerTransactionCosts = new TellerTransactionCosts();
    tellerTransactionCosts.setCharges(charges);
    tellerTransactionCosts.setTellerTransactionIdentifier(tellerTransaction.getIdentifier());
    tellerTransactionCosts.setTotalAmount(
        BigDecimal.valueOf(
            charges.stream().mapToDouble(value -> value.getAmount().doubleValue()).sum()
        )
    );

    return tellerTransactionCosts;
  }

  public void processRepayment(final String tellerCode, final TellerTransaction tellerTransaction) {
    final Optional<TellerEntity> optionalTellerEntity = this.tellerRepository.findByIdentifier(tellerCode);
    if (optionalTellerEntity.isPresent()) {
      final TellerEntity tellerEntity = optionalTellerEntity.get();

      this.portfolioService.processRepayment(
          tellerTransaction.getProductIdentifier(),
          tellerTransaction.getProductCaseIdentifier(),
          tellerEntity.getTellerAccountIdentifier(),
          tellerTransaction.getAmount()
      );

    } else {
      throw ServiceException.notFound("Teller {0} not found." , tellerCode);
    }

  }
}

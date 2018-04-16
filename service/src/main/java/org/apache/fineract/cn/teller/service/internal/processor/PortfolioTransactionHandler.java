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
package org.apache.fineract.cn.teller.service.internal.processor;

import org.apache.fineract.cn.teller.ServiceConstants;
import org.apache.fineract.cn.teller.api.v1.domain.Charge;
import org.apache.fineract.cn.teller.api.v1.domain.TellerTransaction;
import org.apache.fineract.cn.teller.api.v1.domain.TellerTransactionCosts;
import org.apache.fineract.cn.teller.service.internal.repository.TellerEntity;
import org.apache.fineract.cn.teller.service.internal.repository.TellerRepository;
import org.apache.fineract.cn.teller.service.internal.service.helper.PortfolioService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
        charges
            .stream()
            .map(Charge::getAmount)
            .collect(Collectors.toList())
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
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

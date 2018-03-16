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
package io.mifos.teller.service.internal.processor;

import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TellerTransactionProcessor {

  private final Logger logger;
  private final DepositTransactionHandler depositTransactionHandler;
  private final PortfolioTransactionHandler portfolioTransactionHandler;
  private final ChequeTransactionHandler chequeTransactionHandler;

  @Autowired
  public TellerTransactionProcessor(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                    final DepositTransactionHandler depositTransactionHandler,
                                    final PortfolioTransactionHandler portfolioTransactionHandler,
                                    final ChequeTransactionHandler chequeTransactionHandler) {
    super();
    this.logger = logger;
    this.depositTransactionHandler = depositTransactionHandler;
    this.portfolioTransactionHandler = portfolioTransactionHandler;
    this.chequeTransactionHandler = chequeTransactionHandler;
  }

  public void process(final String tellerCode, final TellerTransaction tellerTransaction, final Boolean chargesIncluded) {
    switch (tellerTransaction.getTransactionType()) {
      case ServiceConstants.TX_OPEN_ACCOUNT:
        this.depositTransactionHandler.processDepositAccountOpening(tellerCode, tellerTransaction, chargesIncluded);
        break;
      case ServiceConstants.TX_CLOSE_ACCOUNT:
        this.depositTransactionHandler.processDepositAccountClosing(tellerCode, tellerTransaction, chargesIncluded);
        break;
      case ServiceConstants.TX_ACCOUNT_TRANSFER:
        this.depositTransactionHandler.processTransfer(tellerCode, tellerTransaction, chargesIncluded);
        break;
      case ServiceConstants.TX_CASH_DEPOSIT:
        this.depositTransactionHandler.processCashDeposit(tellerCode, tellerTransaction, chargesIncluded);
        break;
      case ServiceConstants.TX_CASH_WITHDRAWAL:
        this.depositTransactionHandler.processCashWithdrawal(tellerCode, tellerTransaction, chargesIncluded);
        break;
      case ServiceConstants.TX_REPAYMENT:
        this.portfolioTransactionHandler.processRepayment(tellerCode, tellerTransaction);
        break;
      case ServiceConstants.TX_CHEQUE:
        this.chequeTransactionHandler.processCheque(tellerCode, tellerTransaction);
        break;
      default:
        throw new IllegalArgumentException("Unsupported TX type " + tellerTransaction.getTransactionType());
    }
  }

  public TellerTransactionCosts getCosts(final TellerTransaction tellerTransaction) {
    switch (tellerTransaction.getTransactionType()) {
      case ServiceConstants.TX_OPEN_ACCOUNT:
      case ServiceConstants.TX_CLOSE_ACCOUNT:
      case ServiceConstants.TX_ACCOUNT_TRANSFER:
      case ServiceConstants.TX_CASH_DEPOSIT:
      case ServiceConstants.TX_CASH_WITHDRAWAL:
      case ServiceConstants.TX_CHEQUE:
        return this.depositTransactionHandler.getTellerTransactionCosts(tellerTransaction);
      case ServiceConstants.TX_REPAYMENT:
        return this.portfolioTransactionHandler.getTellerTransactionCosts(tellerTransaction);
      default:
        throw new IllegalArgumentException("Unsupported TX type " + tellerTransaction.getTransactionType());
    }
  }

}

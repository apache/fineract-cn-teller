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

import io.mifos.accounting.api.v1.client.AccountNotFoundException;
import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.accounting.api.v1.domain.AccountCommand;
import io.mifos.accounting.api.v1.domain.AccountEntryPage;
import io.mifos.accounting.api.v1.domain.AccountPage;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.core.lang.ServiceException;
import io.mifos.teller.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountingService {

  private final Logger logger;
  private final LedgerManager ledgerManager;

  @Autowired
  public AccountingService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                           final LedgerManager ledgerManager) {
    super();
    this.logger = logger;
    this.ledgerManager = ledgerManager;
  }

  public Optional<Account> findAccount(final String accountIdentifier) {
    try {
      return Optional.of(this.ledgerManager.findAccount(accountIdentifier));
    } catch (final AccountNotFoundException anfex) {

      final AccountPage accountPage = this.ledgerManager.fetchAccounts(true, accountIdentifier, null, true,
          0, 10, null, null);

      return accountPage.getAccounts()
          .stream()
          .filter(account -> account.getAlternativeAccountNumber() != null
              && account.getAlternativeAccountNumber().equals(accountIdentifier))
          .findFirst();
    }
  }

  public AccountEntryPage fetchAccountEntries(final String accountIdentifier, final String dateRange, final Integer pageIndex,
                                              final Integer pageSize) {
    return this.ledgerManager.fetchAccountEntries(accountIdentifier, dateRange, null, pageIndex, pageSize, null,
        Sort.Direction.ASC.name());
  }

  public void postJournalEntry(final JournalEntry journalEntry) {
    this.ledgerManager.createJournalEntry(journalEntry);
  }

  public void closeAccount(final String accountIdentifier) {
    final AccountCommand accountCommand = new AccountCommand();
    accountCommand.setAction(AccountCommand.Action.CLOSE.name());
    accountCommand.setComment(ServiceConstants.TX_CLOSE_ACCOUNT);
    this.ledgerManager.accountCommand(this.resolveAccountIdentifier(accountIdentifier), accountCommand);
  }

  public void openAccount(final String accountIdentifier) {
    final AccountCommand accountCommand = new AccountCommand();
    accountCommand.setAction(AccountCommand.Action.REOPEN.name());
    accountCommand.setComment(ServiceConstants.TX_OPEN_ACCOUNT);
    this.ledgerManager.accountCommand(this.resolveAccountIdentifier(accountIdentifier), accountCommand);
  }

  public String resolveAccountIdentifier(final String proposedAccountIdentifier) {
    final Optional<Account> resolvedAccountIdentifier = this.findAccount(proposedAccountIdentifier);
    if (resolvedAccountIdentifier.isPresent()) {
      return resolvedAccountIdentifier.get().getIdentifier();
    } else {
      throw ServiceException.notFound("Account {0} not found.", proposedAccountIdentifier);
    }
  }
}

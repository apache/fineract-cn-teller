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

import io.mifos.accounting.api.v1.domain.Creditor;
import io.mifos.accounting.api.v1.domain.Debtor;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.deposit.api.v1.definition.domain.ProductDefinition;
import io.mifos.deposit.api.v1.instance.domain.ProductInstance;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Charge;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import io.mifos.teller.service.internal.service.helper.DepositAccountManagementService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DepositTransactionHandler {

  private final Logger logger;
  private final AccountingService accountingService;
  private final DepositAccountManagementService depositAccountManagementService;
  private final TellerRepository tellerRepository;

  @Autowired
  public DepositTransactionHandler(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                   final AccountingService accountingService,
                                   final DepositAccountManagementService depositAccountManagementService,
                                   final TellerRepository tellerRepository) {
    super();
    this.logger = logger;
    this.accountingService = accountingService;
    this.depositAccountManagementService = depositAccountManagementService;
    this.tellerRepository = tellerRepository;
  }

  public TellerTransactionCosts getDepositTransactionCosts(final TellerTransaction tellerTransaction) {
    final List<Charge> charges = this.depositAccountManagementService.getCharges(tellerTransaction);

    final TellerTransactionCosts tellerTransactionCosts = new TellerTransactionCosts();
    tellerTransactionCosts.setCharges(charges);
    tellerTransactionCosts.setTellerTransactionIdentifier(tellerTransaction.getIdentifier());
    tellerTransactionCosts.setTotalAmount(
        tellerTransaction.getAmount() + charges.stream().mapToDouble(Charge::getAmount).sum()
    );

    return tellerTransactionCosts;
  }

  public void processTransfer(final String tellerCode, final TellerTransaction tellerTransaction,
                               final boolean chargesIncluded) {
    final TellerEntity tellerEntity = getTellerEntity(tellerCode);
    final JournalEntry journalEntry = this.prepareJournalEntry(tellerTransaction);

    final TellerTransactionCosts tellerTransactionCosts = this.getDepositTransactionCosts(tellerTransaction);

    final HashSet<Debtor> debtors = new HashSet<>();
    journalEntry.setDebtors(debtors);

    final Debtor customerDebtor = new Debtor();
    customerDebtor.setAccountNumber(tellerTransaction.getCustomerAccountIdentifier());
    customerDebtor.setAmount(tellerTransaction.getAmount().toString());
    debtors.add(customerDebtor);

    if (!tellerTransactionCosts.getCharges().isEmpty()) {
      if (chargesIncluded) {
        debtors.add(this.createChargesDebtor(tellerEntity.getTellerAccountIdentifier(), tellerTransactionCosts));
      } else {
        debtors.add(this.createChargesDebtor(tellerTransaction.getCustomerAccountIdentifier(), tellerTransactionCosts));
      }
    }

    final HashSet<Creditor> creditors = new HashSet<>();
    journalEntry.setCreditors(creditors);

    final Creditor targetCreditor = new Creditor();
    targetCreditor.setAccountNumber(tellerTransaction.getTargetAccountIdentifier());
    targetCreditor.setAmount(tellerTransaction.getAmount().toString());
    creditors.add(targetCreditor);

    creditors.addAll(this.createChargeCreditors(tellerTransactionCosts));

    this.accountingService.postJournalEntry(journalEntry);
  }

  public void processCashDeposit(final String tellerCode, final TellerTransaction tellerTransaction, final boolean chargesIncluded) {
    final TellerEntity tellerEntity = getTellerEntity(tellerCode);
    final JournalEntry journalEntry = this.prepareJournalEntry(tellerTransaction);
    final TellerTransactionCosts tellerTransactionCosts = this.getDepositTransactionCosts(tellerTransaction);

    final HashSet<Debtor> debtors = new HashSet<>();
    journalEntry.setDebtors(debtors);

    final Debtor tellerDebtor = new Debtor();
    tellerDebtor.setAccountNumber(tellerEntity.getTellerAccountIdentifier());
    if (chargesIncluded) {
      tellerDebtor.setAmount(tellerTransactionCosts.getTotalAmount().toString());
    } else {
      tellerDebtor.setAmount(tellerTransaction.getAmount().toString());
      if (!tellerTransactionCosts.getCharges().isEmpty()) {
        debtors.add(this.createChargesDebtor(tellerTransaction.getCustomerAccountIdentifier(), tellerTransactionCosts));
      }
    }
    debtors.add(tellerDebtor);

    final HashSet<Creditor> creditors = new HashSet<>();
    journalEntry.setCreditors(creditors);

    final Creditor customerCreditor = new Creditor();
    customerCreditor.setAccountNumber(tellerTransaction.getCustomerAccountIdentifier());
    customerCreditor.setAmount(tellerTransaction.getAmount().toString());
    creditors.add(customerCreditor);

    creditors.addAll(this.createChargeCreditors(tellerTransactionCosts));

    this.accountingService.postJournalEntry(journalEntry);
  }

  public void processCashWithdrawal(final String tellerCode, final TellerTransaction tellerTransaction,
                                    final boolean chargesIncluded) {
    final TellerEntity tellerEntity = getTellerEntity(tellerCode);
    final JournalEntry journalEntry = this.prepareJournalEntry(tellerTransaction);
    final TellerTransactionCosts tellerTransactionCosts = this.getDepositTransactionCosts(tellerTransaction);

    final HashSet<Debtor> debtors = new HashSet<>();
    journalEntry.setDebtors(debtors);

    final Debtor customerDebtor = new Debtor();
    customerDebtor.setAccountNumber(tellerTransaction.getCustomerAccountIdentifier());
    customerDebtor.setAmount(tellerTransaction.getAmount().toString());
    debtors.add(customerDebtor);

    if (!tellerTransactionCosts.getCharges().isEmpty()) {
      if (chargesIncluded) {
        debtors.add(this.createChargesDebtor(tellerEntity.getTellerAccountIdentifier(), tellerTransactionCosts));
      } else {
        debtors.add(this.createChargesDebtor(tellerTransaction.getCustomerAccountIdentifier(), tellerTransactionCosts));
      }
    }

    final HashSet<Creditor> creditors = new HashSet<>();
    journalEntry.setCreditors(creditors);

    final Creditor tellerCreditor = new Creditor();
    tellerCreditor.setAccountNumber(tellerEntity.getTellerAccountIdentifier());
    tellerCreditor.setAmount(tellerTransaction.getAmount().toString());
    creditors.add(tellerCreditor);

    creditors.addAll(this.createChargeCreditors(tellerTransactionCosts));

    this.accountingService.postJournalEntry(journalEntry);
  }

  public void processDepositAccountClosing(final String tellerCode, final TellerTransaction tellerTransaction,
                                            final boolean chargesIncluded) {
    this.processCashWithdrawal(tellerCode, tellerTransaction, chargesIncluded);

    this.depositAccountManagementService.closeProductInstance(tellerTransaction.getCustomerAccountIdentifier());
    this.accountingService.closeAccount(tellerTransaction.getCustomerAccountIdentifier());
  }

  public void processDepositAccountOpening(final String tellerCode, final TellerTransaction tellerTransaction,
                                            final boolean chargesIncluded) {
    final ProductInstance productInstances =
        this.depositAccountManagementService.findProductInstance(tellerTransaction.getCustomerAccountIdentifier());

    final ProductDefinition productDefinition =
        this.depositAccountManagementService.findProductDefinition(productInstances.getProductIdentifier());

    this.processCashDeposit(tellerCode, tellerTransaction, chargesIncluded);

    if ((tellerTransaction.getAmount() + productInstances.getBalance()) >= productDefinition.getMinimumBalance()) {
      this.depositAccountManagementService.activateProductInstance(tellerTransaction.getCustomerAccountIdentifier());
      this.accountingService.openAccount(tellerTransaction.getCustomerAccountIdentifier());
    }
  }

  private TellerEntity getTellerEntity(final String tellerCode) {
    final Optional<TellerEntity> optionalTeller = this.tellerRepository.findByIdentifier(tellerCode);
    if (!optionalTeller.isPresent()) {
      this.logger.warn("Teller {} not found.", tellerCode);
      throw new IllegalStateException("Teller not found.");
    }

    return optionalTeller.get();
  }

  private JournalEntry prepareJournalEntry(final TellerTransaction tellerTransaction) {
    final JournalEntry journalEntry = new JournalEntry();
    journalEntry.setTransactionIdentifier(tellerTransaction.getIdentifier());
    journalEntry.setTransactionDate(tellerTransaction.getTransactionDate());
    journalEntry.setTransactionType(tellerTransaction.getTransactionType());
    journalEntry.setMessage(tellerTransaction.getTransactionType());
    journalEntry.setClerk(UserContextHolder.checkedGetUser());

    return journalEntry;
  }

  private Debtor createChargesDebtor(final String accountIdentifier, final TellerTransactionCosts tellerTransactionCosts) {
    final Debtor chargesDebtor = new Debtor();
    chargesDebtor.setAccountNumber(accountIdentifier);
    chargesDebtor.setAmount(
        Double.valueOf(
            tellerTransactionCosts.getCharges()
                .stream()
                .mapToDouble(Charge::getAmount)
                .sum()
        ).toString()
    );

    return chargesDebtor;
  }

  private Set<Creditor> createChargeCreditors(final TellerTransactionCosts tellerTransactionCosts) {
    return tellerTransactionCosts.getCharges()
        .stream()
        .map(charge -> {
          final Creditor chargeCreditor = new Creditor();
          chargeCreditor.setAccountNumber(charge.getIncomeAccountIdentifier());
          chargeCreditor.setAmount(charge.getAmount().toString());
          return chargeCreditor;
        })
        .collect(Collectors.toSet());
  }
}

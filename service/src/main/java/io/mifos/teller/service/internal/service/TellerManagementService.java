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
package io.mifos.teller.service.internal.service;

import io.mifos.accounting.api.v1.domain.AccountEntryPage;
import io.mifos.core.lang.DateConverter;
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerBalanceSheet;
import io.mifos.teller.api.v1.domain.TellerEntry;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.service.internal.mapper.TellerEntryMapper;
import io.mifos.teller.service.internal.mapper.TellerMapper;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.repository.TellerTransactionEntity;
import io.mifos.teller.service.internal.repository.TellerTransactionRepository;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TellerManagementService {

  private final TellerRepository tellerRepository;
  private final TellerTransactionRepository tellerTransactionRepository;
  private final AccountingService accountingService;

  @Autowired
  public TellerManagementService(final TellerRepository tellerRepository,
                                 final TellerTransactionRepository tellerTransactionRepository,
                                 final AccountingService accountingService) {
    super();
    this.tellerRepository = tellerRepository;
    this.tellerTransactionRepository = tellerTransactionRepository;
    this.accountingService = accountingService;
  }

  public Optional<Teller> findByIdentifier(final String code) {
    return this.tellerRepository.findByIdentifier(code).map(TellerMapper::map);
  }

  public List<Teller> findByOfficeIdentifier(final String officeIdentifier) {
    return this.tellerRepository.findByOfficeIdentifier(officeIdentifier)
        .stream()
        .map(TellerMapper::map)
        .collect(Collectors.toList());
  }

  public TellerBalanceSheet getBalance(final String tellerCode) {
    final TellerBalanceSheet tellerBalanceSheet = new TellerBalanceSheet();

    final Optional<TellerEntity> optionalTellerEntity = this.tellerRepository.findByIdentifier(tellerCode);
    optionalTellerEntity.ifPresent(tellerEntity -> {

      if (tellerEntity.getLastOpenedOn() != null) {
        final String accountIdentifier = tellerEntity.getTellerAccountIdentifier();
        final LocalDate startDate = tellerEntity.getLastOpenedOn().toLocalDate();
        tellerBalanceSheet.setDay(startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
        final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
        final LocalDate endDate = now.toLocalDate();
        final String dateRange =
            DateConverter.toIsoString(startDate) + ".." + DateConverter.toIsoString(endDate);

        final List<TellerEntry> tellerEntries = this.fetchTellerEntries(accountIdentifier, dateRange, 0);

        tellerBalanceSheet.setCashEntries(tellerEntries);

        tellerBalanceSheet.setCashReceivedTotal(
            tellerBalanceSheet.getCashEntries()
                .stream()
                .filter(tellerEntry -> tellerEntry.getType().equals(TellerEntry.Type.DEBIT.name()))
                .map(TellerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        tellerBalanceSheet.setCashDisbursedTotal(
            tellerEntries
                .stream()
                .filter(tellerEntry -> tellerEntry.getType().equals(TellerEntry.Type.CREDIT.name()))
                .map(TellerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        tellerBalanceSheet.setCashOnHand(tellerBalanceSheet.getCashReceivedTotal().subtract(tellerBalanceSheet.getCashDisbursedTotal()));

        final List<TellerTransactionEntity> chequeTransactions =
            this.tellerTransactionRepository.findByTellerAndTransactionTypeAndTransactionDateBetween(tellerEntity,
                ServiceConstants.TX_CHEQUE, tellerEntity.getLastOpenedOn(), now);

        tellerBalanceSheet.setChequeEntries(
            chequeTransactions
                .stream()
                .filter(tellerTransactionEntity -> tellerTransactionEntity.getState().equals(TellerTransaction.State.CONFIRMED.name()))
                .map(tellerTransactionEntity -> {
                    final TellerEntry tellerEntry = new TellerEntry();
                    tellerEntry.setTransactionDate(DateConverter.toIsoString(tellerTransactionEntity.getTransactionDate()));
                    tellerEntry.setType(TellerEntry.Type.CHEQUE.name());
                    tellerEntry.setAmount(tellerTransactionEntity.getAmount());
                    tellerEntry.setMessage(tellerTransactionEntity.getTransactionType());
                    return tellerEntry;
                })
                .collect(Collectors.toList())
        );

        tellerBalanceSheet.setChequesReceivedTotal(
            tellerBalanceSheet.getChequeEntries()
                .stream()
                .map(TellerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
      } else {
        tellerBalanceSheet.setCashReceivedTotal(BigDecimal.ZERO);
        tellerBalanceSheet.setCashDisbursedTotal(BigDecimal.ZERO);
        tellerBalanceSheet.setCashOnHand(BigDecimal.ZERO);
        tellerBalanceSheet.setChequesReceivedTotal(BigDecimal.ZERO);
      }
    });
    return tellerBalanceSheet;
  }

  private List<TellerEntry> fetchTellerEntries(final String accountIdentifier, final String dateRange, final Integer pageIndex) {
    final ArrayList<TellerEntry> tellerEntries = new ArrayList<>();

    final AccountEntryPage accountEntryPage =
        this.accountingService.fetchAccountEntries(accountIdentifier, dateRange, pageIndex, 250);
    tellerEntries.addAll(
        accountEntryPage.getAccountEntries().stream().map(TellerEntryMapper::map).collect(Collectors.toList())
    );

    final Integer nextPage = pageIndex + 1;
    if (nextPage < accountEntryPage.getTotalPages()) {
      tellerEntries.addAll(this.fetchTellerEntries(accountIdentifier, dateRange, nextPage));
    }

    return tellerEntries;
  }

  public Optional<Teller> findByAssignedEmployee(final String employeeIdentifier) {
    return this.tellerRepository.findFirstByAssignedEmployeeIdentifier(employeeIdentifier).map(TellerMapper::map);
  }
}

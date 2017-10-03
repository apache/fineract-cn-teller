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

import io.mifos.accounting.api.v1.domain.AccountEntry;
import io.mifos.accounting.api.v1.domain.AccountEntryPage;
import io.mifos.core.lang.DateConverter;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerBalanceSheet;
import io.mifos.teller.api.v1.domain.TellerEntry;
import io.mifos.teller.service.internal.mapper.TellerEntryMapper;
import io.mifos.teller.service.internal.mapper.TellerMapper;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TellerManagementService {

  private final TellerRepository tellerRepository;
  private final AccountingService accountingService;

  @Autowired
  public TellerManagementService(final TellerRepository tellerRepository,
                                 final AccountingService accountingService) {
    super();
    this.tellerRepository = tellerRepository;
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

      final String accountIdentifier = tellerEntity.getTellerAccountIdentifier();
      final LocalDate startDate = tellerEntity.getLastOpenedOn().toLocalDate();
      final LocalDate endDate = LocalDate.now(Clock.systemUTC());
      final String dateRange =
          DateConverter.toIsoString(startDate) + ".." + DateConverter.toIsoString(endDate);

      final List<TellerEntry> tellerEntries = this.fetchTellerEntries(accountIdentifier, dateRange, 0);
      tellerBalanceSheet.setEntries(tellerEntries);
      tellerBalanceSheet.setDay(startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
      final double sumDebits = tellerEntries
          .stream()
          .filter(tellerEntry -> tellerEntry.getType().equals(AccountEntry.Type.DEBIT.name()))
          .mapToDouble(tellerEntry -> tellerEntry.getAmount().doubleValue())
          .sum();

      final double sumCredits = tellerEntries
          .stream()
          .filter(tellerEntry -> tellerEntry.getType().equals(AccountEntry.Type.CREDIT.name()))
          .mapToDouble(tellerEntry -> tellerEntry.getAmount().doubleValue())
          .sum();

      tellerBalanceSheet.setBalance(BigDecimal.valueOf(sumDebits - sumCredits));
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

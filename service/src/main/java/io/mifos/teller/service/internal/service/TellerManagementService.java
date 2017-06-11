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
import io.mifos.teller.ServiceConstants;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerBalanceSheet;
import io.mifos.teller.api.v1.domain.TellerEntry;
import io.mifos.teller.service.internal.mapper.TellerEntryMapper;
import io.mifos.teller.service.internal.mapper.TellerMapper;
import io.mifos.teller.service.internal.repository.TellerEntity;
import io.mifos.teller.service.internal.repository.TellerRepository;
import io.mifos.teller.service.internal.service.helper.AccountingService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TellerManagementService {

  private final Logger logger;
  private final TellerRepository tellerRepository;
  private final AccountingService accountingService;

  @Autowired
  public TellerManagementService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                 final TellerRepository tellerRepository,
                                 final AccountingService accountingService) {
    super();
    this.logger = logger;
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
      final LocalDateTime lastModifiedOn = tellerEntity.getLastModifiedOn();
      final LocalDateTime startDate = lastModifiedOn.withHour(0).withMinute(0).withSecond(0).withNano(0);
      final LocalDateTime endDate = lastModifiedOn.withHour(23).withMinute(59).withSecond(59).withNano(999);
      final String dateRange =
          startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".." + endDate.format(DateTimeFormatter.BASIC_ISO_DATE);

      final List<TellerEntry> tellerEntries = this.fetchTellerEntries(accountIdentifier, dateRange, 0);

      tellerBalanceSheet.setDay(startDate.format(DateTimeFormatter.BASIC_ISO_DATE));
      tellerBalanceSheet.setBalance(tellerEntries.stream().mapToDouble(TellerEntry::getAmount).sum());
      tellerBalanceSheet.setEntries(tellerEntries);
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
}

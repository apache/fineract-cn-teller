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
package io.mifos.teller;

import com.google.common.collect.Lists;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.accounting.api.v1.domain.AccountEntry;
import io.mifos.accounting.api.v1.domain.AccountEntryPage;
import io.mifos.accounting.api.v1.domain.JournalEntry;
import io.mifos.core.lang.DateRange;
import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.client.TellerValidationException;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerDenomination;
import io.mifos.teller.api.v1.domain.TellerManagementCommand;
import io.mifos.teller.api.v1.domain.UnlockDrawerCommand;
import io.mifos.teller.util.TellerGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TestTellerDenomination extends AbstractTellerTest {

  public TestTellerDenomination() {
    super();
  }

  @Test
  public void shouldProcessDenomination() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final BigDecimal openingAmount = BigDecimal.valueOf(20000.00D);

    final Teller teller = this.prepareTeller(officeIdentifier, openingAmount);

    super.testSubject.post(teller.getCode(), "PAUSE");
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PAUSE_TELLER, teller.getCode()));

    final AccountEntry openingAmountEntry = new AccountEntry();
    openingAmountEntry.setType(AccountEntry.Type.DEBIT.name());
    openingAmountEntry.setMessage(ServiceConstants.TX_REPAYMENT);
    openingAmountEntry.setAmount(openingAmount.doubleValue());

    final AccountEntryPage accountEntryPage = new AccountEntryPage();
    accountEntryPage.setAccountEntries(Lists.newArrayList(openingAmountEntry));
    accountEntryPage.setTotalPages(1);
    accountEntryPage.setTotalElements(Integer.valueOf(accountEntryPage.getAccountEntries().size()).longValue());

    Mockito
        .doAnswer(invocation -> accountEntryPage)
        .when(super.accountingServiceSpy)
        .fetchAccountEntries(Matchers.eq(teller.getTellerAccountIdentifier()), Matchers.anyString(),
            Matchers.eq(0), Matchers.anyInt());

    final TellerDenomination tellerDenomination = new TellerDenomination();
    tellerDenomination.setCountedTotal(openingAmount);
    tellerDenomination.setNote("Nothing has happened.");

    super.testSubject.saveTellerDenomination(officeIdentifier, teller.getCode(), tellerDenomination);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.SAVE_DENOMINATION, teller.getCode()));

    Mockito
        .verify(super.accountingServiceSpy, Mockito.atMost(1))
        .postJournalEntry(Matchers.any(JournalEntry.class));

    final LocalDate now = LocalDate.now(Clock.systemUTC());
    final DateRange dateRange = new DateRange(now, now);
    final List<TellerDenomination> tellerDenominations =
        super.testSubject.fetchTellerDenominations(officeIdentifier, teller.getCode(), dateRange.toString());

    Assert.assertEquals(1, tellerDenominations.size());
    Assert.assertNull(tellerDenominations.get(0).getAdjustingJournalEntry());

    this.closeTeller(officeIdentifier, teller.getCode());
  }

  @Test
  public void shouldProcessDenominationOver() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final BigDecimal openingAmount = BigDecimal.valueOf(20000.00D);

    final Teller teller = this.prepareTeller(officeIdentifier, openingAmount);

    super.testSubject.post(teller.getCode(), "PAUSE");
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PAUSE_TELLER, teller.getCode()));

    final AccountEntry openingAmountEntry = new AccountEntry();
    openingAmountEntry.setType(AccountEntry.Type.DEBIT.name());
    openingAmountEntry.setMessage(ServiceConstants.TX_REPAYMENT);
    openingAmountEntry.setAmount(openingAmount.doubleValue());

    final AccountEntryPage accountEntryPage = new AccountEntryPage();
    accountEntryPage.setAccountEntries(Lists.newArrayList(openingAmountEntry));
    accountEntryPage.setTotalPages(1);
    accountEntryPage.setTotalElements(Integer.valueOf(accountEntryPage.getAccountEntries().size()).longValue());

    Mockito
        .doAnswer(invocation -> accountEntryPage)
        .when(super.accountingServiceSpy)
        .fetchAccountEntries(Matchers.eq(teller.getTellerAccountIdentifier()), Matchers.anyString(),
            Matchers.eq(0), Matchers.anyInt());

    final TellerDenomination tellerDenomination = new TellerDenomination();
    tellerDenomination.setCountedTotal(openingAmount.add(BigDecimal.valueOf(1000.00D)));
    tellerDenomination.setNote("Teller is over.");

    super.testSubject.saveTellerDenomination(officeIdentifier, teller.getCode(), tellerDenomination);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.SAVE_DENOMINATION, teller.getCode()));

    Mockito
        .verify(super.accountingServiceSpy, Mockito.atMost(2))
        .postJournalEntry(Matchers.any(JournalEntry.class));

    final LocalDate now = LocalDate.now(Clock.systemUTC());
    final DateRange dateRange = new DateRange(now, now);
    final List<TellerDenomination> tellerDenominations =
        super.testSubject.fetchTellerDenominations(officeIdentifier, teller.getCode(), dateRange.toString());

    Assert.assertEquals(1, tellerDenominations.size());
    Assert.assertNotNull(tellerDenominations.get(0).getAdjustingJournalEntry());

    this.closeTeller(officeIdentifier, teller.getCode());
  }

  @Test
  public void shouldProcessDenominationShort() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final BigDecimal openingAmount = BigDecimal.valueOf(20000.00D);

    final Teller teller = this.prepareTeller(officeIdentifier, openingAmount);

    super.testSubject.post(teller.getCode(), "PAUSE");
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PAUSE_TELLER, teller.getCode()));

    final AccountEntry openingAmountEntry = new AccountEntry();
    openingAmountEntry.setType(AccountEntry.Type.DEBIT.name());
    openingAmountEntry.setMessage(ServiceConstants.TX_REPAYMENT);
    openingAmountEntry.setAmount(openingAmount.doubleValue());

    final AccountEntryPage accountEntryPage = new AccountEntryPage();
    accountEntryPage.setAccountEntries(Lists.newArrayList(openingAmountEntry));
    accountEntryPage.setTotalPages(1);
    accountEntryPage.setTotalElements(Integer.valueOf(accountEntryPage.getAccountEntries().size()).longValue());

    Mockito
        .doAnswer(invocation -> accountEntryPage)
        .when(super.accountingServiceSpy)
        .fetchAccountEntries(Matchers.eq(teller.getTellerAccountIdentifier()), Matchers.anyString(),
            Matchers.eq(0), Matchers.anyInt());

    final TellerDenomination tellerDenomination = new TellerDenomination();
    tellerDenomination.setCountedTotal(openingAmount.subtract(BigDecimal.valueOf(1000.00D)));
    tellerDenomination.setNote("Teller is short.");

    super.testSubject.saveTellerDenomination(officeIdentifier, teller.getCode(), tellerDenomination);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.SAVE_DENOMINATION, teller.getCode()));

    Mockito
        .verify(super.accountingServiceSpy, Mockito.atMost(2))
        .postJournalEntry(Matchers.any(JournalEntry.class));

    final LocalDate now = LocalDate.now(Clock.systemUTC());
    final DateRange dateRange = new DateRange(now, now);
    final List<TellerDenomination> tellerDenominations =
        super.testSubject.fetchTellerDenominations(officeIdentifier, teller.getCode(), dateRange.toString());

    Assert.assertEquals(1, tellerDenominations.size());
    Assert.assertNotNull(tellerDenominations.get(0).getAdjustingJournalEntry());

    this.closeTeller(officeIdentifier, teller.getCode());
  }

  @Test
  public void shouldNotProcessDenominationIsActive() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final BigDecimal openingAmount = BigDecimal.valueOf(20000.00D);

    final Teller teller = this.prepareTeller(officeIdentifier, openingAmount);

    final TellerDenomination tellerDenomination = new TellerDenomination();
    tellerDenomination.setCountedTotal(openingAmount.subtract(BigDecimal.valueOf(1000.00D)));

    try {
      super.testSubject.saveTellerDenomination(officeIdentifier, teller.getCode(), tellerDenomination);
      Assert.fail();
    } catch (final TellerValidationException tvex) {
      // do nothing ... expected
    }

    this.closeTeller(officeIdentifier, teller.getCode());
  }

  private Teller prepareTeller(final String officeIdentifier, BigDecimal openingAmount)  throws Exception {
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getVaultAccountIdentifier()));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getChequesReceivableAccount()));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getCashOverShortAccount()));

    super.testSubject.create(officeIdentifier, teller);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode()));

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.OPEN.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.DEBIT.name());
    command.setAmount(openingAmount);
    command.setAssignedEmployeeIdentifier(AbstractTellerTest.TEST_USER);

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, teller.getCode(), command);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, teller.getCode()));

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode()));

    return teller;
  }

  private void closeTeller(final String officeIdentifier, final String tellerCode) throws Exception {
    final TellerManagementCommand closeTellerCommand = new TellerManagementCommand();
    closeTellerCommand.setAction(TellerManagementCommand.Action.CLOSE.name());
    closeTellerCommand.setAmount(BigDecimal.ZERO);
    closeTellerCommand.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());

    super.testSubject.post(officeIdentifier, tellerCode, closeTellerCommand);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.CLOSE_TELLER, tellerCode));

  }
}

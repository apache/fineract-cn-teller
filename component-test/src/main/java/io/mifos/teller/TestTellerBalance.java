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
import io.mifos.core.lang.DateConverter;
import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.domain.Cheque;
import io.mifos.teller.api.v1.domain.MICR;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerBalanceSheet;
import io.mifos.teller.api.v1.domain.TellerManagementCommand;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.api.v1.domain.TellerTransactionCosts;
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
import java.time.LocalDateTime;
import java.util.Optional;

public class TestTellerBalance extends AbstractTellerTest {

  @Test
  public void shouldCreateTellerBalanceSheet() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller randomTeller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(randomTeller.getTellerAccountIdentifier()));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(randomTeller.getVaultAccountIdentifier()));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(randomTeller.getChequesReceivableAccount()));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(randomTeller.getCashOverShortAccount()));

    super.testSubject.create(officeIdentifier, randomTeller);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, randomTeller.getCode()));

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.OPEN.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    command.setAssignedEmployeeIdentifier(AbstractTellerTest.TEST_USER);

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, randomTeller.getCode(), command);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, randomTeller.getCode()));

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(randomTeller.getPassword());
    super.testSubject.unlockDrawer(randomTeller.getCode(), unlockDrawerCommand);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, randomTeller.getCode()));

    this.prepareCheque(randomTeller);
    this.prepareAccountEntryMocks(randomTeller.getTellerAccountIdentifier());

    final TellerBalanceSheet tellerBalanceSheet = super.testSubject.getBalance(officeIdentifier, randomTeller.getCode());
    Assert.assertTrue(BigDecimal.valueOf(604.00D).compareTo(tellerBalanceSheet.getCashReceivedTotal()) == 0);
    Assert.assertTrue(BigDecimal.valueOf(150.00D).compareTo(tellerBalanceSheet.getCashDisbursedTotal()) == 0);
    Assert.assertTrue(BigDecimal.valueOf(500.00D).compareTo(tellerBalanceSheet.getChequesReceivedTotal()) == 0);
    Assert.assertTrue(BigDecimal.valueOf(454.00D).compareTo(tellerBalanceSheet.getCashOnHand()) == 0);
  }

  private void prepareCheque(final Teller teller) throws Exception {
    final TellerTransaction chequeTransaction =  new TellerTransaction();
    chequeTransaction.setTransactionType(ServiceConstants.TX_CHEQUE);
    chequeTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    chequeTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    chequeTransaction.setProductCaseIdentifier(RandomStringUtils.randomAlphanumeric(32));
    chequeTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    chequeTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    chequeTransaction.setClerk(AbstractTellerTest.TEST_USER);
    chequeTransaction.setAmount(BigDecimal.valueOf(500.00D));

    final MICR micr = new MICR();
    micr.setChequeNumber("0011");
    micr.setBranchSortCode("08154711");
    micr.setAccountNumber("4711");

    Mockito
        .doAnswer(invocation -> Optional.empty())
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(micr.getAccountNumber()));

    final Cheque cheque = new Cheque();
    cheque.setMicr(micr);
    cheque.setDrawee("whatever Bank");
    cheque.setDrawer("Jane Doe");
    cheque.setPayee("John Doe");
    cheque.setDateIssued(DateConverter.toIsoString(LocalDate.now(Clock.systemUTC())));
    cheque.setAmount(BigDecimal.valueOf(500.00D));
    cheque.setOpenCheque(Boolean.FALSE);
    chequeTransaction.setCheque(cheque);

    Mockito
        .doAnswer(invocation -> {
          final Account mockedAccount = new Account();
          mockedAccount.setState(Account.State.OPEN.name());
          return Optional.of(mockedAccount);
        })
        .when(super.accountingServiceSpy).findAccount(chequeTransaction.getCustomerAccountIdentifier());

    final TellerTransactionCosts tellerTransactionCosts = super.testSubject.post(teller.getCode(), chequeTransaction);

    super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(),"CONFIRM", null);

    Assert.assertTrue(
        super.eventRecorder.wait(EventConstants.CONFIRM_TRANSACTION,
            tellerTransactionCosts.getTellerTransactionIdentifier())
    );
  }

  private void prepareAccountEntryMocks(final String accountIdentifier) {

    final AccountEntry firstDeposit = new AccountEntry();
    firstDeposit.setType(AccountEntry.Type.DEBIT.name());
    firstDeposit.setMessage(ServiceConstants.TX_CASH_DEPOSIT);
    firstDeposit.setAmount(200.00D);

    final AccountEntry secondDeposit = new AccountEntry();
    secondDeposit.setType(AccountEntry.Type.DEBIT.name());
    secondDeposit.setMessage(ServiceConstants.TX_CASH_DEPOSIT);
    secondDeposit.setAmount(150.00D);

    final AccountEntry firstWithdrawal = new AccountEntry();
    firstWithdrawal.setType(AccountEntry.Type.CREDIT.name());
    firstWithdrawal.setMessage(ServiceConstants.TX_CASH_WITHDRAWAL);
    firstWithdrawal.setAmount(50.00D);

    final AccountEntry secondWithdrawal = new AccountEntry();
    secondWithdrawal.setType(AccountEntry.Type.CREDIT.name());
    secondWithdrawal.setMessage(ServiceConstants.TX_CASH_WITHDRAWAL);
    secondWithdrawal.setAmount(100.00D);

    final AccountEntry loanRepayment = new AccountEntry();
    loanRepayment.setType(AccountEntry.Type.DEBIT.name());
    loanRepayment.setMessage(ServiceConstants.TX_REPAYMENT);
    loanRepayment.setAmount(254.00D);

    final AccountEntryPage accountEntryPage = new AccountEntryPage();
    accountEntryPage.setAccountEntries(
        Lists.newArrayList(firstDeposit, secondDeposit, firstWithdrawal, secondWithdrawal, loanRepayment)
    );
    accountEntryPage.setTotalPages(1);
    accountEntryPage.setTotalElements(Integer.valueOf(accountEntryPage.getAccountEntries().size()).longValue());

    Mockito
        .doAnswer(invocation -> accountEntryPage)
        .when(super.accountingServiceSpy)
        .fetchAccountEntries(Matchers.eq(accountIdentifier), Matchers.anyString(), Matchers.eq(0), Matchers.anyInt());
  }
}

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

import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.lang.DateConverter;
import io.mifos.deposit.api.v1.definition.domain.ProductDefinition;
import io.mifos.deposit.api.v1.instance.domain.ProductInstance;
import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.client.TellerNotFoundException;
import io.mifos.teller.api.v1.client.TellerTransactionValidationException;
import io.mifos.teller.api.v1.client.TellerValidationException;
import io.mifos.teller.api.v1.client.TransactionProcessingException;
import io.mifos.teller.api.v1.domain.Teller;
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

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

public class TestTellerOperation extends AbstractTellerTest {

  private static Teller tellerUnderTest = null;

  public TestTellerOperation() {
    super();
  }

  @Test
  public void shouldUnlock() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }


  @Test(expected = TellerNotFoundException.class)
  public void shouldNotUnlockUserMismatch() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier("unassigneduser");
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }

  @Test(expected = TellerNotFoundException.class)
  public void shouldNotUnlockPasswordMismatch() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword("wrongpasword");

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }

  @Test
  public void shouldPauseTeller() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    super.testSubject.post(teller.getCode(), "PAUSE");

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PAUSE_TELLER, teller.getCode()));
  }

  @Test
  public void shouldOpenAccount() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(1234.56D);

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  @Test
  public void shouldCloseAccount() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(1234.56D);

    final Account account = new Account();
    account.setBalance(2000.00D);
    Mockito.doAnswer(invocation -> Optional.of(account))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  @Test
  public void shouldTransferAccountToAccount() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_ACCOUNT_TRANSFER);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setTargetAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(1234.56D);

    final Account account = new Account();
    account.setBalance(2000.00D);
    Mockito.doAnswer(invocation -> Optional.of(account))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getTargetAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  @Test
  public void shouldDeposit() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_DEPOSIT);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(1234.56D);

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  @Test
  public void shouldWithdraw() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(1234.56D);

    final Account account = new Account();
    account.setBalance(2000.00D);
    Mockito.doAnswer(invocation -> Optional.of(account))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  @Test(expected = TransactionProcessingException.class)
  public void shouldNotWithdrawLackingBalance() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(5000.00D);

    final Account account = new Account();
    account.setBalance(2000.00D);
    Mockito.doAnswer(invocation -> Optional.of(account))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  @Test(expected = TransactionProcessingException.class)
  public void shouldNotWithdrawExceedsCashDrawLimit() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction =  new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
    tellerTransaction.setAmount(15000.00D);

    final Account account = new Account();
    account.setBalance(20000.00D);
    Mockito.doAnswer(invocation -> Optional.of(account))
        .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  private Teller prepareTeller() throws Exception {
    if (TestTellerOperation.tellerUnderTest == null) {
      final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
      TestTellerOperation.tellerUnderTest = TellerGenerator.createRandomTeller();

      Mockito.doAnswer(invocation -> true)
          .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

      Mockito.doAnswer(invocation -> Optional.of(new Account()))
          .when(super.accountingServiceSpy).findAccount(Matchers.eq(TestTellerOperation.tellerUnderTest.getTellerAccountIdentifier()));

      Mockito.doAnswer(invocation -> Optional.of(new Account()))
          .when(super.accountingServiceSpy).findAccount(Matchers.eq(TestTellerOperation.tellerUnderTest.getVaultAccountIdentifier()));

      super.testSubject.create(officeIdentifier, TestTellerOperation.tellerUnderTest);

      Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, TestTellerOperation.tellerUnderTest.getCode()));

      final TellerManagementCommand command = new TellerManagementCommand();
      command.setAction(TellerManagementCommand.Action.OPEN.name());
      command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
      command.setAssignedEmployeeIdentifier(AbstractTellerTest.TEST_USER);

      Mockito.doAnswer(invocation -> true)
          .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

      super.testSubject.post(officeIdentifier, TestTellerOperation.tellerUnderTest.getCode(), command);

      Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, TestTellerOperation.tellerUnderTest.getCode()));
    }

    final ProductInstance productInstance = new ProductInstance();
    productInstance.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    productInstance.setBalance(0.00D);
    Mockito.doAnswer(invocation -> productInstance)
        .when(super.depositAccountManagementServiceSpy).findProductInstance(Matchers.anyString());

    final ProductDefinition productDefinition = new ProductDefinition();
    productDefinition.setMinimumBalance(0.00D);
    Mockito.doAnswer(invocation -> productDefinition)
        .when(super.depositAccountManagementServiceSpy).findProductDefinition(Matchers.eq(productInstance.getProductIdentifier()));

    return TestTellerOperation.tellerUnderTest;
  }

  @Test(expected = TellerNotFoundException.class)
  public void shouldNotUnlockTellerClosed() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode()));

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }

  @Test(expected = TellerTransactionValidationException.class)
  public void shouldNotReopenAccountClosed() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction openAccountTransaction =  new TellerTransaction();
    openAccountTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
    openAccountTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    openAccountTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    openAccountTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    openAccountTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    openAccountTransaction.setClerk(AbstractTellerTest.TEST_USER);
    openAccountTransaction.setAmount(1234.56D);

    Mockito.doAnswer(invocation -> Optional.of(new Account()))
        .when(super.accountingServiceSpy).findAccount(openAccountTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(openAccountTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).fetchProductInstances(openAccountTransaction.getCustomerIdentifier());

    final TellerTransactionCosts openingCosts = super.testSubject.post(teller.getCode(), openAccountTransaction);
    super.testSubject.confirm(teller.getCode(), openingCosts.getTellerTransactionIdentifier(), "CONFIRM", "excluded");
    super.eventRecorder.wait(EventConstants.CONFIRM_TRANSACTION, openingCosts.getTellerTransactionIdentifier());

    final TellerTransaction closeAccountTransaction =  new TellerTransaction();
    closeAccountTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
    closeAccountTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    closeAccountTransaction.setProductIdentifier(openAccountTransaction.getProductIdentifier());
    closeAccountTransaction.setCustomerAccountIdentifier(openAccountTransaction.getCustomerAccountIdentifier());
    closeAccountTransaction.setCustomerIdentifier(openAccountTransaction.getCustomerIdentifier());
    closeAccountTransaction.setClerk(AbstractTellerTest.TEST_USER);
    closeAccountTransaction.setAmount(1234.56D);

    final Account account = new Account();
    account.setBalance(1234.56D);

    Mockito.doAnswer(invocation -> Optional.of(account))
        .when(super.accountingServiceSpy).findAccount(openAccountTransaction.getCustomerAccountIdentifier());

    final TellerTransactionCosts closingCosts = super.testSubject.post(teller.getCode(), closeAccountTransaction);
    super.testSubject.confirm(teller.getCode(), closingCosts.getTellerTransactionIdentifier(), "CONFIRM", "excluded");
    super.eventRecorder.wait(EventConstants.CONFIRM_TRANSACTION, closingCosts.getTellerTransactionIdentifier());

    final TellerTransaction reopenAccountTransaction =  new TellerTransaction();
    reopenAccountTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
    reopenAccountTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    reopenAccountTransaction.setProductIdentifier(openAccountTransaction.getProductIdentifier());
    reopenAccountTransaction.setCustomerAccountIdentifier(openAccountTransaction.getCustomerAccountIdentifier());
    reopenAccountTransaction.setCustomerIdentifier(openAccountTransaction.getCustomerIdentifier());
    reopenAccountTransaction.setClerk(AbstractTellerTest.TEST_USER);
    reopenAccountTransaction.setAmount(1234.56D);

    super.testSubject.post(teller.getCode(), reopenAccountTransaction);
  }
}

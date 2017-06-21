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

import io.mifos.core.lang.DateConverter;
import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.client.TellerNotFoundException;
import io.mifos.teller.api.v1.client.TellerValidationException;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.UnlockDrawerCommand;
import io.mifos.teller.api.v1.domain.TellerManagementCommand;
import io.mifos.teller.api.v1.domain.TellerTransaction;
import io.mifos.teller.util.TellerGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;

public class TestTellerOperation extends AbstractTellerTest {

  public TestTellerOperation() {
    super();
  }

  @Test
  public void shouldAuthenticate() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }


  @Test(expected = TellerNotFoundException.class)
  public void shouldNotAuthenticateUserMismatch() throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier("unassigneduser");
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }

  @Test(expected = TellerNotFoundException.class)
  public void shouldNotAuthenticatePasswordMismatch() throws Exception {
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

  @Test(expected = TellerValidationException.class)
  public void shouldNotPauseTellerNotAuthenticated() throws Exception {
    final Teller teller = this.prepareTeller();

    super.testSubject.post(teller.getCode(), "PAUSE");
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

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getProductInstance(tellerTransaction.getCustomerIdentifier());

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

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getProductInstance(tellerTransaction.getCustomerIdentifier());

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

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(tellerTransaction.getTargetAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getProductInstance(tellerTransaction.getCustomerIdentifier());

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

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getProductInstance(tellerTransaction.getCustomerIdentifier());

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

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(tellerTransaction.getCustomerAccountIdentifier());
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
    Mockito.doAnswer(invocation -> Collections.emptyList())
        .when(super.depositAccountManagementServiceSpy).getProductInstance(tellerTransaction.getCustomerIdentifier());

    super.testSubject.post(teller.getCode(), tellerTransaction);
  }

  private Teller prepareTeller() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode()));

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.OPEN.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    command.setAssignedEmployeeIdentifier(AbstractTellerTest.TEST_USER);

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, teller.getCode(), command);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, teller.getCode()));

    return teller;
  }

  @Test(expected = TellerNotFoundException.class)
  public void shouldNotUnlockTellerClosed() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode()));

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
  }
}

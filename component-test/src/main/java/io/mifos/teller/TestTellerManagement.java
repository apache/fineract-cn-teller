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

import io.mifos.teller.api.v1.EventConstants;
import io.mifos.teller.api.v1.client.TellerAlreadyExistsException;
import io.mifos.teller.api.v1.client.TellerNotFoundException;
import io.mifos.teller.api.v1.client.TellerValidationException;
import io.mifos.teller.api.v1.domain.Teller;
import io.mifos.teller.api.v1.domain.TellerManagementCommand;
import io.mifos.teller.util.TellerGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

public class TestTellerManagement extends AbstractTellerTest {

  public TestTellerManagement() {
    super();
  }

  @Test
  public void shouldCreateTeller() throws Exception {
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
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotCreateTellerUnknownOffice() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> false)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    super.testSubject.create(officeIdentifier, teller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotCreateTellerUnknownTellerAccount() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> false)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotCreateTellerUnknownVaultAccount() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> false)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);
  }


  @Test(expected = TellerAlreadyExistsException.class)
  public void shouldCreateTellerAlreadyExists() throws Exception {
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

    super.testSubject.create(officeIdentifier, teller);
  }

  @Test
  public void shouldFindTeller() throws Exception {
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

    final Teller foundTeller = super.testSubject.find(officeIdentifier, teller.getCode());
    Assert.assertNotNull(foundTeller);

    this.compareTeller(teller, foundTeller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotFindTellerUnknownOffice() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final String tellerCode = RandomStringUtils.randomAlphabetic(32);

    Mockito.doAnswer(invocation -> false)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    super.testSubject.find(officeIdentifier, tellerCode);
  }

  @Test(expected = TellerNotFoundException.class)
  public void shouldNotFindTellerUnknownTeller() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final String tellerCode = RandomStringUtils.randomAlphabetic(32);

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    super.testSubject.find(officeIdentifier, tellerCode);
  }

  @Test
  public void shouldFetchTeller() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final List<Teller> tellerToCreate = Arrays.asList(
        TellerGenerator.createRandomTeller(),
        TellerGenerator.createRandomTeller(),
        TellerGenerator.createRandomTeller(),
        TellerGenerator.createRandomTeller(),
        TellerGenerator.createRandomTeller()
    );

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    tellerToCreate.forEach(teller -> {
      Mockito.doAnswer(invocation -> true)
          .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

      Mockito.doAnswer(invocation -> true)
          .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

      super.testSubject.create(officeIdentifier, teller);

      try {
        Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode()));
      } catch (final InterruptedException e) {
        throw new IllegalStateException(e);
      }
    });

    final List<Teller> fetchedTeller = super.testSubject.fetch(officeIdentifier);

    Assert.assertTrue(fetchedTeller.size() >= tellerToCreate.size());
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotFetchTellerUnknownOffice() {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);

    Mockito.doAnswer(invocation -> false)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    super.testSubject.fetch(officeIdentifier);
  }

  @Test
  public void shouldUpdateTeller() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    teller.setCashdrawLimit(15000.00D);

    super.testSubject.change(officeIdentifier, teller.getCode(), teller);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.PUT_TELLER, teller.getCode()));

    final Teller changedTeller = super.testSubject.find(officeIdentifier, teller.getCode());
    Assert.assertNotNull(changedTeller);

    this.compareTeller(teller, changedTeller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotUpdateTellerUnknownOffice() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    Mockito.doAnswer(invocation -> false)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    super.testSubject.change(officeIdentifier, teller.getCode(), teller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotUpdateTellerUnknownTellerAccount() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    teller.setTellerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));

    Mockito.doAnswer(invocation -> false)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    super.testSubject.change(officeIdentifier, teller.getCode(), teller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotUpdateTellerUnknownVaultAccount() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    teller.setVaultAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));

    Mockito.doAnswer(invocation -> false)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.change(officeIdentifier, teller.getCode(), teller);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotUpdateTellerMismatch() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final String tellerCode = RandomStringUtils.randomAlphanumeric(32);
    final Teller teller = TellerGenerator.createRandomTeller();
    teller.setCode(tellerCode);

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    teller.setCode(RandomStringUtils.randomAlphanumeric(32));

    super.testSubject.change(officeIdentifier, tellerCode, teller);
  }

  @Test(expected = TellerNotFoundException.class)
  public void shouldNotUpdateTellerUnknownTeller() {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    super.testSubject.change(officeIdentifier, teller.getCode(), teller);
  }

  @Test
  public void shouldOpenTeller() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.OPEN.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    command.setAssignedEmployeeIdentifier(RandomStringUtils.randomAlphanumeric(32));

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, teller.getCode(), command);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, teller.getCode()));

    final Teller openedTeller = super.testSubject.find(officeIdentifier, teller.getCode());
    Assert.assertEquals(Teller.State.OPEN.name(), openedTeller.getState());
    Assert.assertEquals(command.getAssignedEmployeeIdentifier(), openedTeller.getAssignedEmployee());
  }

  @Test
  public void shouldCloseTeller() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.OPEN.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    command.setAssignedEmployeeIdentifier(RandomStringUtils.randomAlphanumeric(32));

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, teller.getCode(), command);

    super.eventRecorder.wait(EventConstants.OPEN_TELLER, teller.getCode());

    command.setAction(TellerManagementCommand.Action.CLOSE.name());
    command.setAssignedEmployeeIdentifier(null);
    super.testSubject.post(officeIdentifier, teller.getCode(), command);

    Assert.assertTrue(super.eventRecorder.wait(EventConstants.CLOSE_TELLER, teller.getCode()));

    final Teller openedTeller = super.testSubject.find(officeIdentifier, teller.getCode());
    Assert.assertEquals(Teller.State.CLOSED.name(), openedTeller.getState());
    Assert.assertNull(openedTeller.getAssignedEmployee());
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotCloseTellerAlreadyClosed() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.CLOSE.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());

    super.testSubject.post(officeIdentifier, teller.getCode(), command);
  }

  @Test(expected = TellerValidationException.class)
  public void shouldNotOpenTellerAlreadyOpen() throws Exception {
    final String officeIdentifier = RandomStringUtils.randomAlphabetic(32);
    final Teller teller = TellerGenerator.createRandomTeller();

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getTellerAccountIdentifier()));

    Mockito.doAnswer(invocation -> true)
        .when(super.accountingServiceSpy).accountExists(Matchers.eq(teller.getVaultAccountIdentifier()));

    super.testSubject.create(officeIdentifier, teller);

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    final TellerManagementCommand command = new TellerManagementCommand();
    command.setAction(TellerManagementCommand.Action.OPEN.name());
    command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    command.setAssignedEmployeeIdentifier(RandomStringUtils.randomAlphanumeric(32));

    Mockito.doAnswer(invocation -> true)
        .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, teller.getCode(), command);

    super.eventRecorder.wait(EventConstants.OPEN_TELLER, teller.getCode());

    // and again ...
    super.testSubject.post(officeIdentifier, teller.getCode(), command);
  }

  private void compareTeller(final Teller expected, final Teller actual) {
    Assert.assertEquals(expected.getCode(), actual.getCode());
    Assert.assertEquals(expected.getTellerAccountIdentifier(), actual.getTellerAccountIdentifier());
    Assert.assertEquals(expected.getVaultAccountIdentifier(), actual.getVaultAccountIdentifier());
    Assert.assertEquals(expected.getCashdrawLimit(), actual.getCashdrawLimit());
  }
}

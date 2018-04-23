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
package org.apache.fineract.cn.teller;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.ProductDefinition;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.ProductInstance;
import org.apache.fineract.cn.lang.DateConverter;
import org.apache.fineract.cn.teller.api.v1.EventConstants;
import org.apache.fineract.cn.teller.api.v1.client.TellerNotFoundException;
import org.apache.fineract.cn.teller.api.v1.client.TellerTransactionValidationException;
import org.apache.fineract.cn.teller.api.v1.client.TransactionProcessingException;
import org.apache.fineract.cn.teller.api.v1.domain.*;
import org.apache.fineract.cn.teller.service.internal.service.helper.ChequeService;
import org.apache.fineract.cn.teller.util.TellerGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestTellerOperation extends AbstractTellerTest {

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("src/doc/generated-snippets/test-operation-management");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    final String path = "/teller/v1";

    @Before
    public void setUp(){

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
                .build();
    }

    private static Teller tellerUnderTest = null;
    private final BigDecimal commonAmount = BigDecimal.valueOf(1234.56D);

    @MockBean
    private ChequeService chequeService;

    public TestTellerOperation() {
        super();
    }

    @Test
    public void shouldUnlock() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/drawer/")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode()).accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
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
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword("wrongpasword");

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
    }

    @Test
    public void shouldPauseTeller() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        super.testSubject.post(teller.getCode(), "PAUSE");

        Assert.assertTrue(super.eventRecorder.wait(EventConstants.PAUSE_TELLER, teller.getCode()));

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Paused").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldOpenAccount() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(BigDecimal.valueOf(1234.56D));

        final Account account = new Account();
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());
        Mockito.doAnswer(invocation -> new ProductDefinition())
                .when(super.depositAccountManagementServiceSpy).findProductDefinition(tellerTransaction.getProductIdentifier());

        super.testSubject.post(teller.getCode(), tellerTransaction);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Open").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldCloseAccount() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(this.commonAmount);

        final Account account = new Account();
        account.setBalance(this.commonAmount.doubleValue());
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        super.testSubject.post(teller.getCode(), tellerTransaction);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Closed").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldCloseAccountZeroBalance() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(BigDecimal.ZERO);

        final Account account = new Account();
        account.setBalance(0.00D);
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        super.testSubject.post(teller.getCode(), tellerTransaction);
    }

    @Test(expected = TransactionProcessingException.class)
    public void shouldNotCloseAccountRemainingBalance() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(this.commonAmount);

        final Account account = new Account();
        account.setBalance(2000.00D);
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        final TellerTransactionCosts tellerTransactionCosts = super.testSubject.post(teller.getCode(), tellerTransaction);

        super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(), "CONFIRM", null);
    }

    @Test
    public void shouldTransferAccountToAccount() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
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
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(commonAmount);

        final Account customerAccount = new Account();
        customerAccount.setBalance(2000.00D);
        customerAccount.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(customerAccount))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());

        final Account targetAccount = new Account();
        targetAccount.setBalance(2000.00D);
        targetAccount.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(targetAccount))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getTargetAccountIdentifier());

        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        super.testSubject.post(teller.getCode(), tellerTransaction);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/transactions/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Transferred").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldDeposit() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_DEPOSIT);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(commonAmount);

        final Account account = new Account();
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        super.testSubject.post(teller.getCode(), tellerTransaction);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/transactions/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Deposited").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldWithdraw() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(commonAmount);

        final Account account = new Account();
        account.setBalance(2000.00D);
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        super.testSubject.post(teller.getCode(), tellerTransaction);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/transactions/")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Withdrawn").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test(expected = TransactionProcessingException.class)
    public void shouldNotWithdrawLackingBalance() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(BigDecimal.valueOf(5000L));

        final Account account = new Account();
        account.setBalance(2000.00D);
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(tellerTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        final TellerTransactionCosts tellerTransactionCosts = super.testSubject.post(teller.getCode(), tellerTransaction);

        super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(), "CONFIRM", null);
    }

    @Test(expected = TransactionProcessingException.class)
    public void shouldNotWithdrawExceedsCashDrawLimit() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(BigDecimal.valueOf(15000L));

        final Account account = new Account();
        account.setState(Account.State.OPEN.name());
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
            Mockito.doAnswer(invocation -> Optional.of(new Account()))
                    .when(super.accountingServiceSpy).findAccount(Matchers.eq(TestTellerOperation.tellerUnderTest.getChequesReceivableAccount()));
            Mockito.doAnswer(invocation -> Optional.of(new Account()))
                    .when(super.accountingServiceSpy).findAccount(Matchers.eq(TestTellerOperation.tellerUnderTest.getCashOverShortAccount()));

            super.testSubject.create(officeIdentifier, TestTellerOperation.tellerUnderTest);

            Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, TestTellerOperation.tellerUnderTest.getCode()));

            Mockito.verify(this.organizationServiceSpy, Mockito.times(1)).setTellerReference(Matchers.eq(officeIdentifier));

            final TellerManagementCommand command = new TellerManagementCommand();
            command.setAction(TellerManagementCommand.Action.OPEN.name());
            command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
            command.setAssignedEmployeeIdentifier(TEST_USER);

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
        Mockito.doAnswer(invocation -> Optional.of(new Account()))
                .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getChequesReceivableAccount()));
        Mockito.doAnswer(invocation -> Optional.of(new Account()))
                .when(super.accountingServiceSpy).findAccount(Matchers.eq(teller.getCashOverShortAccount()));

        super.testSubject.create(officeIdentifier, teller);

        Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode()));

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);
    }

    @Test(expected = TransactionProcessingException.class)
    public void shouldNotReopenAccountClosed() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction openAccountTransaction =  new TellerTransaction();
        openAccountTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
        openAccountTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        openAccountTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        openAccountTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        openAccountTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        openAccountTransaction.setClerk(TEST_USER);
        openAccountTransaction.setAmount(commonAmount);

        final Account account = new Account();
        account.setState(Account.State.OPEN.name());
        account.setBalance(2000.00D);
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(openAccountTransaction.getCustomerAccountIdentifier());
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.eq(openAccountTransaction));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(openAccountTransaction.getCustomerIdentifier());
        Mockito.doAnswer(invocation -> new ProductDefinition())
                .when(super.depositAccountManagementServiceSpy).findProductDefinition(openAccountTransaction.getProductIdentifier());

        final TellerTransactionCosts openingCosts = super.testSubject.post(teller.getCode(), openAccountTransaction);
        super.testSubject.confirm(teller.getCode(), openingCosts.getTellerTransactionIdentifier(), "CONFIRM", "excluded");
        super.eventRecorder.wait(EventConstants.CONFIRM_TRANSACTION, openingCosts.getTellerTransactionIdentifier());

        final TellerTransaction closeAccountTransaction =  new TellerTransaction();
        closeAccountTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
        closeAccountTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        closeAccountTransaction.setProductIdentifier(openAccountTransaction.getProductIdentifier());
        closeAccountTransaction.setCustomerAccountIdentifier(openAccountTransaction.getCustomerAccountIdentifier());
        closeAccountTransaction.setCustomerIdentifier(openAccountTransaction.getCustomerIdentifier());
        closeAccountTransaction.setClerk(TEST_USER);
        closeAccountTransaction.setAmount(commonAmount);

        account.setBalance(1234.56D);

        final TellerTransactionCosts closingCosts = super.testSubject.post(teller.getCode(), closeAccountTransaction);
        super.testSubject.confirm(teller.getCode(), closingCosts.getTellerTransactionIdentifier(), "CONFIRM", "excluded");
        super.eventRecorder.wait(EventConstants.CONFIRM_TRANSACTION, closingCosts.getTellerTransactionIdentifier());

        account.setState(Account.State.CLOSED.name());

        final TellerTransaction reopenAccountTransaction =  new TellerTransaction();
        reopenAccountTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
        reopenAccountTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        reopenAccountTransaction.setProductIdentifier(openAccountTransaction.getProductIdentifier());
        reopenAccountTransaction.setCustomerAccountIdentifier(openAccountTransaction.getCustomerAccountIdentifier());
        reopenAccountTransaction.setCustomerIdentifier(openAccountTransaction.getCustomerIdentifier());
        reopenAccountTransaction.setClerk(TEST_USER);
        reopenAccountTransaction.setAmount(commonAmount);

        super.testSubject.post(teller.getCode(), reopenAccountTransaction);
    }

    @Test
    public void shouldProcessRepayment() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction repaymentTransaction =  new TellerTransaction();
        repaymentTransaction.setTransactionType(ServiceConstants.TX_REPAYMENT);
        repaymentTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        repaymentTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        repaymentTransaction.setProductCaseIdentifier(RandomStringUtils.randomAlphanumeric(32));
        repaymentTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        repaymentTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        repaymentTransaction.setClerk(TEST_USER);
        repaymentTransaction.setAmount(BigDecimal.valueOf(246.80D));

        final Account account = new Account();
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(repaymentTransaction.getCustomerAccountIdentifier());

        final TellerTransactionCosts tellerTransactionCosts = super.testSubject.post(teller.getCode(), repaymentTransaction);

        super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(), "CONFIRM", null);

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/transactions/" + repaymentTransaction.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Process Repayment").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void shouldProcessCheque() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction chequeTransaction =  new TellerTransaction();
        chequeTransaction.setTransactionType(ServiceConstants.TX_CHEQUE);
        chequeTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        chequeTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setProductCaseIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setClerk(TEST_USER);
        chequeTransaction.setAmount(BigDecimal.valueOf(246.80D));

        final MICR micr = new MICR();
        micr.setChequeNumber("0011");
        micr.setBranchSortCode("08154711");
        micr.setAccountNumber("4711");

        Mockito
                .doAnswer(invocation -> {
                    final Account mockedAccount = new Account();
                    mockedAccount.setBalance(2000.00D);
                    mockedAccount.setState(Account.State.OPEN.name());
                    return Optional.of(mockedAccount);
                })
                .when(super.accountingServiceSpy).findAccount(Matchers.eq(micr.getAccountNumber()));

        final Cheque cheque = new Cheque();
        cheque.setMicr(micr);
        cheque.setDrawee("whatever Bank");
        cheque.setDrawer("Jane Doe");
        cheque.setPayee("John Doe");
        cheque.setDateIssued(DateConverter.toIsoString(LocalDate.now(Clock.systemUTC())));
        cheque.setAmount(BigDecimal.valueOf(246.80D));
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

        super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(),
                "CONFIRM", null);

        Assert.assertTrue(
                super.eventRecorder.wait(EventConstants.CONFIRM_TRANSACTION,
                        tellerTransactionCosts.getTellerTransactionIdentifier())
        );

        this.mockMvc.perform(post(path + "/teller/" + teller.getCode() + "/transactions/" + chequeTransaction.getIdentifier())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(teller.getCode() + " Processed Cheque").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError());
    }

    @Test(expected = TellerTransactionValidationException.class)
    public void shouldNotProcessChequeAlreadyUsed() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction chequeTransaction =  new TellerTransaction();
        chequeTransaction.setTransactionType(ServiceConstants.TX_CHEQUE);
        chequeTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        chequeTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setProductCaseIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        chequeTransaction.setClerk(TEST_USER);
        chequeTransaction.setAmount(BigDecimal.valueOf(246.80D));

        final MICR micr = new MICR();
        micr.setChequeNumber("0012");
        micr.setBranchSortCode("08154711");
        micr.setAccountNumber("4711");

        Mockito
                .doAnswer(invocation -> {
                    final Account mockedAccount = new Account();
                    mockedAccount.setBalance(1000.00D);
                    mockedAccount.setState(Account.State.OPEN.name());
                    return Optional.of(mockedAccount);
                })
                .when(super.accountingServiceSpy).findAccount(Matchers.eq(micr.getAccountNumber()));

        final Cheque cheque = new Cheque();
        cheque.setMicr(micr);
        cheque.setDrawee("whatever Bank");
        cheque.setDrawer("Jane Doe");
        cheque.setPayee("John Doe");
        cheque.setDateIssued(DateConverter.toIsoString(LocalDate.now(Clock.systemUTC())));
        cheque.setAmount(BigDecimal.valueOf(246.80D));
        cheque.setOpenCheque(Boolean.FALSE);
        chequeTransaction.setCheque(cheque);

        Mockito
                .doAnswer(invocation -> {
                    final Account mockedAccount = new Account();
                    mockedAccount.setState(Account.State.OPEN.name());
                    return Optional.of(mockedAccount);
                })
                .when(super.accountingServiceSpy).findAccount(chequeTransaction.getCustomerAccountIdentifier());

        super.testSubject.post(teller.getCode(), chequeTransaction);

        super.testSubject.post(teller.getCode(), chequeTransaction);
    }

    @Test(expected = TransactionProcessingException.class)
    public void shouldNotWithdrawExcludingCharges() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(BigDecimal.valueOf(2000.00D));

        final Account account = new Account();
        account.setBalance(2000.00D);
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());

        final Charge charge = new Charge();
        charge.setAmount(BigDecimal.valueOf(15.00D));
        Mockito.doAnswer(invocation -> Lists.newArrayList(charge))
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.any(TellerTransaction.class));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        final TellerTransactionCosts tellerTransactionCosts = super.testSubject.post(teller.getCode(), tellerTransaction);

        super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(), "CONFIRM", null);
    }

    @Test
    public void shouldWithdrawIncludingCharges() throws Exception {
        final Teller teller = this.prepareTeller();

        final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
        unlockDrawerCommand.setEmployeeIdentifier(TEST_USER);
        unlockDrawerCommand.setPassword(teller.getPassword());

        super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

        super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

        final TellerTransaction tellerTransaction =  new TellerTransaction();
        tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
        tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
        tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
        tellerTransaction.setClerk(TEST_USER);
        tellerTransaction.setAmount(BigDecimal.valueOf(2000.00D));

        final Account account = new Account();
        account.setBalance(2000.00D);
        account.setState(Account.State.OPEN.name());
        Mockito.doAnswer(invocation -> Optional.of(account))
                .when(super.accountingServiceSpy).findAccount(tellerTransaction.getCustomerAccountIdentifier());

        final Charge charge = new Charge();
        charge.setAmount(BigDecimal.valueOf(15.00D));
        Mockito.doAnswer(invocation -> Lists.newArrayList(charge))
                .when(super.depositAccountManagementServiceSpy).getCharges(Matchers.any(TellerTransaction.class));
        Mockito.doAnswer(invocation -> Collections.emptyList())
                .when(super.depositAccountManagementServiceSpy).fetchProductInstances(tellerTransaction.getCustomerIdentifier());

        final TellerTransactionCosts tellerTransactionCosts = super.testSubject.post(teller.getCode(), tellerTransaction);

        super.testSubject.confirm(teller.getCode(), tellerTransactionCosts.getTellerTransactionIdentifier(), "CONFIRM", "included");
    }
}

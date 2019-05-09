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

import org.apache.fineract.cn.teller.api.v1.EventConstants;
import org.apache.fineract.cn.teller.api.v1.client.TellerManager;
import org.apache.fineract.cn.teller.service.internal.service.helper.AccountingService;
import org.apache.fineract.cn.teller.service.internal.service.helper.ChequeService;
import org.apache.fineract.cn.teller.service.internal.service.helper.DepositAccountManagementService;
import org.apache.fineract.cn.teller.service.internal.service.helper.OrganizationService;
import org.apache.fineract.cn.teller.service.internal.service.helper.PortfolioService;
import java.security.interfaces.RSAPrivateKey;
import org.apache.fineract.cn.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import org.apache.fineract.cn.api.context.AutoUserContext;
import org.apache.fineract.cn.lang.ApplicationName;
import org.apache.fineract.cn.test.fixture.TenantDataStoreContextTestRule;
import org.apache.fineract.cn.test.listener.EnableEventRecording;
import org.apache.fineract.cn.test.listener.EventRecorder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {AbstractTellerTest.TestConfiguration.class}
)
public class AbstractTellerTest extends SuiteTestEnvironment {

  public static final String LOGGER_NAME = "test-logger";

  @Configuration
  @EnableEventRecording
  @EnableFeignClients(basePackages = {"org.apache.fineract.cn.teller.api.v1.client"})
  @RibbonClient(name = APP_NAME)
  @Import({TellerConfiguration.class})
  @ComponentScan("org.apache.fineract.cn.teller.listener")
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean(name = LOGGER_NAME)
    public Logger logger() {
      return LoggerFactory.getLogger(LOGGER_NAME);
    }
  }

  static final String TEST_USER = "homer";

  @ClassRule
  public static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer, postgreSQLInitializer);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
      = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment, this::waitForInitialize);

  private AutoUserContext userContext;

  @Autowired
  TellerManager testSubject;

  @Autowired
  EventRecorder eventRecorder;

  @MockBean
  OrganizationService organizationServiceSpy;

  @MockBean
  AccountingService accountingServiceSpy;

  @MockBean
  DepositAccountManagementService depositAccountManagementServiceSpy;

  @MockBean
  PortfolioService portfolioServiceSpy;

  @MockBean
  ChequeService chequeServiceSpy;

  @Autowired
  private ApplicationName applicationName;

  @SuppressWarnings("WeakerAccess")
  @Autowired
  @Qualifier(LOGGER_NAME)
  Logger logger;

  @Before
  public void prepTest() {
    userContext = tenantApplicationSecurityEnvironment.createAutoUserContext(TEST_USER);
    final RSAPrivateKey tenantPrivateKey = tenantApplicationSecurityEnvironment.getSystemSecurityEnvironment().tenantPrivateKey();
    logger.info("tenantPrivateKey = {}", tenantPrivateKey);
  }

  @After
  public void cleanTest() {
    userContext.close();
    eventRecorder.clear();
  }

  public boolean waitForInitialize() {
    try {
      final String version = this.applicationName.getVersionString();
      this.logger.info("Waiting on initialize event for version: {}.", version);
      return this.eventRecorder.wait(EventConstants.INITIALIZE, version);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }
}

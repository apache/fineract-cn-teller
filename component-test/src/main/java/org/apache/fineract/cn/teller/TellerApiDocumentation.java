package org.apache.fineract.cn.teller;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.accounting.api.v1.domain.Account;
import org.apache.fineract.cn.deposit.api.v1.definition.domain.ProductDefinition;
import org.apache.fineract.cn.deposit.api.v1.instance.domain.ProductInstance;
import org.apache.fineract.cn.lang.DateConverter;
import org.apache.fineract.cn.teller.api.v1.EventConstants;
import org.apache.fineract.cn.teller.api.v1.domain.*;
import org.apache.fineract.cn.teller.service.internal.service.helper.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

public class TellerApiDocumentation extends AbstractTellerTest {

  @Rule
  public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/doc/generated-snippets/test-teller");

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @MockBean
  OrganizationService organizationServiceSpy;

  @MockBean
  AccountingService accountingService;

  @MockBean
  DepositAccountManagementService depositAccountManagementServiceSpy;

  @MockBean
  PortfolioService portfolioServiceSpy;

  @MockBean
  ChequeService chequeServiceSpy;

  private static Teller tellerUnderTest = null;
  private final BigDecimal commonAmount = BigDecimal.valueOf(1234.56D);

  @Before
  public void setUp ( ) {

    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(documentationConfiguration(this.restDocumentation))
            .alwaysDo(document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
            .build();
  }

  @Test
  public void documentCreateTeller ( ) throws Exception {

    final String officeIdentifier = "office1234";
    final Teller teller = TellerGenerator.createRandomTeller();

    teller.setCode("1234");
    teller.setPassword("password");
    teller.setTellerAccountIdentifier("TEL123BA");
    teller.setVaultAccountIdentifier("TEL123BA");
    teller.setChequesReceivableAccount("CHA2018XYZ");
    teller.setCashOverShortAccount("CHA2018XYZ");
    teller.setAssignedEmployee("Nakuve Lah");
    teller.setCashdrawLimit(new BigDecimal("5000000"));
    teller.setDenominationRequired(Boolean.FALSE);


    Mockito.doAnswer(invocation -> true)
            .when(this.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
            .when(this.accountingService).findAccount(Matchers.eq(teller.getTellerAccountIdentifier()));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
            .when(this.accountingService).findAccount(Matchers.eq(teller.getVaultAccountIdentifier()));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
            .when(this.accountingService).findAccount(Matchers.eq(teller.getChequesReceivableAccount()));
    Mockito.doAnswer(invocation -> Optional.of(new Account()))
            .when(this.accountingService).findAccount(Matchers.eq(teller.getCashOverShortAccount()));

    Gson gson = new Gson();
    this.mockMvc.perform(post("/offices/" + officeIdentifier + "/teller/")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(teller))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-create-teller", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("code").description("Code"),
                            fieldWithPath("password").description("Password"),
                            fieldWithPath("cashdrawLimit").type("BigDecimal").description("Cash Withdrawal Limit"),
                            fieldWithPath("tellerAccountIdentifier").description("Teller Account Identifier"),
                            fieldWithPath("vaultAccountIdentifier").description("Vault Account Identifier"),
                            fieldWithPath("chequesReceivableAccount").description("Cheques Receivable Account"),
                            fieldWithPath("cashOverShortAccount").description("Cash Over Short Account"),
                            fieldWithPath("denominationRequired").description("Denomination Required"),
                            fieldWithPath("assignedEmployee").description("Assigned Employee")
                    )));
  }

  @Test
  public void documentFindTeller ( ) throws Exception {

    final String officeIdentifier = "office412";
    final Teller teller = TellerGenerator.createRandomTeller();

    teller.setCode("4123");
    teller.setPassword("assward");
    teller.setTellerAccountIdentifier("TEL412AC");
    teller.setVaultAccountIdentifier("TEL412AC");
    teller.setChequesReceivableAccount("CHA2018AB");
    teller.setCashOverShortAccount("CHA2018AB");
    teller.setAssignedEmployee("Chi Ndohah");
    teller.setCashdrawLimit(new BigDecimal("4"));
    teller.setDenominationRequired(Boolean.FALSE);

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

    this.mockMvc.perform(get("/offices/" + officeIdentifier + "/teller/" + teller.getCode())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-find-teller", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("code").description("Code"),
                            fieldWithPath("password").type("String").description("Password"),
                            fieldWithPath("cashdrawLimit").type("BigDecimal").description("Cash Withdrawal Limit"),
                            fieldWithPath("tellerAccountIdentifier").description("Teller Account Identifier"),
                            fieldWithPath("vaultAccountIdentifier").description("Vault Account Identifier"),
                            fieldWithPath("chequesReceivableAccount").description("Cheques Receivable Account"),
                            fieldWithPath("cashOverShortAccount").description("Cash Over Short Account"),
                            fieldWithPath("denominationRequired").description("Denomination Required"),
                            fieldWithPath("assignedEmployee").description("Assigned Employee"),
                            fieldWithPath("state").description(" State of Teller " +
                                    " + \n" +
                                    " *enum* _State_ { + \n" +
                                    "    ACTIVE, + \n" +
                                    "    CLOSED, + \n" +
                                    "    OPEN, + \n" +
                                    "    PAUSED + \n" +
                                    "  }"),
                            fieldWithPath("createdBy").description("Employee who created teller"),
                            fieldWithPath("createdOn").description("Date employee was created"),
                            fieldWithPath("lastModifiedBy").type("String").description("Employee who last modified teller"),
                            fieldWithPath("lastModifiedOn").type("String").description("Date when teller was last modified"),
                            fieldWithPath("lastOpenedBy").type("String").description("Last employee who opened teller"),
                            fieldWithPath("lastOpenedOn").type("String").description("Last time teller was opened")
                    )));
  }

  @Test
  public void documentFetchTellers ( ) throws Exception {

    final String officeIdentifier = "office247";
    final Teller tellerOne = TellerGenerator.createRandomTeller();
    final Teller tellerTwo = TellerGenerator.createRandomTeller();
    List <Teller> tellers = Lists.newArrayList(tellerOne, tellerTwo);

    tellerOne.setCode("412389");
    tellerOne.setPassword(RandomStringUtils.randomAlphabetic(9));
    tellerOne.setTellerAccountIdentifier("TEL412389C");
    tellerOne.setVaultAccountIdentifier("TEL412389C");
    tellerOne.setChequesReceivableAccount("CHA2018ABC");
    tellerOne.setCashOverShortAccount("CHA2018ABC");
    tellerOne.setAssignedEmployee("Chi Ndi");
    tellerOne.setCashdrawLimit(new BigDecimal("4000000"));
    tellerOne.setDenominationRequired(Boolean.FALSE);

    tellerTwo.setCode("512389");
    tellerTwo.setPassword(RandomStringUtils.randomAlphabetic(9));
    tellerTwo.setTellerAccountIdentifier("TEL512389D");
    tellerTwo.setVaultAccountIdentifier("TEL512389D");
    tellerTwo.setChequesReceivableAccount("DHA2018ABD");
    tellerTwo.setCashOverShortAccount("DHA2018ABD");
    tellerTwo.setAssignedEmployee("Chia Chenjo");
    tellerTwo.setCashdrawLimit(new BigDecimal("5000000"));
    tellerTwo.setDenominationRequired(Boolean.FALSE);

    Mockito.doAnswer(invocation -> true)
            .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

    tellers.stream().forEach(tell -> {

      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(this.accountingService).findAccount(Matchers.eq(tell.getTellerAccountIdentifier()));
      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(this.accountingService).findAccount(Matchers.eq(tell.getVaultAccountIdentifier()));
      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(this.accountingService).findAccount(Matchers.eq(tell.getChequesReceivableAccount()));
      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(this.accountingService).findAccount(Matchers.eq(tell.getCashOverShortAccount()));

      super.testSubject.create(officeIdentifier, tell);

      try {
        Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, tell.getCode()));
      } catch (final InterruptedException e) {
        throw new IllegalStateException(e);
      }
    });

    this.mockMvc.perform(get("/offices/" + officeIdentifier + "/teller/")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-tellers", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("[].code").description("Code for first teller "),
                            fieldWithPath("[].password").type("String").description("first teller's Password"),
                            fieldWithPath("[].cashdrawLimit").type("BigDecimal").description("first teller's Cash Withdrawal Limit"),
                            fieldWithPath("[].tellerAccountIdentifier").description("first Teller Account Identifier"),
                            fieldWithPath("[].vaultAccountIdentifier").description("first teller Vault Account Identifier"),
                            fieldWithPath("[].chequesReceivableAccount").description("Cheques Receivable Account"),
                            fieldWithPath("[].cashOverShortAccount").description("Cash Over Short Account"),
                            fieldWithPath("[].denominationRequired").description("first teller's Denomination Required"),
                            fieldWithPath("[].assignedEmployee").description("first teller's Assigned Employee"),
                            fieldWithPath("[].state").description(" State of first Teller " +
                                    " + \n" +
                                    " *enum* _State_ { + \n" +
                                    "    ACTIVE, + \n" +
                                    "    CLOSED, + \n" +
                                    "    OPEN, + \n" +
                                    "    PAUSED + \n" +
                                    "  }"),
                            fieldWithPath("[].createdBy").description("Employee who created teller"),
                            fieldWithPath("[].createdOn").description("Date employee was created"),
                            fieldWithPath("[].lastModifiedBy").type("String").description("Employee who last modified teller"),
                            fieldWithPath("[].lastModifiedOn").type("String").description("Date when teller was last modified"),
                            fieldWithPath("[].lastOpenedBy").type("String").description("Last employee who opened teller"),
                            fieldWithPath("[].lastOpenedOn").type("String").description("Last time teller was opened"),
                            fieldWithPath("[1].code").description("Second teller's Code"),
                            fieldWithPath("[1].password").type("String").description("Second teller's Password"),
                            fieldWithPath("[1].cashdrawLimit").type("BigDecimal").description("Cash Withdrawal Limit"),
                            fieldWithPath("[1].tellerAccountIdentifier").description("Second Teller's Account Identifier"),
                            fieldWithPath("[1].vaultAccountIdentifier").description("Vault Account Identifier"),
                            fieldWithPath("[1].chequesReceivableAccount").description("Cheques Receivable Account"),
                            fieldWithPath("[1].cashOverShortAccount").description("Cash Over Short Account"),
                            fieldWithPath("[1].denominationRequired").description("Denomination Required"),
                            fieldWithPath("[1].assignedEmployee").description("second teller's Assigned Employee"),
                            fieldWithPath("[1].state").description(" State of second Teller " +
                                    " + \n" +
                                    " *enum* _State_ { + \n" +
                                    "    ACTIVE, + \n" +
                                    "    CLOSED, + \n" +
                                    "    OPEN, + \n" +
                                    "    PAUSED + \n" +
                                    "  }"),
                            fieldWithPath("[1].createdBy").description("Employee who created teller"),
                            fieldWithPath("[1].createdOn").description("Date employee was created"),
                            fieldWithPath("[1].lastModifiedBy").type("String").description("Employee who last modified teller"),
                            fieldWithPath("[1].lastModifiedOn").type("String").description("Date when teller was last modified"),
                            fieldWithPath("[1].lastOpenedBy").type("String").description("Last employee who opened teller"),
                            fieldWithPath("[1].lastOpenedOn").type("String").description("Last time teller was opened")
                    )));
  }

  @Test
  public void documentUpdateTeller ( ) throws Exception {

    final String officeIdentifier = "wakanda";
    final Teller teller = TellerGenerator.createRandomTeller();

    teller.setCode("6789");
    teller.setPassword(RandomStringUtils.randomAlphabetic(9));
    teller.setTellerAccountIdentifier("TEL6789Z1");
    teller.setVaultAccountIdentifier("TEL6789Z1");
    teller.setChequesReceivableAccount("ZHX2018ABZ");
    teller.setCashOverShortAccount("ZHX2018ABZ");
    teller.setAssignedEmployee("Dioh Dione");
    teller.setCashdrawLimit(new BigDecimal("5000000"));
    teller.setDenominationRequired(Boolean.TRUE);

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

    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    teller.setCashdrawLimit(BigDecimal.valueOf(15000.00D));

    Gson gson = new Gson();
    this.mockMvc.perform(put("/offices/" + officeIdentifier + "/teller/" + teller.getCode())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(teller))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-update-teller", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("code").description("Code"),
                            fieldWithPath("password").description("Password"),
                            fieldWithPath("cashdrawLimit").type("BigDecimal").description("Cash Withdrawal Limit"),
                            fieldWithPath("tellerAccountIdentifier").description("Teller Account Identifier"),
                            fieldWithPath("vaultAccountIdentifier").description("Vault Account Identifier"),
                            fieldWithPath("chequesReceivableAccount").description("Cheques Receivable Account"),
                            fieldWithPath("cashOverShortAccount").description("Cash Over Short Account"),
                            fieldWithPath("denominationRequired").description("Denomination Required"),
                            fieldWithPath("assignedEmployee").description("Assigned employee")
                    )));
  }

  @Test
  public void documentOpenTeller ( ) throws Exception {

    final String officeIdentifier = "moritavo";
    final Teller teller = TellerGenerator.createRandomTeller();
    teller.setCode("689");

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
    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    final TellerManagementCommand open = new TellerManagementCommand();
    open.setAction(TellerManagementCommand.Action.OPEN.name());
    open.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    open.setAssignedEmployeeIdentifier("Ashu");

    Mockito.doAnswer(invocation -> true)
            .when(super.organizationServiceSpy).employeeExists(Matchers.eq(open.getAssignedEmployeeIdentifier()));

    Gson gson = new Gson();
    this.mockMvc.perform(post("/offices/" + officeIdentifier + "/teller/" + teller.getCode() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(open))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-open-teller", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action " +
                                    " + \n " +
                                    " *enum* _Action_ { + \n" +
                                    "    OPEN, + \n" +
                                    "    CLOSE + \n" +
                                    "  }"),
                            fieldWithPath("adjustment").description("Adjustment " +
                                    "*enum* _Adjustment_ { + \n" +
                                    "    NONE, + \n" +
                                    "    DEBIT, + \n" +
                                    "    CREDIT + \n" +
                                    "  } + \n" +
                                    ""),
                            fieldWithPath("assignedEmployeeIdentifier").type("String").optional().description("Teller Account Identifier")
                    )));
  }

  @Test
  public void documentCloseTeller ( ) throws Exception {

    final String officeIdentifier = "antananarivo";
    final Teller teller = TellerGenerator.createRandomTeller();
    teller.setCode("7239");
    teller.setState(Teller.State.OPEN.name());

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
    super.eventRecorder.wait(EventConstants.POST_TELLER, teller.getCode());

    final TellerManagementCommand close = new TellerManagementCommand();
    close.setAction(TellerManagementCommand.Action.CLOSE.name());
    close.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    close.setAssignedEmployeeIdentifier("Antah");

    final TellerManagementCommand open = new TellerManagementCommand();
    open.setAction(TellerManagementCommand.Action.OPEN.name());
    open.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
    open.setAssignedEmployeeIdentifier("Antah");

    Mockito.doAnswer(invocation -> true)
            .when(super.organizationServiceSpy).employeeExists(Matchers.eq(close.getAssignedEmployeeIdentifier()));

    super.testSubject.post(officeIdentifier, teller.getCode(), open);
    Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, teller.getCode()));

    Gson gson = new Gson();
    this.mockMvc.perform(post("/offices/" + officeIdentifier + "/teller/" + teller.getCode() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(close))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-close-teller", preprocessRequest(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action " +
                                    " + \n " +
                                    " *enum* _Action_ { + \n" +
                                    "    OPEN, + \n" +
                                    "    CLOSE + \n" +
                                    "  }"),
                            fieldWithPath("adjustment").description("Adjustment " +
                                    "*enum* _Adjustment_ { + \n" +
                                    "    NONE, + \n" +
                                    "    DEBIT, + \n" +
                                    "    CREDIT + \n" +
                                    "  } + \n" +
                                    ""),
                            fieldWithPath("assignedEmployeeIdentifier").type("String").optional().description("Teller Account Identifier")
                    )));
  }

  @Test
  public void documentPauseTeller ( ) throws Exception {

    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    this.mockMvc.perform(post("/teller/" + teller.getCode())
            .param("command", "PAUSE")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-pause-teller"));
  }

  @Test
  public void documentGetBalance ( ) throws Exception {

    final String officeIdentifier = "fesse";
    final Teller teller = TellerGenerator.createRandomTeller();
    teller.setCode("kombone987");
    teller.setCashdrawLimit(BigDecimal.valueOf(10000.00D));

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

    this.mockMvc.perform(get("/offices/" + officeIdentifier + "/teller/" + teller.getCode() + "/balance")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-get-balance", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("day").type("String").description("Code"),
                            fieldWithPath("cashOnHand").type("BigDecimal").description("Password"),
                            fieldWithPath("cashReceivedTotal").type("BigDecimal").description("Cash Withdrawal Limit"),
                            fieldWithPath("cashDisbursedTotal").type("BigDecimal").description("Teller Account Identifier"),
                            fieldWithPath("chequesReceivedTotal").type("BigDecimal").description("Vault Account Identifier"),
                            fieldWithPath("cashEntries").type("List<TellerEntry>").description("Cheques Receivable Account"),
                            fieldWithPath("chequeEntries").type("List<TellerEntry>").description("Cash Over Short Account")
                    )));
  }

  @Test
  public void documentDeleteTeller ( ) throws Exception {

    final String officeIdentifier = "kakeOne";
    final Teller teller = TellerGenerator.createRandomTeller();
    teller.setCode("9876");
    teller.setCashdrawLimit(BigDecimal.valueOf(10000.00D));

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

    this.mockMvc.perform(delete("/offices/" + officeIdentifier + "/teller/" + teller.getCode())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-delete-teller"));
  }

  @Test
  public void documentUnlockDrawer ( ) throws Exception {

    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    Gson gson = new Gson();
    this.mockMvc.perform(post("/teller/" + teller.getCode() + "/drawer")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(unlockDrawerCommand))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-unlock-drawer", preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("code").description("Teller Code"),
                            fieldWithPath("password").type("String").description("Password to unlock drawer"),
                            fieldWithPath("cashdrawLimit").type("BigDecimal").description("Cash draw limit"),
                            fieldWithPath("tellerAccountIdentifier").description("Teller Account Identifier"),
                            fieldWithPath("vaultAccountIdentifier").description("Vault account identifier"),
                            fieldWithPath("chequesReceivableAccount").description("Cheques receivables account"),
                            fieldWithPath("cashOverShortAccount").description("Cash Over/Short Account"),
                            fieldWithPath("denominationRequired").description("Denomination Required ?"),
                            fieldWithPath("assignedEmployee").description("Assigned Employee"),
                            fieldWithPath("state").description(" State of Teller " +
                                    " + \n" +
                                    " *enum* _State_ { + \n" +
                                    "    ACTIVE, + \n" +
                                    "    CLOSED, + \n" +
                                    "    OPEN, + \n" +
                                    "    PAUSED + \n" +
                                    "  }"),
                            fieldWithPath("createdBy").description("Employee who created teller"),
                            fieldWithPath("createdOn").description("Date employee was created"),
                            fieldWithPath("lastModifiedBy").type("String").description("Employee who last modified teller"),
                            fieldWithPath("lastModifiedOn").type("String").description("Date when teller was last modified"),
                            fieldWithPath("lastOpenedBy").type("String").description("Last employee who opened teller"),
                            fieldWithPath("lastOpenedOn").type("String").description("Last time teller was opened")
                    )));
  }

  @Test
  public void documentOpenAccount ( ) throws Exception {

    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction = new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_OPEN_ACCOUNT);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier("product101");
    tellerTransaction.setCustomerAccountIdentifier("Customer001");
    tellerTransaction.setCustomerIdentifier("CustomerOne");
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
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

    Gson gson = new Gson();
    this.mockMvc.perform(post("/teller/" + teller.getCode() + "/transactions")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(tellerTransaction))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-open-account", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("transactionType").description("Transaction type"),
                            fieldWithPath("transactionDate").description("Transaction Date"),
                            fieldWithPath("customerIdentifier").type("String").optional().description("Customer Identifier"),
                            fieldWithPath("productIdentifier").description("Product identifier"),
                            fieldWithPath("customerAccountIdentifier").description("Customer's account"),
                            fieldWithPath("clerk").description("Clerk's name"),
                            fieldWithPath("amount").type("BigDecimal").description("Amount in account")
                    ),
                    responseFields(
                            fieldWithPath("tellerTransactionIdentifier").type("String").description("Teller transaction"),
                            fieldWithPath("totalAmount").type("BigDecimal").description("Total Amount"),
                            fieldWithPath("charges").type("List<Charge>").description("List of Charges")
                    )));
  }

  @Test
  public void documentCloseAccount ( ) throws Exception {

    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction = new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CLOSE_ACCOUNT);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier("product102");
    tellerTransaction.setCustomerAccountIdentifier("Customer002");
    tellerTransaction.setCustomerIdentifier("CustomerTwo");
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
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

    Gson gson = new Gson();
    this.mockMvc.perform(post("/teller/" + teller.getCode() + "/transactions")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(tellerTransaction))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-close-account", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("transactionType").description("Transaction type"),
                            fieldWithPath("transactionDate").description("Transaction Date"),
                            fieldWithPath("customerIdentifier").type("String").optional().description("Customer Identifier"),
                            fieldWithPath("productIdentifier").description("Product identifier"),
                            fieldWithPath("customerAccountIdentifier").description("Customer's account"),
                            fieldWithPath("clerk").description("Clerk's name"),
                            fieldWithPath("amount").type("BigDecimal").description("Amount in account")
                    ),
                    responseFields(
                            fieldWithPath("tellerTransactionIdentifier").type("String").description("Teller transaction"),
                            fieldWithPath("totalAmount").type("BigDecimal").description("Total Amount"),
                            fieldWithPath("charges").type("List<Charge>").description("List of Charges")
                    )));
  }

  @Test
  public void documentConfirmTransaction ( ) throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction = new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
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

    this.mockMvc.perform(post("/teller/" + teller.getCode() + "/transactions/" + tellerTransactionCosts.getTellerTransactionIdentifier())
            .param("command", "CONFIRM")
            .param("charges", "included")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-confirm-transaction"));
  }

  @Test
  public void documentCancelTransaction ( ) throws Exception {
    final Teller teller = this.prepareTeller();

    final UnlockDrawerCommand unlockDrawerCommand = new UnlockDrawerCommand();
    unlockDrawerCommand.setEmployeeIdentifier(AbstractTellerTest.TEST_USER);
    unlockDrawerCommand.setPassword(teller.getPassword());

    super.testSubject.unlockDrawer(teller.getCode(), unlockDrawerCommand);

    super.eventRecorder.wait(EventConstants.AUTHENTICATE_TELLER, teller.getCode());

    final TellerTransaction tellerTransaction = new TellerTransaction();
    tellerTransaction.setTransactionType(ServiceConstants.TX_CASH_WITHDRAWAL);
    tellerTransaction.setTransactionDate(DateConverter.toIsoString(LocalDateTime.now(Clock.systemUTC())));
    tellerTransaction.setProductIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerAccountIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setCustomerIdentifier(RandomStringUtils.randomAlphanumeric(32));
    tellerTransaction.setClerk(AbstractTellerTest.TEST_USER);
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

    this.mockMvc.perform(post("/teller/" + teller.getCode() + "/transactions/" + tellerTransactionCosts.getTellerTransactionIdentifier())
            .param("command", "CANCEL")
            .param("charges", "excluded")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-cancel-transaction"));
  }

  private Teller prepareTeller ( ) throws Exception {
    if (this.tellerUnderTest == null) {
      final String officeIdentifier = "office" + RandomStringUtils.randomAlphabetic(3);
      this.tellerUnderTest = TellerGenerator.createRandomTeller();
      tellerUnderTest.setPassword(RandomStringUtils.randomAlphanumeric(12));

      final String telAccIdentifier = "telAcc" + RandomStringUtils.randomAlphabetic(3);
      tellerUnderTest.setTellerAccountIdentifier(telAccIdentifier);

      final String vaulAccIdentifier = "telAcc" + RandomStringUtils.randomAlphabetic(3);
      tellerUnderTest.setVaultAccountIdentifier(vaulAccIdentifier);

      final String cheqRecAccIdentifier = "cheqRec" + RandomStringUtils.randomAlphabetic(3);
      tellerUnderTest.setChequesReceivableAccount(cheqRecAccIdentifier);

      final String cashOSAccIdentifier = "cashOSAcc" + RandomStringUtils.randomAlphabetic(3);
      tellerUnderTest.setCashOverShortAccount(cashOSAccIdentifier);

      final String tellerId = "teller" + RandomStringUtils.randomAlphabetic(3);
      tellerUnderTest.setCode(tellerId);

      Mockito.doAnswer(invocation -> true)
              .when(super.organizationServiceSpy).officeExists(Matchers.eq(officeIdentifier));

      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(super.accountingServiceSpy).findAccount(Matchers.eq(this.tellerUnderTest.getTellerAccountIdentifier()));
      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(super.accountingServiceSpy).findAccount(Matchers.eq(this.tellerUnderTest.getVaultAccountIdentifier()));
      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(super.accountingServiceSpy).findAccount(Matchers.eq(this.tellerUnderTest.getChequesReceivableAccount()));
      Mockito.doAnswer(invocation -> Optional.of(new Account()))
              .when(super.accountingServiceSpy).findAccount(Matchers.eq(this.tellerUnderTest.getCashOverShortAccount()));

      super.testSubject.create(officeIdentifier, this.tellerUnderTest);

      Assert.assertTrue(super.eventRecorder.wait(EventConstants.POST_TELLER, this.tellerUnderTest.getCode()));

      Mockito.verify(this.organizationServiceSpy, Mockito.times(1)).setTellerReference(Matchers.eq(officeIdentifier));

      final TellerManagementCommand command = new TellerManagementCommand();
      command.setAction(TellerManagementCommand.Action.OPEN.name());
      command.setAdjustment(TellerManagementCommand.Adjustment.NONE.name());
      command.setAssignedEmployeeIdentifier(AbstractTellerTest.TEST_USER);

      Mockito.doAnswer(invocation -> true)
              .when(super.organizationServiceSpy).employeeExists(Matchers.eq(command.getAssignedEmployeeIdentifier()));

      super.testSubject.post(officeIdentifier, this.tellerUnderTest.getCode(), command);

      Assert.assertTrue(super.eventRecorder.wait(EventConstants.OPEN_TELLER, this.tellerUnderTest.getCode()));
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

    return this.tellerUnderTest;
  }
}

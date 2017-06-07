package io.mifos.teller.client;

import io.mifos.core.api.annotation.ThrowsException;
import io.mifos.core.api.annotation.ThrowsExceptions;
import io.mifos.core.api.util.CustomFeignClientsConfiguration;
import io.mifos.teller.domain.Teller;
import io.mifos.teller.domain.TellerAuthentication;
import io.mifos.teller.domain.TellerBalanceSheet;
import io.mifos.teller.domain.TellerManagementCommand;
import io.mifos.teller.domain.TellerTransaction;
import io.mifos.teller.domain.TellerTransactionCosts;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

@FeignClient(value = "teller-v1", path = "/teller/v1", configuration = CustomFeignClientsConfiguration.class)
public interface TellerManager {

  @RequestMapping(
      value = "/offices/{officeIdentifier}",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = TellerValidationException.class),
      @ThrowsException(status = HttpStatus.CONFLICT, exception = TellerAlreadyExistsException.class)
  })
  void create(@PathVariable("officeIdentifier") final String officeIdentifier, @RequestBody @Valid final Teller teller);

  @RequestMapping(
      value = "/offices/{officeIdentifier}/teller/{tellerCode}",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.ALL_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class)
  })
  Teller find(@PathVariable("officeIdentifier") final String officeIdentifier,
              @PathVariable("tellerCode") final String tellerCode);

  @RequestMapping(
      value = "/offices/{officeIdentifier}/teller",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.ALL_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class)
  })
  List<Teller> fetch(@PathVariable("officeIdentifier") final String officeIdentifier);

  @RequestMapping(
      value = "/offices/{officeIdentifier}/teller/{tellerCode}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = TellerValidationException.class)
  })
  void change(@PathVariable("officeIdentifier") final String officeIdentifier,
              @PathVariable("tellerCode") final String tellerCode,
              @RequestBody @Valid final Teller teller);

  @RequestMapping(
      value = "/offices/{officeIdentifier}/teller/{tellerCode}/commands",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = TellerValidationException.class)
  })
  void post(@PathVariable("officeIdentifier") final String officeIdentifier,
            @PathVariable("tellerCode") final String tellerCode,
            @RequestBody @Valid final TellerManagementCommand tellerManagementCommand);

  @RequestMapping(
      value = "/offices/{officeIdentifier}/teller/{tellerCode}/balance",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.ALL_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class)
  })
  TellerBalanceSheet getBalance(@PathVariable("officeIdentifier") final String officeIdentifier,
                                @PathVariable("tellerCode") final String tellerCode);

  @RequestMapping(
      value = "/teller/{tellerCode}/auth",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = TellerNotFoundException.class)
  })
  void auth(@PathVariable("tellerCode") final String tellerCode,
            @RequestBody @Valid final TellerAuthentication tellerAuthentication);

  @RequestMapping(
      value = "/teller/{tellerCode}",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class)
  })
  void post(@PathVariable("tellerCode") final String tellerCode,
            @RequestParam(value = "command", required = true) final String command);

  @RequestMapping(
      value = "/teller/{tellerCode}/transactions",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = TellerTransactionValidationException.class)
  })
  TellerTransactionCosts post(@PathVariable("tellerCode") final String tellerCode,
                              @RequestBody @Valid final TellerTransaction tellerTransaction);

  @RequestMapping(
      value = "/teller/{tellerCode}/transactions/{identifier}",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class)
  })
  void confirm(@PathVariable("tellerCode") final String tellerCode,
               @PathVariable("identifier") final String tellerTransactionIdentifier,
               @RequestParam(value = "command", required = true) final String command);

  @RequestMapping(
      value = "/teller/{tellerCode}/transactions",
      method = RequestMethod.GET,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.ALL_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = TellerNotFoundException.class)
  })
  List<TellerTransaction> fetch(@PathVariable("tellerCode") final String tellerCode,
                                @RequestParam(value = "status", required = false) final String status);
}

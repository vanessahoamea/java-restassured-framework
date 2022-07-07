package com.apitesting.stepDefinitions;

import static org.assertj.core.api.Assertions.assertThat;

import com.apitesting.models.common.ErrorList;
import com.apitesting.models.common.ErrorMessage;
import com.apitesting.models.common.ErrorType;
import com.apitesting.persistence.World;
import com.apitesting.utils.JsonSchemaUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.Assert;

public class CommonSteps {
  private static final String ERROR_SCHEMA = "errorSchema.json";

  World world = World.getWorld();

  @Given("I'm new on GoRest API")
  public void doNothing() {}

  @Then("^the received status code is (.*)$")
  public void checkStatus(int statusCode) {
    Assert.assertEquals(
        "Expected status code: "
            + statusCode
            + " doesn't match the actual one: "
            + world.getResponse().statusCode(),
        statusCode,
        world.getResponse().statusCode());
  }

  @Then("^I receive \\w* (.*) error$")
  public void verifyErrorMessage(ErrorType errorType) {
    ErrorList errorList = world.getErrorList();

    assertThat(errorList)
        .usingRecursiveComparison()
        .isEqualTo(ErrorList.getExpectedError(errorType));
  }

  @Then("^I receive (.*) error message$")
  public void verifyErrorMessage(String errorMessage) {
    ErrorMessage errorMsg = world.getResponse().as(ErrorMessage.class);

    assertThat(errorMsg.getMessage()).isEqualTo(errorMessage);
  }

  @Then("the error response JSON schema matches the one expected")
  public void validateErrorResponseJSONSchema() {
    JsonSchemaUtils.validateJsonSchema(world.getResponse(), ERROR_SCHEMA);
  }
}


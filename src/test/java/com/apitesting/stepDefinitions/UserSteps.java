package com.apitesting.stepDefinitions;

import static com.apitesting.utils.RandomValuesGenerator.generateRandomEmail;
import static org.assertj.core.api.Assertions.assertThat;

import com.apitesting.models.common.ErrorList;
import com.apitesting.models.common.Error;
import com.apitesting.models.users.UserRequestModel;
import com.apitesting.models.users.UserResponseModel;
import com.apitesting.persistence.World;
import com.apitesting.requests.GenericRetryClient;
import com.apitesting.requests.user.UserRequest;
import com.apitesting.utils.JsonSchemaUtils;
import com.apitesting.utils.ReflectionUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jetty.http.HttpStatus;

public class UserSteps {

  private static final String USER_REGISTRATION_SCHEMA = "userRegistrationSchema.json";
  private static final String USER_UPDATE_SCHEMA = "userUpdateSchema.json";
  private static final String USER_PATCH_SCHEMA = "userPatchSchema.json";

  World world = World.getWorld();
  private final SoftAssertions softly = new SoftAssertions();

  Response response;
  UserRequestModel userRequestObject;

  // POST/PUT/PATCH user response
  UserResponseModel userResponse;

  @Given("I'm an already GoRest registered user")
  @When("I create a new GoRest user")
  public void createGoRestUser(DataTable userTable) {
    Map<String, String> userDetails = userTable.asMaps().get(0);

    buildUserObject(userDetails);
    response = new UserRequest().postUser(userRequestObject);
    setUserRequestAndResponseForLaterUse();
  }

  @When("I try to create a new GoRest user with invalid data")
  public void tryToCreateGoRestUser(DataTable invalidUserTable) {
    Map<String, String> invalidUserDetails = invalidUserTable.asMaps().get(0);

    if(invalidUserDetails.get("email").equals("@random"))
      buildUserObject(invalidUserDetails);
    else
      buildUserObject(invalidUserDetails, true);

    response =
        new GenericRetryClient<>()
            .waitForStatusCode(
                () -> new UserRequest().postUser(userRequestObject),
                HttpStatus.UNPROCESSABLE_ENTITY_422);

    world.setResponse(response);
    ErrorList errorList = new ErrorList(Arrays.asList(response.as(Error[].class)));
    world.setErrorList(errorList);
  }

  @When("I update the GoRest user with the following details")
  public void updateGoRestUser(DataTable updateUserTable) {
    Map<String, String> userDetails = updateUserTable.asMaps().get(0);
    int userIdToBeUpdated = userResponse.getId();

    buildUserObject(userDetails);
    response = new UserRequest().updateUser(userIdToBeUpdated, userRequestObject);
    setUserRequestAndResponseForLaterUse();
  }

  @When("I try to update the GoRest user with invalid data")
  public void tryToUpdateGoRestUser(DataTable invalidUpdateUserTable) {
    Map<String, String> invalidUserDetails = invalidUpdateUserTable.asMaps().get(0);
    int userIdToBeUpdated = userResponse.getId();

    if(invalidUserDetails.get("email").equals("@random"))
      buildUserObject(invalidUserDetails);
    else
      buildUserObject(invalidUserDetails, true);

    response =
            new GenericRetryClient<>()
                    .waitForStatusCode(
                            () -> new UserRequest().updateUser(userIdToBeUpdated, userRequestObject),
                            HttpStatus.UNPROCESSABLE_ENTITY_422);

    world.setResponse(response);
    ErrorList errorList = new ErrorList(Arrays.asList(response.as(Error[].class)));
    world.setErrorList(errorList);
  }

  @When("I try to update an invalid GoRest user with the following details")
  public void updateGoRestInvalidUser(DataTable updateUserTable) {
    Map<String, String> userDetails = updateUserTable.asMaps().get(0);
    int invalidUserId = Integer.parseInt(userDetails.get("id"));

    buildUserObject(userDetails);
    response = new UserRequest().updateUser(invalidUserId, userRequestObject);

    world.setResponse(response);
  }

  private void buildUserObject(Map<String, String> user) {
    buildUserObject(user, false);
  }

  private void buildUserObject(Map<String, String> user, boolean userEmail) {
    userRequestObject =
            UserRequestModel.builder()
                    .name(user.get("name"))
                    .gender(user.get("gender"))
                    .email(userEmail ? user.get("email") : generateRandomEmail())
                    .status(user.get("status"))
                    .build();
  }

  private void setUserRequestAndResponseForLaterUse() {
    world.setUserRequest(userRequestObject);
    world.setResponse(response);

    userResponse = response.as(UserResponseModel.class);
  }

  @When("I patch the GoRest user with the following details")
  public void patchGoRestUserWithSpecificDetails(DataTable patchUserTable) {
    Map<String, String> userDetails = patchUserTable.asMaps().get(0);
    int userIdToBeUpdated = userResponse.getId();
    UserRequestModel patchedUserRequestObject = new UserRequestModel();

    for (Entry<String, String> entry : userDetails.entrySet()) {
      if (Objects.equals(entry.getKey(), "email")) {
        String randomEmail = generateRandomEmail();
        ReflectionUtils.setField(patchedUserRequestObject, "email", randomEmail);
        ReflectionUtils.setField(userRequestObject, "email", randomEmail);
      } else {
        ReflectionUtils.setField(patchedUserRequestObject, entry.getKey(), entry.getValue());
        ReflectionUtils.setField(userRequestObject, entry.getKey(), entry.getValue());
      }
    }

    response = new UserRequest().patchUser(userIdToBeUpdated, patchedUserRequestObject);
    setUserRequestAndResponseForLaterUse();
  }

  @When("I delete my GoRest user")
  public void deleteGoRestUser() {
    world.setResponse(new UserRequest().deleteUser(userResponse.getId()));
  }

  @When("I try to delete an invalid GoRest user with the following ID")
  public void deleteGoRestInvalidUser(DataTable updateUserTable) {
    Map<String, String> userDetails = updateUserTable.asMaps().get(0);
    int invalidUserId = Integer.parseInt(userDetails.get("id"));

    response = new UserRequest().deleteUser(invalidUserId);

    world.setResponse(response);
  }

  @Then("the user response is successfully validated")
  public void validateUserResponse() {
    softly.assertThat(Objects.equals(userRequestObject.getEmail(), userResponse.getEmail()))
        .as("The user registration email should match the data sent with the request")
        .isTrue();
    softly.assertThat(Objects.equals(userRequestObject.getName(), userResponse.getName()))
        .as("The user registration name should match the data sent with the request")
        .isTrue();
    softly.assertThat(Objects.equals(userRequestObject.getGender(), userResponse.getGender()))
        .as("The user registration gender should match the data sent with the request")
        .isTrue();
    softly.assertThat(Objects.equals(userRequestObject.getStatus(), userResponse.getStatus()))
        .as("The user registration status should match the data sent with the request")
        .isTrue();

    softly.assertAll();
  }

  @Then("the get user endpoint response reflects the changes")
  public void theGetUserEndpointResponseReflectsTheChanges() {
    UserResponseModel getUserResponse =
        new UserRequest().getUserById(userResponse.getId()).as(UserResponseModel.class);

    assertThat(Objects.equals(userResponse, getUserResponse))
        .as("The user registration response and the get user response must be the same")
        .isTrue();
  }

  @Then("^the get user endpoint returns (.*) after the user deletion$")
  public void theGetUserEndpointReturnsNotFoundAfterTheUserDeletion(int statusCode) {
    world.setResponse(new UserRequest().getUserById(userResponse.getId()));
    new CommonSteps().checkStatus(statusCode);
  }

  // JSON schemas validation steps

  @Then("the user registration response JSON schema matches the one expected")
  public void validateUserRegistrationResponseJSONSchema() {
    JsonSchemaUtils.validateJsonSchema(response, USER_REGISTRATION_SCHEMA);
  }

  @Then("the update user response JSON schema matches the one expected")
  public void validateUpdateUserResponseJSONSchema() {
    JsonSchemaUtils.validateJsonSchema(response, USER_UPDATE_SCHEMA);
  }

  @Then("the patch user response JSON schema matches the one expected")
  public void validatePatchUserResponseJSONSchema() {
    JsonSchemaUtils.validateJsonSchema(response, USER_PATCH_SCHEMA);
  }
}
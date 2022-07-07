@regression @GoRest
Feature: GoRest users endpoints tests
  For documentation about the API please check: https://gorest.co.in

  Scenario Outline: GoRest user registration flow
    Given I'm new on GoRest API
    When I create a new GoRest user
      | name   | gender   | email   | status   |
      | <name> | <gender> | <email> | <status> |
    Then the received status code is 201
    And the user registration response JSON schema matches the one expected
    And the user response is successfully validated
    And the get user endpoint response reflects the changes

    Examples:
      | name   | gender | email   | status   |
      | Radu   | male   | @random | active   |
      | Iulian | female | @random | inactive |

  Scenario: GoRest user update flow
    Given I'm an already GoRest registered user
      | name   | gender | email   | status |
      | Radu   | male   | @random | active |
    When I update the GoRest user with the following details
      | name   | gender | email   | status   |
      | Iulian | female | @random | inactive |
    Then the received status code is 200
    And the update user response JSON schema matches the one expected
    And the user response is successfully validated
    And the get user endpoint response reflects the changes

  Scenario: GoRest user patch flow
    Given I'm an already GoRest registered user
      | name   | gender | email   | status |
      | Radu   | male   | @random | active |
    When I patch the GoRest user with the following details
      | name   | email   |
      | Iulian | @random |
    Then the received status code is 200
    And the patch user response JSON schema matches the one expected
    And the user response is successfully validated
    And the get user endpoint response reflects the changes

  Scenario: GoRest delete user flow
    Given I'm an already GoRest registered user
      | name | gender | email   | status |
      | Radu | male   | @random | active |
    When I delete my GoRest user
    Then the received status code is 204
    And the get user endpoint returns 404 after the user deletion

  ########################### Negative tests ###########################
  @Test
  Scenario: [N] GoRest user registration flow with invalid gender
    Given I'm new on GoRest API
    When I try to create a new GoRest user with invalid data
      | name | gender            | email   | status |
      | Radu | notMaleOrFemale   | @random | active |
    And the error response JSON schema matches the one expected
    Then I receive a GO_REST_GENDER_MALE_FEMALE error

  @Test
  Scenario: [N] GoRest user registration flow with invalid status
    Given I'm new on GoRest API
    When I try to create a new GoRest user with invalid data
      | name | gender | email   | status  |
      | Radu | male   | @random | offline |
    And the error response JSON schema matches the one expected
    Then I receive a GO_REST_INVALID_STATUS error

  @Test
  Scenario: [N] GoRest user registration flow with existing email
    Given I'm an already GoRest registered user
      | name | gender | email                        | status |
      | Radu | male   | radu.pregatit.2022@gmail.com | active |
    When I try to create a new GoRest user with invalid data
      | name | gender | email                        | status |
      | Radu | male   | radu.pregatit.2022@gmail.com | active |
    And the error response JSON schema matches the one expected
    Then I receive a GO_REST_EXISTING_EMAIL error

  @Test
  Scenario: [N] GoRest user update flow with invalid gender
    Given I'm an already GoRest registered user
      | name   | gender | email   | status |
      | Radu   | male   | @random | active |
    When I try to update the GoRest user with invalid data
      | name | gender          | email   | status |
      | Radu | notMaleOrFemale | @random | active |
    And the error response JSON schema matches the one expected
    Then I receive a GO_REST_GENDER_MALE_FEMALE error

  @Test
  Scenario: [N] GoRest user update flow with invalid status
    Given I'm an already GoRest registered user
      | name   | gender | email   | status |
      | Radu   | male   | @random | active |
    When I try to update the GoRest user with invalid data
      | name | gender | email   | status  |
      | Radu | female | @random | offline |
    And the error response JSON schema matches the one expected
    Then I receive a GO_REST_INVALID_STATUS error

  @Test
  Scenario: [N] GoRest update flow with existing email
    Given I'm an already GoRest registered user
      | name    | gender | email              | status |
      | Vanessa | female | oldemail@gmail.com | active |
    When I try to update the GoRest user with invalid data
      | name    | gender | email                     | status |
      | Mihaela | female | bhattacharya_chitrangada@lang.com | active |
    And the error response JSON schema matches the one expected
    Then I receive a GO_REST_EXISTING_EMAIL error

  @Test
  Scenario: [N] GoRest user update flow for invalid user
    When I try to update an invalid GoRest user with the following details
      | id    | name   | gender | email   | status  |
      | 99999 | Iulian | female | @random | offline |
    Then the received status code is 404
    And I receive Resource not found error message

  @Test
  Scenario: [N] GoRest delete user flow for invalid user
    When I try to delete an invalid GoRest user with the following ID
      | id    |
      | 99999 |
    Then the received status code is 404
    And I receive Resource not found error message
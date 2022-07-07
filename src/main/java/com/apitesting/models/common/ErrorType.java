package com.apitesting.models.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ErrorType {

  GO_REST_BLANK_USER_NAME("name", "can't be blank"),
  GO_REST_BLANK_USER_GENDER("gender", "can't be blank"),
  GO_REST_BLANK_USER_EMAIL("email", "can't be blank"),
  GO_REST_BLANK_USER_STATUS("status", "can't be blank"),
  GO_REST_GENDER_MALE_FEMALE("gender", "gender cannot have other values than male or female"), //more detailed error message
  GO_REST_INVALID_STATUS("status", "status cannot have other values than active or inactive"), //more detailed error message
  GO_REST_EXISTING_EMAIL("email", "has already been taken");

  private String field;
  private String message;

}

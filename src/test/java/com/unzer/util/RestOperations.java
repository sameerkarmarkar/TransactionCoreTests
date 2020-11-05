package com.unzer.util;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class RestOperations {

    public static void withUrlEncoding(boolean encoding){
        RestAssured.urlEncodingEnabled = encoding;
    }

    public static Response sendPostRequest(String endpoint, Map<String, String> parameters) {
        RequestSpecification spec = new RequestSpecBuilder().setBaseUri(endpoint).build();
        return RestAssured.given().relaxedHTTPSValidation()
                .log().all().and().spec(spec)
                .and().queryParams(parameters)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }

    public static Response sendPostRequest(String endpoint, Map<String, String> parameters, ContentType contentType) {
        RequestSpecification spec = new RequestSpecBuilder().setBaseUri(endpoint).build();
        return RestAssured.given().relaxedHTTPSValidation().contentType(contentType)
                .log().all().and().spec(spec)
                .and().queryParams(parameters)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }

    public static Response post(RequestSpecification requestSpec, Map parameters) {
        return RestAssured
                .given().log().all()
                .and().spec(requestSpec)
                .and().formParams(parameters)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }

    public static Response postWithQueryParameters(RequestSpecification requestSpec, Map parameters) {
        return RestAssured
                .given().log().all().with().relaxedHTTPSValidation()
                .and().spec(requestSpec)
                .and().queryParams(parameters)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }

    public static Response post(String url) {
        return RestAssured.given().log().all().relaxedHTTPSValidation().baseUri(url).when().get().then().log().all().and().extract().response();
    }

    public static Response get(String url) {
        return RestAssured.given().log().all().when().get(url).then().log().all().and().extract().response();
    }



}

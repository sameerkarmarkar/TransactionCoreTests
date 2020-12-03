package com.unzer.clients;

import com.unzer.util.Configuration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import static com.unzer.util.Formatter.*;

@Slf4j
public class CoreClient {

    public static final CoreClient INSTANCE = new CoreClient();

    private static final Configuration config = Configuration.INSTANCE;
    private static final QName _Request_QNAME = new QName("", "Request");


    @SneakyThrows
    public ResponseType send(RequestType requestType) {
        log.info("sending {} request sent to transaction core", requestType.getTransaction().getPayment().getCode());
        JAXBElement<RequestType> jaxbRequestType = new JAXBElement(_Request_QNAME, RequestType.class, (Class) null, requestType);
        String requestString = marshall(jaxbRequestType);

        Response restResponse = RestAssured.given().log().all().relaxedHTTPSValidation()
                .filter(AllureRestAssured())
                .filter(RequestLoggingFilter())
                .filter(ResponseLoggingFilter())
                .baseUri(config.getProperty("core.url"))
                .with().formParam("load", requestString)
                .when().post().then().extract().response();

        String responseString = restResponse.asString();
        ResponseType response = unmarshal(responseString).getValue();

        log.info("response status -> {} --- response code -> {}",
                response.getTransaction().getProcessing().getStatus().getValue(), response.getTransaction().getProcessing().getStatus().getCode());
        return response;
    }

    private AllureRestAssured AllureRestAssured() {
        return new AllureRestAssured();
    }

    private RequestLoggingFilter RequestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    private ResponseLoggingFilter ResponseLoggingFilter() {
        return new ResponseLoggingFilter();
    }


}

package com.unzer.util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import sun.net.www.http.HttpClient;

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
        Response responseRedurect = RestAssured.given().log().all().relaxedHTTPSValidation()
                .baseUri(config.getProperty("core.url")).with().formParam("load", requestString).when().post().then().log().all().and().extract().response();
        String responseString = responseRedurect.asString();
        ResponseType response = unmarshal(responseString).getValue();
        log.info("response status -> {} --- response code -> {}",
                response.getTransaction().getProcessing().getStatus().getValue(), response.getTransaction().getProcessing().getStatus().getCode());
        return response;
    }


}

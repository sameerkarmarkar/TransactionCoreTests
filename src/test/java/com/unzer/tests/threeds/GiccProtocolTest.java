package com.unzer.tests.threeds;

import com.unzer.constants.*;
import com.unzer.util.Flow;
import com.unzer.util.GiccVerifier;
import com.unzer.util.RequestBuilder;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.unzer.constants.Constants.DESTINATION;
import static com.unzer.util.Formatter.marshall;
import static com.unzer.util.Formatter.unmarshal;

public class GiccProtocolTest {

    private static final GiccVerifier GICC_VERIFIER = GiccVerifier.INSTANCE;
    private static final QName _Request_QNAME = new QName("", "Request");

    private static String termUrl, md, paReq;
    private static String acsUrl = "https://3ds-acs.test.modirum.com/mdpayacs/pareq";
    private static String acsPareqUrl = "https://3ds-acs.test.modirum.com/mdpayacs/pareq;mdsessionid=43E546BA51D75650309D54DBB366DC72";
    private static String sessionUrl;
    private static String pares;
    private static Logger log = Logger.getAnonymousLogger();


    private static final RequestSpecification acsReqSpec = new RequestSpecBuilder()
            .setBaseUri(acsUrl)
            .build();

    private static final RequestSpecification acsPareqSpec = new RequestSpecBuilder()
            .setBaseUri(acsPareqUrl)
            .build();


    @ParameterizedTest(name = "{0}")
    @MethodSource("oneOffTransactions")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForOneOffThreedsTwoTransaction(String description, Flow flow) {
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        RequestType request = flow.getLastTransactionRequest();
        String shortId = response.getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForOneOff(request.getTransaction().getAccount().getBrand());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("returningCustomer")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForReturningCustomerThreedsTwoTransaction(String description, Flow flow) {
        flow.execute();
        RequestType request = flow.getLastTransactionRequest();
        ResponseType response = flow.getLastTransactionResponse();
        ResponseType parentResponse = flow.getParentResponse();
        String shortId = response.getTransaction().getIdentification().getShortID();
        String parentShortId = parentResponse.getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortId).getMessage().and().verifyFieldsForReturningCustomer(request.getTransaction().getAccount().getBrand(), parentShortId);
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledInitialRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds();
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledInitialRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForUnscheduledRepeatedRecurringTransaction(Card card, Merchant merchant) {
        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(card)
                .then().debit().withResponseUrl().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.INITIAL).asThreeds()
                .then().debit().withResponseUrl().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.REPEATED).asThreeds();
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledSubsequentRecurring(card.getCardBrand());
    }

    @ParameterizedTest
    @MethodSource("cards")
    @SneakyThrows
    public void shouldSendCorrectGiccMessageForScheduledRecurringThreedsTwoTransaction(Card card, Merchant merchant) {

        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(card)
                .then().debit().referringToNth(TransactionType.REGISTRATION).and().withRecurringIndicator(Recurrence.INITIAL).withResponseUrl().and().asThreeds()
                .then().schedule().withSchedule(TransactionType.DEBIT).referringToNth(TransactionType.REGISTRATION);
        flow.execute();

        String shortID = flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID();
        GICC_VERIFIER.withShortId(shortID).getMessage().and().verifyFieldsForUnscheduledInitialRecurring(card.getCardBrand());
    }

    @Test
    public void threedsOneFlow() {

        RequestType preauthorization = RequestBuilder.newRequest().withDefaultTransactionInfo()
                .and().withMerchant(Merchant.SIX_THREEDS_ONE_MERCHANT)
                .and().withCard(Card.MASTERCARD).and().withAmount("100").and().withCurrency("EUR")
                .and().withTransactionType(TransactionType.PREAUTHORIZATION).and().and().withResponseUrl().build();

        ResponseType response = execute(preauthorization);
        String shortId = response.getTransaction().getIdentification().getShortID();
        termUrl = response.getTransaction().getProcessing().getRedirect().getParameter().get(0).getValue();
        md = response.getTransaction().getProcessing().getRedirect().getParameter().get(1).getValue();
        paReq = response.getTransaction().getProcessing().getRedirect().getParameter().get(2).getValue();
        sendPareq();
        enterPassword();
        sendPares(shortId);

        /*VERIFIER.withShortId(shortId).getMessage().and().verifyField("22", "812");
        VERIFIER.withShortId(shortId).getMessage().and().verifySubField("72", "1");
        //VERIFIER.withShortId(shortId).getMessage().and().verifySubField("73", "1");
        VERIFIER.withShortId(shortId).getMessage().and().verifySubField("63", "jMhMvWU2Pyj4CBoASZ1HBTUAAAA");*/

        RequestType capture = RequestBuilder.newRequest().withDefaultTransactionInfo()
                .and().withMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT)
                .and().withCard(Card.MASTERCARD).and().withAmount("100").and().withCurrency("EUR")
                .and().referringTo(response)
                .and().withTransactionType(TransactionType.CAPTURE).build();

        ResponseType captureresponse = execute(capture);
        String shortIdCapture = captureresponse.getTransaction().getIdentification().getShortID();

        /*VERIFIER.withShortId(shortIdCapture).getMessage().and().verifyField("22", "102");
        VERIFIER.withShortId(shortIdCapture).getMessage().and().verifySubField("72", "1");
        //VERIFIER.withShortId(shortIdCapture).getMessage().and().verifySubField("73", "1");
        VERIFIER.withShortId(shortIdCapture).getMessage().and().verifySubField("63", "jMhMvWU2Pyj4CBoASZ1HBTUAAAA");*/

/*        VERIFIER.withShortId(shortIdCapture).getMessage().and().verifyField("15","812");
        VERIFIER.withShortId(shortIdCapture).getMessage().and().verifyField("61","812");*/

    }

    /*@SneakyThrows
    @Test
    public void shouldSendCorrectValuesInGiccMessageForStandaloneTransaction() {

        RequestType preauthorization = RequestBuilder.newRequest().withDefaultTransactionInfo()
                .and().withMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT)
                .and().withCard(Card.MASTERCARD).and().withAmount("100").and().withCurrency("EUR")
                .and().withTransactionType(TransactionType.PREAUTH).and().and().withResponseUrl().build();

        ResponseType response = execute(preauthorization);
        String shortId = response.getTransaction().getIdentification().getShortID();

        sendThreedsTwoPares(response);

        RequestType capture = RequestBuilder.newRequest().withDefaultTransactionInfo()
                .and().withMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT)
                .and().withCard(Card.MASTERCARD).and().withAmount("100").and().withCurrency("EUR")
                .and().referringTo(response)
                .and().withTransactionType(TransactionType.CAPTURE).build();

        ResponseType captureresponse = execute(capture);
        String shortIdCapture = captureresponse.getTransaction().getIdentification().getShortID();

        VERIFIER.withShortId(shortIdCapture).getMessage().and().verifyFields();

    }*/


    private void sendPareq() {

        RequestSpecification acsReqSpec = new RequestSpecBuilder()
                .setBaseUri(acsUrl)
                .build();

        Map parameters = new HashMap<String, String>();
        parameters.put("PaReq", paReq);
        parameters.put("TermUrl", termUrl);
        parameters.put("MD", md);

        Response response = sendPostRequest(acsReqSpec, parameters);
        String start = "<FORM id=\"form\" name=\"form\" method=\"post\" action=\"";
        String end = "\">\n" +
                "\t\t\t\t\t<DL>";
        sessionUrl = response.asString().substring(response.asString().indexOf(start), response.asString().indexOf(end)).replace(start, "");
        System.out.println("Pareq sent successfully");

    }

    @SneakyThrows
    private void enterPassword() {
        Map parameters = new HashMap<String, String>();
        parameters.put("password", "secret3");
        parameters.put("submit", "Submit");

        RestAssured.urlEncodingEnabled = false;
        RequestSpecification acsReqSpec = new RequestSpecBuilder()
                .setBaseUri(sessionUrl)
                .build();

        Response response = sendPasswordRequest(acsReqSpec, parameters);
        String responseBody = response.asString();
        String start = "name=\"PaRes\"";
        String end = "3dsinput";
        String temp1 = responseBody.substring(responseBody.indexOf(start));
        String temp2 = temp1.substring(0, temp1.indexOf(end));
        pares = temp2.replace(start, "").replace("\"", "").replace("value=", "").trim();
    }

    private void sendPares(String shortId) {
        RestAssured.urlEncodingEnabled = true;
        Map queryParameters = new HashMap<String, String>();
        queryParameters.put("PaRes", pares);
        queryParameters.put("MD", md);
        Response response = RestAssured.given().relaxedHTTPSValidation().baseUri(termUrl)
                .and().queryParams(queryParameters)
                .when().post().then().log().all().and().extract().response();

        System.out.println("Pares Send successfully. Short Id:" + shortId);
    }

    public static Response sendPasswordRequest(RequestSpecification requestSpec, Map requestBody) {
        RestAssured.urlEncodingEnabled = false;
        return RestAssured.given()
                .log().all().and().spec(requestSpec)
                .and().queryParams(requestBody)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }

    public static Response sendPostRequest(RequestSpecification requestSpec, Map requestBody) {
        return RestAssured
                .given().contentType(ContentType.URLENC).log().all()
                .and().spec(requestSpec)
                .and().formParams(requestBody)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }




    @SneakyThrows
    private ResponseType execute(RequestType requestType) {
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(DESTINATION);
        JAXBElement<RequestType> jaxbRequestType = new JAXBElement(_Request_QNAME, RequestType.class, (Class) null, requestType);
        String xmlOutput = marshall(jaxbRequestType);
        method.setParameter("load", xmlOutput);
        int statusCode = client.executeMethod(method);
        JAXBElement<ResponseType> response = unmarshal(method.getResponseBodyAsString());
        log.info("info-------Short id-"+response.getValue().getTransaction().getIdentification().getShortID());
        return response.getValue();
    }

    @SneakyThrows
    private void sendThreedsTwoPares(ResponseType response) {
        String redirectUrl = response.getTransaction().getProcessing().getRedirect().getUrl();
        RestAssured.urlEncodingEnabled = true;
        Response responseRedurect = RestAssured.given().log().all().relaxedHTTPSValidation().baseUri(redirectUrl).when().get().then().log().all().and().extract().response();
        String redirectString = responseRedurect.asString();
        String start = "href=\"";
        String temp1 = redirectString.substring(redirectString.indexOf(start));
        String demoUrl = temp1.substring(0, temp1.indexOf("\">Click here")).replace(start, "");
        Response responseFinal = RestAssured.given().log().all().when().get(demoUrl).then().log().all().and().extract().response();
    }


    private static Stream<Arguments> oneOffTransactions() {
        return Stream.of(
                Arguments.of("One off preauth with Mstercard",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().preauthorization().withCard(Card.MASTERCARD).asThreeds().withResponseUrl()),
                Arguments.of("One off preauth with Visa",
                        Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().debit().withCard(Card.MASTERCARD).asThreeds().withResponseUrl())
        );
    }

    private static Stream<Arguments> returningCustomer() {
        return Stream.of(
                Arguments.of("DEBIT >> REFUND with MASTERCARD", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().debit().withCard(Card.MASTERCARD).asThreeds().withResponseUrl()
                        .then().refund().referringToNth(TransactionType.DEBIT)),
                Arguments.of("REGISTER >> DEBIT >> REFUND with VISA", Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT).startWith().register().withCard(Card.VISA)
                        .then().debit().referringToNth(TransactionType.REGISTRATION).asThreeds().withResponseUrl()
                        .then().refund().referringToNth(TransactionType.DEBIT))
        );
    }

    private static Stream<Arguments> cards() {
        return Stream.of(
                Arguments.of(Card.MASTERCARD, Merchant.SIX_THREEDS_TWO_MERCHANT),
                Arguments.of(Card.VISA, Merchant.SIX_THREEDS_TWO_MERCHANT)
        );
    }

}

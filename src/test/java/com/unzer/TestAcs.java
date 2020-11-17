package com.unzer;

import com.unzer.util.Formatter;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import java.util.HashMap;
import java.util.Map;


public class TestAcs {

    private static String paReq;
    private static String termUrl;
    private static String md;
    private static String acsUrl = "https://3ds-acs.test.modirum.com/mdpayacs/pareq";
    private static String acsPareqUrl = "https://3ds-acs.test.modirum.com/mdpayacs/pareq;mdsessionid=43E546BA51D75650309D54DBB366DC72";
    private static String sessionUrl;
    private static String pares;
    private static String responseParameter;
    private static String shortId;


    private static String txnCoreEndpoint = "https://int-heidelpay.hpcgw.net/TransactionCore/xml";

    private static final RequestSpecification acsReqSpec = new RequestSpecBuilder()
            .setBaseUri(acsUrl)
            .build();

    private static final RequestSpecification acsPareqSpec = new RequestSpecBuilder()
            .setBaseUri(acsPareqUrl)
            .build();

    @Test
    public void sendThreedsTransaction() {
        TestAcs testAcs = new TestAcs();
        testAcs.sendPreauthorization();
        testAcs.sendPareq();
        testAcs.enterPassword();
        testAcs.sendPares();

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

    public static Response sendPasswordRequest(RequestSpecification requestSpec, Map requestBody) {
        RestAssured.urlEncodingEnabled = false;
        return RestAssured.given()
                .log().all().and().spec(requestSpec)
                .and().queryParams(requestBody)
                .when().post()
                .then().log().all()
                .and().extract().response();
    }

    @SneakyThrows
    private void sendPreauthorization () {
        String xmlRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Request>\n" +
                "    <Header>\n" +
                "        <Security sender=\"31HA07BC8102D566343D9FB34BD7D5EE\"/>\n" +
                "    </Header>\n" +
                "    <Transaction mode=\"INTEGRATOR_TEST\" response=\"SYNC\" channel=\"31HA07BC810C26F59B5B15B6FA085418\">\n" +
                "        <User login=\"31ha07bc8102d566343d379bcfb1e3a6\" pwd=\"19EC0D85\"/>\n" +
                "        <Payment code=\"CC.PA\">\n" +
                "            <Presentation>\n" +
                "                <Amount>100</Amount>\n" +
                "                <Currency>EUR</Currency>\n" +
                "                <Usage>6aySdKe8sM</Usage>\n" +
                "            </Presentation>\n" +
                "        </Payment>\n" +
                "        <Account>\n" +
                "            <Holder>Test Account</Holder>\n" +
                "            <Number>5453010000059543</Number>\n" +
                "            <Brand>MASTER</Brand>\n" +
                "            <Expiry month=\"11\" year=\"2024\"/>\n" +
                "            <Verification>123</Verification>\n" +
                "        </Account>\n" +
                "        <Customer>\n" +
                "            <Name>\n" +
                "                <Given>Test</Given>\n" +
                "                <Family>Account</Family>\n" +
                "            </Name>\n" +
                "            <Address>\n" +
                "                <Street>Grasbrunn</Street>\n" +
                "                <Zip>85630</Zip>\n" +
                "                <City>Munich</City>\n" +
                "                <Country>DE</Country>\n" +
                "            </Address>\n" +
                "            <Contact>\n" +
                "                <Email>test@unzer.com</Email>\n" +
                "                <Ip>12.12.12.12</Ip>\n" +
                "            </Contact>\n" +
                "        </Customer>\n" +
                "        <CredentialOnFile>\n" +
                "            <Initiation>MIT</Initiation> \n" +
                "        </CredentialOnFile>\n" +
                "        <Frontend>\n" +
                "            <Enabled>false</Enabled>\n" +
                "            <Mode>DEFAULT</Mode>    \n" +
                " <ResponseUrl>https://web.dev.hpchd.loc/testtool/response;jsessionid=F10438C50B5E4985731E4F6085DBF2B5.testtools01</ResponseUrl>\n" +
                "        </Frontend>\n" +
                "    </Transaction>\n" +
                "</Request>\n";

        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(txnCoreEndpoint);
        QName _Request_QNAME = new QName("", "Request");
        method.setParameter("load", xmlRequest);
        client.executeMethod(method);
        JAXBElement<ResponseType> response = Formatter.unmarshal(method.getResponseBodyAsString());
        termUrl = response.getValue().getTransaction().getProcessing().getRedirect().getParameter().get(0).getValue();
        md = response.getValue().getTransaction().getProcessing().getRedirect().getParameter().get(1).getValue();
        paReq = response.getValue().getTransaction().getProcessing().getRedirect().getParameter().get(2).getValue();
        shortId = response.getValue().getTransaction().getIdentification().getShortID();

    }

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
        sessionUrl = response.asString().substring(response.asString().indexOf(start),response.asString().indexOf(end)).replace(start,"");
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
        pares = temp2.replace(start,"").replace("\"","").replace("value=","").trim();
    }

    private void sendPares() {
        RestAssured.urlEncodingEnabled = true;
        Map queryParameters = new HashMap<String, String>();
        queryParameters.put("PaRes", pares);
        queryParameters.put("MD",md);
        Response response = RestAssured.given().log().all().relaxedHTTPSValidation().baseUri(termUrl)
                .and().queryParams(queryParameters)
                .when().post().then().log().all().and().extract().response();

        System.out.println("Pares Send successfully. Short Id:"+ shortId);

    }

    private void clickContinue() {

    }
}

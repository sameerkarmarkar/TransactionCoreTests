package com.unzer.clients;

import com.unzer.constants.ThreedsVersion;
import com.unzer.util.Configuration;
import com.unzer.util.RestOperations;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AcsClient {
    private static Configuration config = Configuration.INSTANCE;
    private static final String THREEDS_ONE_ACS_URL = config.getProperty("threeds.one.pareq.endpoint");
    private ThreedsVersion version;
    private ResponseType response;

    public static AcsClient forVersion(ThreedsVersion version) {
        AcsClient acsClient = new AcsClient();
        acsClient.version = version;
        return acsClient;
    }

    public void processThreedsAuthorization(ResponseType response) {
        this.response = response;
        if (version.equals(ThreedsVersion.VERSION_1)) {
            processVersionOneAuthorization();
        } else {
            processVersionTwoAutorization();
        }
    }

    private void processVersionOneAuthorization() {
        sendPares(getPares(getSessionUrl()));
    }

    private void processVersionTwoAutorization() {
        String redirectUrl = response.getTransaction().getProcessing().getRedirect().getUrl();
        RestAssured.urlEncodingEnabled = true;
        Response responseRedurect = RestOperations.post(redirectUrl);

        String redirectString = responseRedurect.asString();
        String start = "href=\"";
        String temp1 = redirectString.substring(redirectString.indexOf(start));
        String demoUrl = temp1.substring(0, temp1.indexOf("\">Click here")).replace(start, "");

        Response threedsResponse = RestOperations.get(demoUrl);
        log.info("Threeds authorization completed. response code->{}", threedsResponse.getStatusCode());
    }

    private String getSessionUrl() {
        String termUrl = response.getTransaction().getProcessing().getRedirect().getParameter().get(0).getValue();
        String md = response.getTransaction().getProcessing().getRedirect().getParameter().get(1).getValue();
        String paReq = response.getTransaction().getProcessing().getRedirect().getParameter().get(2).getValue();

        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(THREEDS_ONE_ACS_URL)
                .setRelaxedHTTPSValidation()
                .build();

        Map parameters = new HashMap<String, String>();
        parameters.put("PaReq", paReq);
        parameters.put("TermUrl", termUrl);
        parameters.put("MD", md);

        Response response = RestOperations.post(spec, parameters);
        Document document = Jsoup.parse(response.asString());
        String sessionUrl = document.select("form#form").first().attr("action");
        log.info("threeds one session url is {}", sessionUrl);
        return sessionUrl;
    }

    private String getpassword(String sessionUrl) {
        RequestSpecification acsReqSpec = new RequestSpecBuilder()
                .setUrlEncodingEnabled(false)
                .setBaseUri(sessionUrl)
                .build();
        Response response = RestOperations.get(acsReqSpec);
        Document document = Jsoup.parse(response.asString());
        return document.select("dt:contains(Personal) + dd").first().text();
    }

    private String getPares(String sessionUrl) {
        String password = getpassword(sessionUrl);

        Map parameters = new HashMap<String, String>();
        parameters.put("password", password);
        parameters.put("submit", "Submit");

        RequestSpecification acsReqSpec = new RequestSpecBuilder()
                .setUrlEncodingEnabled(false)
                .setBaseUri(sessionUrl)
                .build();

        Response response = RestOperations.post(acsReqSpec, parameters);

        String responseBody = response.asString();
        Document document = Jsoup.parse(responseBody);
        String pares = document.select("input[name='PaRes']").val();
        log.info("threeds pares is {}", pares);
        return pares;
    }

    private void sendPares(String pares) {
        String termUrl = response.getTransaction().getProcessing().getRedirect().getParameter().get(0).getValue();
        String md = response.getTransaction().getProcessing().getRedirect().getParameter().get(1).getValue();

        RequestSpecification acsReqSpec = new RequestSpecBuilder()
                .setBaseUri(termUrl)
                .setUrlEncodingEnabled(true)
                .build();

        Map queryParameters = new HashMap<String, String>();
        queryParameters.put("PaRes", pares);
        queryParameters.put("MD", md);

        Response response = RestOperations.postWithQueryParameters(acsReqSpec, queryParameters);
        log.info("Threeds authorization completed. response code->{}", response.getStatusCode());
    }
}

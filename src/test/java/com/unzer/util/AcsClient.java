package com.unzer.util;

import com.unzer.constants.ThreedsVersion;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.ResponseType;

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
}

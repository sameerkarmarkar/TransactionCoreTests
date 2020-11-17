package com.unzer;

import com.unzer.constants.Card;
import com.unzer.constants.Merchant;
import com.unzer.constants.TransactionCode;
import com.unzer.util.Flow;
import lombok.SneakyThrows;

import org.junit.jupiter.api.Test;

public class SampleTest {
    @Test
    @SneakyThrows
    public void sampleTest() {

        Flow flow = Flow.forMerchant(Merchant.SIX_THREEDS_TWO_MERCHANT)
                .startWith().preauthorization().withCard(Card.MASTERCARD_1).withResponseUrl().asThreeds()
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION);

        flow.execute();


        //TransactionFlow transactionFlow = TransactionFlow.startWith().preauthorization().execute();

        /*QName _Request_QNAME = new QName("", "Request");
        RequestType requestType = RequestBuilder.newRequest().withSenderId("31HA07BC8102D566343D9FB34BD7D5EE")
                .and().withDefaultTransactionInfo()
                .and().withChannel("31HA07BC810B5D4DFF1C7D6F406BBD52").and().withUser("31ha07bc8102d566343d379bcfb1e3a6", "19EC0D85")
                .build();

        System.out.println("Request type:"+requestType);
        JAXBElement<RequestType> jaxbRequestType = new JAXBElement(_Request_QNAME, RequestType.class, (Class)null, requestType);
        String xmlOutput = marshall(jaxbRequestType);


        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(DESTINATION);
        method.setParameter("load", xmlOutput);

        int statusCode = client.executeMethod(method);

        if (statusCode == HttpStatus.SC_OK) {
            System.out.println("SUCCESS");
        }

        JAXBElement<ResponseType> response = unmarshal(method.getResponseBodyAsString());
        assertEquals(response.getValue().getTransaction().getProcessing().getResult(), "ACK");*/

        //JAXBElement<ResponseType> response = transactionFlow.getLastTransactionResponse();
        //assertEquals(response.getValue().getTransaction().getProcessing().getResult(), "ACK");


       /* GiccVerifier giccVerifier = new GiccVerifier();
        giccVerifier.withShortId("4676.5760.9705").verifyField("1","0100");*/

        flow.getExecutedTransactions();


    }
}

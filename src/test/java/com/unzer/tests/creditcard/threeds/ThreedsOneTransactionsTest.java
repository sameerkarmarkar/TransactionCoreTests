package com.unzer.tests.creditcard.threeds;

import com.unzer.constants.*;
import com.unzer.util.Flow;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static com.unzer.matchers.ProcessingResponseMatches.processingResponseMatches;

//TODO : Need to add the tests here
//This is a placeholder for adding testcases for threeds 2.0 workflows
public class ThreedsOneTransactionsTest {

    private static Merchant merchant = Merchant.SIX_THREEDS_ONE_MERCHANT_1;

    @Test
    public void shouldRejectTransactionWithoutCard() {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().register().withCard(Card.MASTERCARD_1)
                .then().debit().referringToNth(TransactionCode.REGISTERATION).withResponseUrl().asThreeds(ThreedsVersion.VERSION_1);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
        assertThat("Transaction processing status is invalid", response.getTransaction().getProcessing(),
                processingResponseMatches(TransactionProcessing.SUCCESSFUL));
    }

    @Test
    public void shouldProcessThreedsPreauthorization() {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withCard(Card.VISA_1).withResponseUrl().asThreeds(ThreedsVersion.VERSION_1);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
    }

    @Test
    public void shouldProcessThreedsFullCapture() {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withCard(Card.VISA_1).withResponseUrl().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION);
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
    }

    @Test
    public void shouldProcessThreedsPartialCapture() {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withCard(Card.VISA_1).withAmount("100").withResponseUrl().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50");
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
    }

    @Test
    public void shouldProcessThreedsMultiplePartialCapture() {
        Flow flow = Flow.forMerchant(merchant).withPaymentMethod(PaymentMethod.CREDITCARD)
                .startWith().preauthorization().withCard(Card.VISA_1).withAmount("100").withResponseUrl().asThreeds(ThreedsVersion.VERSION_1)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("50")
                .then().capture().withAmount("10").referringToNth(TransactionCode.PREAUTHORIZATION)
                .then().capture().referringToNth(TransactionCode.PREAUTHORIZATION).withAmount("20");
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();
    }

}

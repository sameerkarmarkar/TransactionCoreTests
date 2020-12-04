package com.unzer.tests.creditcard.threeds.kalixa;

import com.unzer.tests.BaseTest;
import com.unzer.util.Flow;
import org.junit.jupiter.api.Test;

import static com.unzer.constants.Card.MASTERCARD_1;
import static com.unzer.constants.PaymentMethod.CREDITCARD;
import static com.unzer.constants.Merchant.KALIXA_NON_THREED_MERCHANT;
import static com.unzer.constants.Merchant.KALIXA_THREEDS_ONE_MERCHANT;
import static com.unzer.constants.Merchant.KALIXA_THREEDS_TWO_MERCHANT;
import static com.unzer.constants.ThreedsVersion.*;
import static com.unzer.constants.TransactionCode.*;

public class SuccessfulRebillTest implements BaseTest {

    @Test
    public void shouldProcessRebillAfterFullCapture() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("50")
                .then().capture().referringToNth(PREAUTHORIZATION)
                .and().withAmount("50")
                .then().rebill().referringToNth(CAPTURE)
                .and().withAmount("20").execute();

    }

    @Test
    public void shouldProcessRebillAfterPartialCapture() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().preauthorization().referringToNth(REGISTERATION)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("2.25")
                .then().rebill().referringToNth(CAPTURE).withAmount("5")
                .execute();
    }

    @Test
    public void shouldProcessRebillAfterDebit() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("10.25")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2).execute();

        flow.continueWith().rebill().referringToNth(DEBIT).withAmount("10.25").execute();
        flow.continueWith().rebill().referringToNth(DEBIT).withAmount("1.25").execute();

    }

    @Test
    public void shouldProcessMultipleRebillsReferringToCapture() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("10.25")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("10").execute();

        flow.continueWith().rebill().referringToNth(CAPTURE).withAmount("10.25").execute();
        flow.continueWith().rebill().referringToNth(CAPTURE).withAmount("10.25").execute();


    }

    @Test
    public void shouldProcessRebillAfterRefundedCapture() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("10.25")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("10")
                .then().refund().referringToNth(CAPTURE).withAmount("10").execute();

        flow.continueWith().rebill().referringToNth(CAPTURE).withAmount("10.25").execute();

    }

    @Test
    public void shouldProcessRebillAfterRefundedDebit() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("10.25")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .then().refund().referringToNth(DEBIT).withAmount("5.20").execute();

        flow.continueWith().rebill().referringToNth(DEBIT).withAmount("10.25").execute();
        flow.continueWith().rebill().referringToNth(DEBIT).withAmount("1.25").execute();
    }
}

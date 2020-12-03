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

public class SuccessfulRefundTest implements BaseTest {

    @Test
    public void shouldProcessFullRefundForNonThreedsDebit() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("5.29")
                .execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("5.29").execute();

    }

    @Test
    public void shouldProcessFullRefundForThreedsDebit() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .execute();

        flow.continueWith().refund().referringToNth(DEBIT).withAmount("10").execute();

    }

    @Test
    public void shouldProcessFullRefundForCofDebit() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().debit().referringToNth(REGISTERATION)
                .and().withAmount("1.35")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .execute();

        flow.continueWith().refund().referringToNth(DEBIT).withAmount("1.35").execute();

    }

    @Test
    public void shouldProcessMultiplePartialRefundsForNonThreedsDebit() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("5.29")
                .execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("2.25").execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("2.00").execute();
    }

    @Test
    public void shouldProcessMultiplePartialRefundsForThreedsDebit() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .execute();

        flow.continueWith().refund().referringToNth(DEBIT).withAmount("5").execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("3.25").execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("1.75").execute();

    }

    @Test
    public void shouldProcessMultiplePartialRefundsForCofDebit() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().debit().referringToNth(REGISTERATION)
                .and().withAmount("5")
                .execute();

        flow.continueWith().refund().referringToNth(DEBIT).withAmount("1.25").execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("1.25").execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("1.25").execute();
        flow.continueWith().refund().referringToNth(DEBIT).withAmount("1.25").execute();

    }

    @Test
    public void shouldProcessFullRefundForCapture() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("5.29")
                .then().capture().referringToNth(PREAUTHORIZATION)
                .withAmount("5.29").execute();

        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("5.29").execute();

    }

    @Test
    public void shouldProcessMultiplePartialRefundsForCapture() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("10")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("10").execute();

        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("2.5").execute();
        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("1.35").execute();
        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("5").execute();
    }

    @Test
    public void shouldProcessFullRefundForPartialCapture() {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().preauthorization().referringToNth(REGISTERATION)
                .and().withAmount("5")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("4.5").execute();

        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("4.5").execute();
    }

    @Test
    public void shouldProcessPartialRefundForPartialCapture() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .then().preauthorization().referringToNth(REGISTERATION)
                .and().withAmount("5")
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("4.5").execute();

        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("2.25").execute();

    }

    @Test
    public void shouldProcessFullRefundForRebill() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .then().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("5")
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("4.5")
                .then().rebill().referringToNth(CAPTURE).withAmount("5").execute();

        flow.continueWith().refund().referringToNth(REBILL).withAmount("5").execute();
    }

    @Test
    public void shouldProcessPartialRefundForRebill() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("5")
                .then().rebill().referringToNth(DEBIT).withAmount("5").execute();

        flow.continueWith().refund().referringToNth(REBILL).withAmount("1.35").execute();
        flow.continueWith().refund().referringToNth(REBILL).withAmount("2").execute();
    }

    @Test
    public void shouldProcessRefundForDebitWithRebill() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withAmount("5")
                .then().rebill().referringToNth(DEBIT).withAmount("4.5")
                .then().rebill().referringToNth(DEBIT).withAmount("5").execute();

        flow.continueWith().refund().referringToNth(DEBIT).withAmount("5").execute();
    }

    @Test
    public void shouldProcessRefundForCaptureWithRebill() {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().preauthorization().withCard(MASTERCARD_1)
                .and().withAmount("5")
                .then().capture().referringToNth(PREAUTHORIZATION).withAmount("3")
                .then().rebill().referringToNth(CAPTURE).withAmount("4.5").execute();

        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("1.25").execute();
        flow.continueWith().refund().referringToNth(CAPTURE).withAmount("1.75").execute();

    }

}

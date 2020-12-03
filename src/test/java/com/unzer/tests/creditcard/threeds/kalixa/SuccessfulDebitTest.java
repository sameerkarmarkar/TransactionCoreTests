package com.unzer.tests.creditcard.threeds.kalixa;

import com.unzer.tests.BaseTest;
import com.unzer.util.Flow;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.unzer.constants.PaymentMethod.CREDITCARD;
import static com.unzer.constants.Merchant.KALIXA_NON_THREED_MERCHANT;
import static com.unzer.constants.Merchant.KALIXA_THREEDS_ONE_MERCHANT;
import static com.unzer.constants.Merchant.KALIXA_THREEDS_TWO_MERCHANT;
import static com.unzer.constants.Card.*;
import static com.unzer.constants.ThreedsVersion.*;
import static com.unzer.constants.TransactionCode.*;

public class SuccessfulDebitTest implements BaseTest {

    @ParameterizedTest(name = "Kalixa non threeds debit with source type {0}")
    @ValueSource(strings = {"XML", "POST"})
    public void shouldProcessNonThreedsdebit(String source) {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withSource(source)
                .and().withAmount("1.35")
                .execute();

    }

    @ParameterizedTest(name = "Kalixa threeds V1 debit with source type {0}")
    @ValueSource(strings = {"XML", "POST"})
    public void shouldProcessThreedsOnedebit(String source) {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withSource(source)
                .and().withAmount("1.35")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .execute();

    }

    @ParameterizedTest(name = "Kalixa threeds V2 debit with source type {0}")
    @ValueSource(strings = {"XML", "POST"})
    public void shouldProcessThreedsTwodebit(String source) {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().debit().withCard(MASTERCARD_1)
                .and().withSource(source)
                .and().withAmount("1.35")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .execute();
    }

    @ParameterizedTest(name = "Kalixa non threeds cof debit with source type {0}")
    @ValueSource(strings = {"XML", "POST"})
    public void shouldProcessNonThreedsCofdebit(String source) {
        Flow flow = Flow.forMerchant(KALIXA_NON_THREED_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .and().withSource(source)
                .then().debit().referringToNth(REGISTERATION)
                .and().withAmount("1.35")
                .execute();

    }

    @ParameterizedTest(name = "Kalixa threeds V1 cof debit with source type {0}")
    @ValueSource(strings = {"XML", "POST"})
    public void shouldProcessThreedsOneCofdebit(String source) {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_ONE_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .and().withSource(source)
                .then().debit().referringToNth(REGISTERATION)
                .and().withAmount("1.35")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_1)
                .execute();

    }

    @ParameterizedTest(name = "Kalixa threeds V2 cof debit with source type {0}")
    @ValueSource(strings = {"XML", "POST"})
    public void shouldProcessThreedsTwoCofdebit(String source) {
        Flow flow = Flow.forMerchant(KALIXA_THREEDS_TWO_MERCHANT).withPaymentMethod(CREDITCARD)
                .startWith().register().withCard(MASTERCARD_1)
                .and().withSource(source)
                .then().debit().referringToNth(REGISTERATION)
                .and().withAmount("1.35")
                .and().withResponseUrl()
                .and().asThreeds(VERSION_2)
                .execute();

    }
}

package com.unzer.tests.threeds.creditcard.threeds;

import com.unzer.constants.Card;
import com.unzer.constants.Merchant;
import com.unzer.constants.TransactionType;
import com.unzer.util.Flow;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.Test;

//TODO : Need to add the tests here
//This is a placeholder for adding testcases for threeds 2.0 workflows
public class ThreedsTwoTransactionsTest {
    private static Merchant merchant = Merchant.SIX_THREEDS_TWO_MERCHANT;

    @Test
    public void shouldRejectTransactionWithoutCard() {
        Flow flow = Flow.forMerchant(merchant)
                .startWith().register().withCard(Card.MASTERCARD)
                .then().debit().referringToNth(TransactionType.REGISTRATION).withResponseUrl().asThreeds();
        flow.execute();
        ResponseType response = flow.getLastTransactionResponse();

    }
}

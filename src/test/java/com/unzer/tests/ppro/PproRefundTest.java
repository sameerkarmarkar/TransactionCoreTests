package com.unzer.tests.ppro;

import com.unzer.constants.Merchant;
import com.unzer.constants.PaymentNetworkProvider;
import com.unzer.tests.BaseTest;
import com.unzer.util.DatabaseHelper;
import com.unzer.util.Flow;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.unzer.constants.PaymentMethod.ONLINE_TRANSFER;
import static com.unzer.constants.TransactionCode.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@Tag("HPCTXNCORE-1934")
@Slf4j
public class PproRefundTest extends BaseTest {

    @Test
    public void shouldSendUsageAsDynamicDescriptorInFullRefund() {
        Flow flow = Flow.forMerchant(Merchant.PPRO_IDEAL_MERCHANT).withPaymentMethod(ONLINE_TRANSFER).withPaymentNetwork(PaymentNetworkProvider.PPRO)
                .startWith().preauthorization().withAccountHolder("Test Account", "ALIPAY");

        flow.execute();

        String receiptUniqueId = DatabaseHelper.getImplicitTransactionUniqueId(flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID(), "REC");
        flow.continueWith().refund().referringToTransactionWithId(receiptUniqueId);

        flow.execute();
        verifyRefundResponse(flow);

    }

    @Test
    public void shouldSendUsageAsDynamicDescriptorInPartialRefund() {
        Flow flow = Flow.forMerchant(Merchant.PPRO_IDEAL_MERCHANT).withPaymentMethod(ONLINE_TRANSFER).withPaymentNetwork(PaymentNetworkProvider.PPRO)
                .startWith().preauthorization().withAccountHolder("Test Account", "ALIPAY").withAmount("50");

        flow.execute();

        String receiptUniqueId = DatabaseHelper.getImplicitTransactionUniqueId(flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID(), "REC");
        flow.continueWith().refund().referringToTransactionWithId(receiptUniqueId).withAmount("10");

        flow.execute();
        verifyRefundResponse(flow);

        flow.continueWith().refund().referringToTransactionWithId(receiptUniqueId).withAmount("20");

        flow.execute();
        verifyRefundResponse(flow);
    }

    @Test
    public void shouldNotSendUsageAsDynamicDescriptorInSofortRefund() {
        Flow flow = Flow.forMerchant(Merchant.SOFORT_ONLINE_TRANSFER_MERCHANT).withPaymentMethod(ONLINE_TRANSFER).withPaymentNetwork(PaymentNetworkProvider.SOFORT)
                .startWith().preauthorization().withAccountHolder("Test Account", "SOFORT")
                .withResponseUrl().withAmount("50");

        flow.execute();

        String receiptUniqueId = DatabaseHelper.getImplicitTransactionUniqueId(flow.getLastTransactionResponse().getTransaction().getIdentification().getShortID(), "REC");
        flow.continueWith().refund().referringToTransactionWithId(receiptUniqueId).withAmount("10");
        flow.execute();

        ResponseType refundResponse = flow.getLastTransactionResponse();
        String usage = flow.getLastTransactionRequest().getTransaction().getPayment().getPresentation().getUsage();

        assertThat("Response descriptor does not contain usage", DatabaseHelper.getMessageSentToConnector(refundResponse.getTransaction().getIdentification().getShortID())
                , not(containsString(usage)));

    }

    private void verifyRefundResponse(Flow flow) {
        ResponseType refundResponse = flow.getLastTransactionResponse();
        RequestType refundRequest = flow.getLastTransactionRequest();
        RequestType parentRequest = flow.getExecutedTransactions().stream()
                .filter(r -> r.getTransaction().getPayment().getCode().contains(PREAUTHORIZATION.getCode()))
                .findFirst().get();

        String expectedAmount = refundRequest.getTransaction().getPayment().getPresentation().getAmount().isEmpty()
                ? parentRequest.getTransaction().getPayment().getPresentation().getAmount()
                : refundRequest.getTransaction().getPayment().getPresentation().getAmount();

        String usage = refundRequest.getTransaction().getPayment().getPresentation().getUsage();
        String shortId = refundResponse.getTransaction().getIdentification().getShortID();

        assertAll(
                () -> assertThat("Invalid transaction status", refundResponse.getTransaction().getProcessing().getStatus().getCode(), equalTo("90")),
                () -> assertThat("Invalid transaction status", refundResponse.getTransaction().getProcessing().getStatus().getValue(), equalTo("NEW")),
                () -> assertThat("Invalid transaction status", refundResponse.getTransaction().getProcessing().getReason().getCode(), equalTo("00")),
                () -> assertThat("Invalid transaction status", refundResponse.getTransaction().getProcessing().getReason().getValue(), equalTo("SUCCESSFULL")),
                () -> assertThat("Invalid transaction status", DatabaseHelper.getTransactionStatus(refundResponse.getTransaction().getIdentification().getShortID()), equalTo("90")),
                () -> assertThat("Invalid usage in dynamic descriptor", Pattern.compile("dynamicdescriptor=.*"+usage)
                        .matcher(DatabaseHelper.getMessageSentToConnector(shortId)).find(), equalTo(true)),
                () -> assertThat("Response descriptor does not contain usage", refundResponse.getTransaction().getPayment().getClearing().getDescriptor().contains(usage)),
                () -> assertThat("Amount in the response is invalid", Double.valueOf(refundResponse.getTransaction().getPayment().getPresentation().getAmount())
                        .equals(Double.valueOf(expectedAmount)))

        );

    }
}

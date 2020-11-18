package com.unzer.util;

import com.unzer.constants.*;
import lombok.extern.slf4j.Slf4j;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class Flow {

    private static final CoreClient coreClient = CoreClient.INSTANCE;
    private static final Configuration conf = Configuration.INSTANCE;

    private RequestType request;
    private Merchant merchant;
    private TransactionMode mode = TransactionMode.as(conf.getProperty("transaction.mode"));
    private boolean isThreeDs = false;
    private ThreedsVersion threeDsVersion;
    private PaymentMethod paymentMethod;
    private Map<Integer, Executable> executionSequence;
    private Integer id = 1;
    private String parentCode = StringUtils.EMPTY;
    private Integer parentIndex = -1;
    private String parentTransactionId = StringUtils.EMPTY;
    private boolean isContinuation = false;

    public static Flow forMerchant(Merchant merchant) {
        Flow flow = new Flow();
        flow.merchant = merchant;
        flow.executionSequence = new HashMap<>();
        return flow;
    }

    public Flow withPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public Flow startWith() {
        return this;
    }

    public Flow and() {
        return this;
    }

    public Flow then() {
        return this;
    }

    public Flow withTransactionType() {
        return this;
    }

    public Flow preauthorization() {
        savePrevious();
        request = RequestBuilder.preauthorization(merchant, paymentMethod, "100", "EUR", mode);
        return this;
    }

    public Flow capture() {
        savePrevious();
        request = RequestBuilder.capture(merchant, paymentMethod, "100", "EUR", mode);
        return this;
    }

    public Flow debit() {
        savePrevious();
        request = RequestBuilder.debit(merchant, paymentMethod, "100", "EUR", mode);
        return this;
    }

    public Flow refund() {
        savePrevious();
        request = RequestBuilder.refund(merchant, paymentMethod, "100", "EUR", mode);
        return this;
    }

    public Flow referringToNth(TransactionCode type) {
        return referringToNth(type, 1);
    }

    public Flow referringToNth(TransactionCode type, Integer n) {
        parentCode = paymentMethod.getMethod() + "." + type.getCode();
        parentIndex = n - 1;
        return this;
    }

    public Flow referringToTransactionWithId(String parentUniqueId) {
        this.parentTransactionId = parentUniqueId;
        return this;
    }

    public Flow register() {
        savePrevious();
        request = RequestBuilder.register(merchant, paymentMethod, "100", "EUR", mode);
        return this;
    }

    public Flow schedule() {
        savePrevious();
        request = RequestBuilder.schedule(merchant, paymentMethod, "100", "EUR", mode);
        return this;
    }

    public Flow withSchedule(TransactionCode scheduledTransaction) {
        request = RequestBuilder.newRequest(request).withSchedule(scheduledTransaction).build();
        return this;
    }

    public Flow withCard(Card card) {
        request = RequestBuilder.newRequest(request).withCard(card).build();
        return this;
    }

    public Flow withRecurringIndicator(Recurrence recurrence) {
        request = RequestBuilder.newRequest(request).withRecurrance(recurrence).build();
        return this;
    }

    public Flow withAccountHolder(String accountHolder, String brand) {
        request = RequestBuilder.newRequest(request).withAccountHolder(accountHolder, brand).build();
        return this;
    }

    public Flow withResponseUrl() {
        request = RequestBuilder.newRequest(request).withResponseUrl().build();
        return this;
    }

    public Flow asThreeds() {
        return asThreeds(ThreedsVersion.VERSION_2);
    }

    public Flow asThreeds(ThreedsVersion version) {
        this.isThreeDs = true;
        this.threeDsVersion = version;
        return this;
    }

    public Flow inMode(TransactionMode mode) {
        this.mode = mode;
        return this;
    }

    private void savePrevious() {
        if (request != null && !isContinuation) {
            log.info("Saving transaction request to execution sequence");
            Executable executable = Executable.builder()
                    .request(request).parentCode(parentCode).parentIndex(parentIndex)
                    .parentTransactionId(parentTransactionId)
                    .isThreeds(isThreeDs).threedsVersion(threeDsVersion).build();
            executionSequence.put(id, executable);
            parentCode = StringUtils.EMPTY;
            parentIndex = -1;
            isThreeDs = false;
            threeDsVersion = ThreedsVersion.NONE;
            parentTransactionId = StringUtils.EMPTY;
            id++;
        }

        isContinuation = false;
    }

    public Flow continueWith() {
        isContinuation = true;
        return this;
    }

    public void execute() {
        savePrevious();
        for (Map.Entry<Integer, Executable> entry: executionSequence.entrySet()) {
            Executable e = entry.getValue();

            if (!e.isExecuted()) {
                RequestType transaction = e.getRequest();
                String parentCode = e.getParentCode();
                if (!parentCode.isEmpty()) {
                    log.info("Building referenced payment");
                    ResponseType parentResponse = executionSequence.values().stream()
                            .filter(t -> t.getRequest().getTransaction().getPayment().getCode().equals(parentCode))
                            .filter(t -> t.isExecuted())
                            .collect(Collectors.toList())
                            .get(e.getParentIndex()).getResponse();
                    e.setRequest(RequestBuilder.newRequest(e.getRequest()).referringTo(parentResponse).build());
                } else if (!e.getParentTransactionId().equals(StringUtils.EMPTY)) {
                    e.setRequest(RequestBuilder.newRequest(e.getRequest()).referringTo(e.getParentTransactionId()).build());
                }

                ResponseType response = coreClient.send(e.getRequest());
                e.setResponse(response);
                if (e.isThreeds())
                    handleThreeds(e);

                if (e.getRequest().getTransaction().getPayment().getCode().contains(PaymentMethod.ONLINE_TRANSFER.getMethod()))
                    OnlineTransferSimulator.authorizeOnlineTransfer(response);

                e.setExecuted(true);
                log.info("executed {}. Short id is {}", response.getTransaction().getPayment().getCode(), response.getTransaction().getIdentification().getShortID());
            } else {
                log.info("transaction "+ e.getRequest().getTransaction().getPayment().getCode() + " already executed. Evaluating the next in the flow");
            }
        }
    }

    public ResponseType getLastTransactionResponse() {
        return executionSequence.values().stream().reduce((first, second) -> second)
                .orElse(null).getResponse();
    }

    public ResponseType getParentResponse() {
        String parentUniqueId = executionSequence.values().stream().reduce((first, second) -> second)
                .orElse(null).getRequest().getTransaction().getIdentification().getReferenceID();

        return executionSequence.values().stream()
                .filter(t -> t.getResponse().getTransaction().getIdentification().getUniqueID().equals(parentUniqueId))
                .findFirst().get().getResponse();
    }

    public RequestType getLastTransactionRequest() {
        return executionSequence.values().stream().reduce((first, second) -> second)
                .orElse(null).getRequest();
    }

    public void handleThreeds(Executable e) {
        AcsClient.forVersion(e.getThreedsVersion()).processThreedsAuthorization(e.getResponse());
    }

    public void getExecutedTransactions() {
        for (Map.Entry<Integer, Executable> entry: executionSequence.entrySet()) {
            RequestType transaction = entry.getValue().getRequest();
            log.info("Transaction id-->{}   Transaction type-->{}   executed-->{}", entry.getKey(),
                    transaction.getTransaction().getPayment().getCode(), entry.getValue().isExecuted());
            entry.getValue().setExecuted(true);
        }
    }





}

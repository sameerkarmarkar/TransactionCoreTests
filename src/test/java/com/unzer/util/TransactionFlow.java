package com.unzer.util;

import com.unzer.chef.RequestChef;
import com.unzer.constants.Card;
import com.unzer.constants.Merchant;
import lombok.*;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.unzer.constants.Constants.DESTINATION;
import static com.unzer.util.Formatter.marshall;
import static com.unzer.util.Formatter.unmarshal;

public class TransactionFlow {

    private static final HttpClient client = new HttpClient();
    private static final PostMethod method = new PostMethod(DESTINATION);
    private static final QName _Request_QNAME = new QName("", "Request");
    private RequestBuilder requestBuilder;

    public TransactionFlow() {
        executedTransactions = new LinkedHashMap<>();
        executionSequence = new LinkedHashMap<>();
        expectedResponseType = ResponseType.class;
    }

    private Merchant merchant;
    private Card card;
    private RequestType processTransaction;
    private Class expectedResponseType;
    private String currency = "EUR";
    private Map<RequestType, TransactionExecutionParameters> executionSequence;
    private Map<RequestType, JAXBElement<ResponseType>> executedTransactions;
    private boolean nextTransactionUnsuccessful = false;
    private boolean purgeNextTransaction = false;
    private boolean saveAsParentTransaction;
    private boolean isThreeds = true;
    private String parentTransactionId;
    private String transactionGroupId;
    private boolean waitForPersistence = false;
    private boolean isAlreadyExecuted = false;

    public static TransactionFlow startWith() {
        return new TransactionFlow();
    }

    public TransactionFlow continueWith() {
        return this;
    }

    public TransactionFlow and() {
        return this;
    }

    public TransactionFlow add() {
        return this;
    }

    public TransactionFlow then() {
        return this;
    }

    public TransactionFlow forMerchant(Merchant merchant) {
        this.merchant = merchant;
        return this;
    }

    public TransactionFlow forCard(Card card) {
        this.card = card;
        return this;
    }

    public TransactionFlow forCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public TransactionFlow preauthorization(String amount, String currency) {
        savePreviousTransaction();
        processTransaction = RequestChef.preautorization("100","CHF", merchant);
        return this;
    }



    private void savePreviousTransaction() {
        //if (processTransaction != null && executedTransactions.entrySet().stream().noneMatch(keyIsSameTransactionAs(processTransaction)))
        if (processTransaction != null) {
            executionSequence.put(processTransaction, new TransactionExecutionParameters(saveAsParentTransaction, waitForPersistence, isAlreadyExecuted));
        }
        /*if (nextTransactionUnsuccessful)
            expectedResponseType = TransactionProcessingError.class;
        else expectedResponseType = TransactionProcessed.class;
        saveAsParentTransaction = !purgeNextTransaction;*/

        nextTransactionUnsuccessful = false;
        purgeNextTransaction = false;
        waitForPersistence = false;
    }

    @SneakyThrows
    public TransactionFlow execute() {
        savePreviousTransaction();
        for (Map.Entry<RequestType, TransactionExecutionParameters> toExecute : executionSequence.entrySet()) {
            var transactionToExecute = toExecute.getKey();
            if (!toExecute.getValue().isAlreadyExecuted) {

                //waitIfRequired(toExecute.getValue().waitForPersistence);
                /*if (parentTransactionId != null)
                    transactionToExecute = transactionToExecute.toBuilder()
                            .setParentTransactionId(of(parentTransactionId))
                            .build();*/

                /*if (transactionGroupId != null)
                    transactionToExecute = transactionToExecute.toBuilder()
                            .setTransactionGroupId(of(transactionGroupId))
                            .build();*/

                /*if (toExecute.getValue().getExpectedResponseType().equals(TransactionProcessed.class)) {
                    val transactionProcessed = novaClient.send(transactionToExecute, TransactionProcessed.class);
                    if (toExecute.getValue().saveTransactionIdForReferencing) {
                        parentTransactionId = transactionProcessed.getTransactionId();
                        transactionGroupId = transactionProcessed.getTransctionGroupDescriptor().getTransactionGroupId();
                    }
                    executedTransactions.put(transactionToExecute, transactionProcessed);
                } else {
                    val transactionProcessingError = novaClient.send(transactionToExecute, TransactionProcessingError.class);
                    if (toExecute.getValue().saveTransactionIdForReferencing) {
                        parentTransactionId = transactionProcessingError.getTransactionId().getValue();
                        transactionGroupId = transactionProcessingError.getTransctionGroupDescriptor().getTransactionGroupId();
                    }
                    executedTransactions.put(transactionToExecute, transactionProcessingError);
                }*/

                JAXBElement<RequestType> jaxbRequestType = new JAXBElement(_Request_QNAME, RequestType.class, (Class)null, transactionToExecute);
                String xmlOutput = marshall(jaxbRequestType);
                method.setParameter("load", xmlOutput);
                int statusCode = client.executeMethod(method);
                JAXBElement<ResponseType> response = unmarshal(method.getResponseBodyAsString());
                executedTransactions.put(transactionToExecute, response);
            }
        }
        return this;
    }

    public JAXBElement<ResponseType> getLastTransactionResponse() {
        val allTransactions = executedTransactions.values().stream()
                .filter(t -> t instanceof JAXBElement)
                .map(t -> (JAXBElement) t)
                .collect(Collectors.toList());;
        var response = (CollectionUtils.isEmpty(allTransactions))
        ? null : allTransactions.get(allTransactions.size() - 1);
        return (JAXBElement<ResponseType>) response;
    }

    @AllArgsConstructor
    @Getter
    private class TransactionExecutionParameters {
        boolean saveTransactionIdForReferencing;
        boolean waitForPersistence;
        boolean isAlreadyExecuted;
        //Class expectedResponseType;
    }

}

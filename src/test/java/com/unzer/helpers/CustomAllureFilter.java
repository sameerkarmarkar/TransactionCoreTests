package com.unzer.helpers;

import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.attachment.http.HttpRequestAttachment;
import io.qameta.allure.attachment.http.HttpResponseAttachment;
import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.internal.NameAndValue;
import io.restassured.internal.support.Prettifier;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CustomAllureFilter implements OrderedFilter {

    private String requestTemplatePath = "http-request.ftl";
    private String responseTemplatePath = "http-response.ftl";

    public CustomAllureFilter() {
    }

    public CustomAllureFilter setRequestTemplate(String templatePath) {
        this.requestTemplatePath = templatePath;
        return this;
    }

    public CustomAllureFilter setResponseTemplate(String templatePath) {
        this.responseTemplatePath = templatePath;
        return this;
    }

    public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext filterContext) {
        Prettifier prettifier = new Prettifier();
        HttpRequestAttachment.Builder requestAttachmentBuilder =
                HttpRequestAttachment.Builder.create("Request",
                        requestSpec.getURI())
                        .setMethod(requestSpec.getMethod())
                        .setHeaders(toMapConverter(requestSpec.getHeaders()))
                        .setCookies(toMapConverter(requestSpec.getCookies()));
        if (Objects.nonNull(requestSpec.getBody())) {
            requestAttachmentBuilder.setBody(prettifier.getPrettifiedBodyIfPossible(requestSpec));
        } else if (Objects.nonNull(requestSpec.getFormParams())) {
            requestAttachmentBuilder.setBody(toStringConverter(requestSpec.getFormParams()));
        } else if (Objects.nonNull(requestSpec.getQueryParams())) {
            requestAttachmentBuilder.setBody(toStringConverter(requestSpec.getQueryParams()));
        }

        HttpRequestAttachment requestAttachment = requestAttachmentBuilder.build();
        (new DefaultAttachmentProcessor()).addAttachment(requestAttachment, new FreemarkerAttachmentRenderer(this.requestTemplatePath));
        Response response = filterContext.next(requestSpec, responseSpec);
        HttpResponseAttachment responseAttachment = io.qameta.allure.attachment.http.HttpResponseAttachment.Builder.create(response.getStatusLine()).setResponseCode(response.getStatusCode()).setHeaders(toMapConverter(response.getHeaders())).setBody(prettifier.getPrettifiedBodyIfPossible(response, response.getBody())).build();
        (new DefaultAttachmentProcessor()).addAttachment(responseAttachment, new FreemarkerAttachmentRenderer(this.responseTemplatePath));
        return response;
    }

    private static Map<String, String> toMapConverter(Iterable<? extends NameAndValue> items) {
        Map<String, String> result = new HashMap();
        items.forEach((h) -> {
            String var10000 = (String)result.put(h.getName(), h.getValue());
        });
        return result;
    }

    private static String toStringConverter(Map<String, String> items) {
        String result = items.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.joining(", "));
        return result;
    }

    public int getOrder() {
        return 2147483647;
    }
}

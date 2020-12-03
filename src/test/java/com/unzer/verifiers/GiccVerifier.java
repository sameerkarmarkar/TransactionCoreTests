package com.unzer.verifiers;

import com.unzer.constants.Card;
import com.unzer.domain.GiccField;
import com.unzer.domain.GiccMessage;
import com.unzer.helpers.DatabaseHelper;
import io.qameta.allure.Step;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

public class GiccVerifier {

    private GiccMessage message;
    private String shortId;

    public static GiccVerifier INSTANCE = new GiccVerifier();

    @SneakyThrows
    public GiccVerifier withShortId(String shortId) {
        this.shortId = shortId;
        return this;
    }

    @SneakyThrows
    public GiccVerifier getMessage() {
        String giccMessage = DatabaseHelper.getMessageSentToConnector(shortId);
        JAXBContext jaxbContext = JAXBContext.newInstance(GiccMessage.class);

        InputStream stream = new ByteArrayInputStream(giccMessage.getBytes("UTF-8"));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        message = (GiccMessage) jaxbUnmarshaller.unmarshal(stream);
        return this;
    }

    public GiccVerifier and() {
        return this;
    }

    @SneakyThrows
    public GiccMessage parseMessage(String giccMessage) {
        JAXBContext jaxbContext = JAXBContext.newInstance(GiccMessage.class);

        InputStream stream = new ByteArrayInputStream(giccMessage.getBytes("UTF-8"));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (GiccMessage) jaxbUnmarshaller.unmarshal(stream);
    }

    public void verifyField(String fieldId, String expectedValue) {
        assertThat("Incorrect value for field "+fieldId,getFieldValue(fieldId), equalTo(expectedValue));
    }

    public boolean isFieldPresent(String fieldId) {
        return message.getField().stream().filter(f -> f.getId().trim().equals(fieldId)).count() > 0;
    }

    public void verifyMandatoryFields() {
        assertAll(
                () -> assertThat("Field 11 is missing from message", isFieldPresent("11")),
                () -> assertThat("Field 12 is missing from message", isFieldPresent("12")),
                () -> assertThat("Field 13 is missing from message", isFieldPresent("13")),
                () -> assertThat("Field 41 is missing from message", isFieldPresent("41")),
                () -> assertThat("Field 42 is missing from message", isFieldPresent("42")),
                () -> assertThat("Field 46 is missing from message", isFieldPresent("46")),
                () -> assertThat("Field 57 is missing from message", isFieldPresent("57"))
        );
    }

    @Step
    public void verifyFieldsForOneOff(String brand) {
        verifyMandatoryFields();
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("812")),
                () -> assertThat("invalid value for field 15", isFieldPresent("15"), is(false)),
                () -> assertThat("invalid value for field 61", isFieldPresent("61"), is(false)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"), equalTo(getExpected6040(brand))),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), equalTo(getExpected6062(brand))),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), equalTo(getExpected6063(brand))),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), equalTo("1")),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), equalTo(DatabaseHelper.getDsTransId(shortId)))
        );
    }

    @Step
    public void verifyFieldsForReturningCustomer(String brand, String parentShortId) {
        verifyMandatoryFields();
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("102")),
                () -> assertThat("invalid value for field 15", isFieldPresent("15"), is(false)),
                () -> assertThat("invalid value for field 61", isFieldPresent("61"), is(false)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"), equalTo("07")),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), is("003022")),
                () -> assertThat("invalid value for field 60.54", getSubFieldValue("60","54"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), is(emptyOrNullString()))
        );
    }

    //TODO: Change the verification for 60.41 once initial recurring and one off can be distinguished
    @Step
    public void verifyFieldsForUnscheduledInitialRecurring(String brand) {
        verifyMandatoryFields();
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("812")),
                () -> assertThat("invalid value for field 15", isFieldPresent("15"), is(false)),
                () -> assertThat("invalid value for field 61", isFieldPresent("61"), is(false)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"),
                        brand.equals(Card.VISA_1.getCardBrand()) ? equalTo("10") : equalTo("11")),
                //() -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), equalTo(brand.equals(Card.MASTERCARD_1.getCardBrand()) ? "06" : "01")),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.54", getSubFieldValue("60","54"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), equalTo(getExpected6062(brand))),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), equalTo(getExpected6063(brand))),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), equalTo("1")),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), equalTo(DatabaseHelper.getDsTransId(shortId)))
        );
    }

    @Step
    public void verifyFieldsForUnscheduledSubsequentRecurring(String brand, String initialTransactionShortId) {
        GiccMessage initialResponse = getGiccResponse(initialTransactionShortId);
        verifyMandatoryFields();
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("102")),
                () -> assertThat("invalid value for field 15", getFieldValue("15"),
                        equalTo(initialResponse.containsField("15") ? initialResponse.fieldById("15").getValue() : null)),
                () -> assertThat("invalid value for field 61", getFieldValue("61"),
                        equalTo(initialResponse.containsField("61") ? initialResponse.fieldById("61").getValue() : null)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"), equalTo("07")),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), is(equalTo("05"))),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), equalTo("003022")),
                () -> assertThat("invalid value for field 60.54", getSubFieldValue("60","54"), equalTo("01")),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), is(emptyOrNullString()))
        );
    }

    @Step
    public void verifyFieldsForScheduledSubsequentRecurring(String brand, String initialTransactionShortId) {
        GiccMessage initialResponse = getGiccResponse(initialTransactionShortId);
        verifyMandatoryFields();
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("102")),
                () -> assertThat("invalid value for field 15", getFieldValue("15"),
                        equalTo(initialResponse.containsField("15") ? initialResponse.fieldById("15").getValue() : null)),
                () -> assertThat("invalid value for field 61", getFieldValue("61"),
                        equalTo(initialResponse.containsField("61") ? initialResponse.fieldById("61").getValue() : null)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"), equalTo("07")),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), equalTo("02")),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), equalTo("003022")),
                () -> assertThat("invalid value for field 60.54", getSubFieldValue("60","54"), equalTo("03")),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), is(emptyOrNullString()))
        );
    }

    public String getExpected6062(String brand) {
     return getExpected6062(brand, shortId);
    }

    public String getExpected6062(String brand, String shortId) {
        return brand.equals(Card.VISA_1.getCardBrand())
                ? "<20 bytes binary>"
                : null;
    }

    public String getExpected6063(String brand) {
        return getExpected6063(brand, shortId);
    }

    public String getExpected6063(String brand, String shortId) {
        return brand.equals(Card.MASTERCARD_1.getCardBrand())
                ? DatabaseHelper.getCavv(shortId)
                : null;
    }
    public String getExpected6040(String brand) {
        return getExpected6040(brand, shortId);
    }

    public String getExpected6040(String brand, String shortId) {
        String eci = DatabaseHelper.getEci(shortId);
        if (brand.equals(Card.MASTERCARD_1.getCardBrand())) {
            switch (eci) {
                case "00":
                    return "7";
                case "07":
                    return "24";
                case "01":
                    return "13";
                case "06":
                    return "23";
                case "02":
                default:
                    return "11";
            }

        } else if (brand.equals(Card.VISA_1.getCardBrand())) {
            switch (eci) {
                case "07":
                    return "7";
                case "01":
                case "06":
                    return "12";
                case "02":
                case "05":
                    return "10";
            }

        }

        return "";
    }

    @SneakyThrows
    public GiccMessage getGiccResponse(String shortId) {
        String giccMessage = DatabaseHelper.getGiccResponse(shortId);
        JAXBContext jaxbContext = JAXBContext.newInstance(GiccMessage.class);

        InputStream stream = new ByteArrayInputStream(giccMessage.getBytes("UTF-8"));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return  (GiccMessage) jaxbUnmarshaller.unmarshal(stream);
    }


    public String getFieldValue(String fieldId) {
        return message.containsField(fieldId)
                ? message.fieldById(fieldId).getValue()
                : null;

    }

    public String getSubFieldValue(String fieldId, String subFieldId) {
        try {
            GiccField subField = message.fieldById(fieldId).getSubFields().stream().filter(f -> f.getId().trim().equals(subFieldId)).findFirst().get();
            return subField.getValue();
        } catch(NoSuchElementException e) {
            return null;
        }
    }
}

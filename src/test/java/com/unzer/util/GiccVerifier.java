package com.unzer.util;

import com.unzer.constants.Card;
import com.unzer.domain.GiccField;
import com.unzer.domain.GiccMessage;
import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
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
        String giccMessage = DatabaseHelper.getGiccMessage(shortId);
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

    public void verifyFieldsForOneOff(String brand) {
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

    public void verifyFieldsForReturningCustomer(String brand, String parentShortId) {
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("102")),
                () -> assertThat("invalid value for field 15", isFieldPresent("15"), is(false)),
                () -> assertThat("invalid value for field 61", isFieldPresent("61"), is(false)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"), equalTo(getExpected6040(brand, parentShortId))),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), equalTo(getExpected6062(brand, parentShortId))),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), equalTo(getExpected6063(brand, parentShortId))),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), equalTo("1")),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), equalTo(DatabaseHelper.getDsTransId(parentShortId)))
        );
    }

    public void verifyFieldsForUnscheduledInitialRecurring(String brand) {
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("812")),
                () -> assertThat("invalid value for field 15", isFieldPresent("15"), is(false)),
                () -> assertThat("invalid value for field 61", isFieldPresent("61"), is(false)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"),
                        brand.equals(Card.VISA.getCardBrand()) ? equalTo("10") : equalTo("11")),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), equalTo(brand.equals(Card.MASTERCARD.getCardBrand()) ? "06" : "01")),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.54", getSubFieldValue("60","54"), is(emptyOrNullString())),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), equalTo(getExpected6062(brand))),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), equalTo(getExpected6063(brand))),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), equalTo("1")),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), equalTo(DatabaseHelper.getDsTransId(shortId)))
        );
    }

    public void verifyFieldsForUnscheduledSubsequentRecurring(String brand) {
        assertAll(
                () -> assertThat("Invalid value for field 22", getFieldValue("22"), equalTo("102")),
                () -> assertThat("invalid value for field 15", isFieldPresent("15"), is(false)),
                () -> assertThat("invalid value for field 61", isFieldPresent("61"), is(false)),
                () -> assertThat("invalid value for field 60.40", getSubFieldValue("60", "40"), equalTo("07")),
                () -> assertThat("invalid value for field 60.41", getSubFieldValue("60","41"), is(equalTo(brand.equals(Card.MASTERCARD.getCardBrand()) ? "02" : "05"))),
                () -> assertThat("invalid value for field 60.52", getSubFieldValue("60","52"), equalTo("003022")),
                () -> assertThat("invalid value for field 60.54", getSubFieldValue("60","54"), equalTo("01")),
                () -> assertThat("invalid value for field 60.62", getSubFieldValue("60","62"), equalTo(getExpected6062(brand))),
                () -> assertThat("invalid value for field 60.63", getSubFieldValue("60","63"), equalTo(getExpected6063(brand))),
                () -> assertThat("invalid value for field 60.72", getSubFieldValue("60","72"), equalTo("1")),
                () -> assertThat("invalid value for field 60.73", getSubFieldValue("60","73"), equalTo(DatabaseHelper.getDsTransId(shortId)))
        );
    }

    public String getExpected6062(String brand) {
     return getExpected6062(brand, shortId);
    }

    public String getExpected6062(String brand, String shortId) {
        return brand.equals(Card.VISA.getCardBrand())
                ? "<20 bytes binary>"
                : null;
    }

    public String getExpected6063(String brand) {
        return getExpected6063(brand, shortId);
    }

    public String getExpected6063(String brand, String shortId) {
        return brand.equals(Card.MASTERCARD.getCardBrand())
                ? DatabaseHelper.getCavv(shortId)
                : null;
    }
    public String getExpected6040(String brand) {
        return getExpected6040(brand, shortId);
    }

    public String getExpected6040(String brand, String shortId) {
        String eci = DatabaseHelper.getEci(shortId);
        if (brand.equals(Card.MASTERCARD.getCardBrand())) {
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

        } else if (brand.equals(Card.VISA.getCardBrand())) {
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


    public String getFieldValue(String fieldId) {
        try {
            return message.fieldById(fieldId).getValue();
        } catch(NoSuchElementException e) {
            return null;
        }

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

package com.unzer.chef.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import static com.unzer.constants.Constants.*;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Account {

    @XmlElement(name = "Holder")
    protected String holder = "Sameer karmarkar";

    @XmlElement(name = "Number")
    protected String number = VISA_CARD_NUMBER;

    @XmlElement(name = "Brand")
    protected String brand = "VISA";

    @XmlElement(name = "Expiry")
    protected Expiry expiry = new Expiry();

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    static class Expiry {
        @XmlAttribute(name = "month")
        protected String month = CARD_EXPITY_MONTH;

        @XmlAttribute(name = "year")
        protected String year = CARD_EXPITY_YEAR;

        @XmlAttribute(name = "Verification")
        protected String verification = CARD_VERIFICATION_CODE;
    }

}


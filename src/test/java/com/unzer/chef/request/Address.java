package com.unzer.chef.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Address {
    @XmlElement(name = "Street")
    protected String street = "Test";

    @XmlElement(name = "Zip")
    protected String zip = "81927";

    @XmlElement(name = "City")
    protected String city = "Munich";

    @XmlElement(name = "Country")
    protected String country = "DE";
}

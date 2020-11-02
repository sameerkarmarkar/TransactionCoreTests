package com.unzer.chef.request;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Customer {
    @XmlElement(name = "Name")
    protected Name name = new Name();

    @XmlElement(name = "Address")
    protected Address address = new Address();

    @XmlElement(name = "Contact")
    protected Contact contact = new Contact();
}

package com.unzer.chef.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import static com.unzer.constants.Constants.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class User {
    @XmlAttribute(name = "login")
    protected String username = USERNAME;

    @XmlAttribute(name = "pwd")
    protected  String password = PASSWORD;
}

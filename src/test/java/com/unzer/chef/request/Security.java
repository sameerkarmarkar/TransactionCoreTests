package com.unzer.chef.request;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Security {
    @XmlAttribute
    protected String sender = "31HA07BC8102D566343D9FB34BD7D5EE";
}

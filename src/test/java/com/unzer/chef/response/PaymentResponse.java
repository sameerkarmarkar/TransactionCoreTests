package com.unzer.chef.response;

import net.hpcsoft.adapter.payonxml.RequestType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Response")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentResponse extends RequestType {

}

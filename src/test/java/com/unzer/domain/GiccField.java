package com.unzer.domain;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType( XmlAccessType.FIELD )
public class GiccField {
    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "value")
    private String value;

    public List<GiccField> getSubFields() {
        String[] subFieldString = (this.getValue().split(";"))[0].split(",");
        List<GiccField> subFields = Arrays.asList(subFieldString).stream().map(f -> f.split("=", 2)).collect(Collectors.toList())
                .stream().map(m -> GiccField.builder().id(m[0]).value(m[1]).build()).
                collect(Collectors.toList());
        return subFields;
    }

}

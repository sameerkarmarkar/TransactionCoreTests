package com.unzer.constants;

import lombok.Getter;

@Getter
public enum Merchant {

    SIX_THREEDS_ONE_MERCHANT("31HA07BC8102D566343D9FB34BD7D5EE","31HA07BC810C26F59B5B15B6FA085418","31ha07bc8102d566343d379bcfb1e3a6","19EC0D85"),
    SIX_THREEDS_TWO_MERCHANT("31HA07BC8102D566343D9FB34BD7D5EE","31HA07BC814DE5EA4DBF0091A01D3EAD","31ha07bc8102d566343d379bcfb1e3a6","19EC0D85"),
    POSTBANK_THREEDS_TWO_MERCHANT("31HA07BC81054B226BE3206EF7CFBD0F", "31HA07BC81411EAAC3517B951B9DBD52", "31ha07bc81054b226be35e8b8bb4be01", "5E3C01D2"),
    KALIXA_THREEDS_ONE_MERCHANT("31HA07BC8102D566343D3CA829597D98", "31HA07BC813A99B2635A832EFD2801FC", "31ha07bc8102d566343d173579d123d1", "69D14907"),
    KALIXA_THREEDS_TWO_MERCHANT("31HA07BC8102D566343D3CA829597D98", "31HA07BC814DE5EA4DBF2AF0B5B07E0A", "31ha07bc8102d566343d173579d123d1", "69D14907"),
    EVO_THREEDS_TWO_MERCHANT("31HA07BC8111514CE24A7068DD9C1907", "31HA07BC814DD5A67B5B35F8AAB62983", "31ha07bc8111514ce24a286754441b5a", "58F44D6F"),
    PAYONE_THREEDS_TWO_MERCHANT("31HA07BC818748C81AC75584A72B614C", "31HA07BC814DE5EA4DBF3DE45EF07BAA", "31ha07bc818748c81ac79e8c451cf6d7", "9655A8C6"),
    PPRO_IDEAL_MERCHANT("31HA07BC815F10CEC1D80E3BF9C5314C","31HA07BC812A9D1FD792175031876045","31ha07bc815f10cec1d8255aae34cec0", "8C1E4170"),
    SOFORT_ONLINE_TRANSFER_MERCHANT("31HA07BC813E498F174A7F63846CF2B1","31HA07BC810B2852D4F515BD8448DBF5","sgw/ngw-abnahme-kunde", "Abnahmetest");


    private String sender, channel, user, password;

    Merchant(String sender, String channel, String user, String password) {
        this.sender = sender;
        this.channel = channel;
        this.user = user;
        this.password = password;
    }


}

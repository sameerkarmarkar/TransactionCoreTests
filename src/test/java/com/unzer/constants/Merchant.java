package com.unzer.constants;

import lombok.Getter;

@Getter
public enum Merchant {

    SIX_THREEDS_ONE_MERCHANT("31HA07BC8102D566343D9FB34BD7D5EE","31HA07BC810C26F59B5B15B6FA085418","31ha07bc8102d566343d379bcfb1e3a6","19EC0D85"),
    SIX_THREEDS_TWO_MERCHANT("31HA07BC8102D566343D9FB34BD7D5EE","31HA07BC814DE5EA4DBF0091A01D3EAD","31ha07bc8102d566343d379bcfb1e3a6","19EC0D85"),
    POSTBANK_THREEDS_TWO_MERCHANT("31HA07BC81054B226BE3206EF7CFBD0F", "31HA07BC81411EAAC3517B951B9DBD52", "31ha07bc81054b226be35e8b8bb4be01", "5E3C01D2");


    private String sender, channel, user, password;

    Merchant(String sender, String channel, String user, String password) {
        this.sender = sender;
        this.channel = channel;
        this.user = user;
        this.password = password;
    }


}

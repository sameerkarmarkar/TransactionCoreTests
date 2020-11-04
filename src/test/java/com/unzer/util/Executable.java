package com.unzer.util;

import com.unzer.constants.ThreedsVersion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.hpcsoft.adapter.payonxml.RequestType;
import net.hpcsoft.adapter.payonxml.ResponseType;

@Setter
@Getter
@Builder
public class Executable {
    private RequestType request;
    private ResponseType response;
    private boolean isExecuted = false;
    private String parentCode;
    private Integer parentIndex;
    private boolean isThreeds = false;
    private ThreedsVersion threedsVersion;

}

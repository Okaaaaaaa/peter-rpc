package com.peter.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoadBalanceTypeEnum {
    CONSISTENT_HASH("consistent_hash"),
    RANDOM("random"),
    ROUND("round");

    private final String name;
}

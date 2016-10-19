package com.khartec.waltz.model.physical_flow;

public enum FrequencyKind {
    ON_DEMAND, // pull
    REAL_TIME, // push
    INTRA_DAY,
    DAILY,
    WEEKLY,
    MONTHLY,
    QUARTERLY,
    BIANUALLY,
    YEARLY
}
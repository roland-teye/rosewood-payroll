package com.rosewood.payroll.pay;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.rosewood.payroll.pay.Money;

class MoneyTest {

    @ParameterizedTest(name = "{0} rounds to {1}")
    @CsvSource({
            "10,      10.00",
            "10.1,    10.10",
            "10.004,  10.00",
            "10.005,  10.01",
            "10.015,  10.02",
            "2.675,   2.68",
            "-10.004, -10.00",
            "-10.005, -10.01",
            "0.004,   0.00"
    })
    void roundsToTwoDecimalPlacesHalfUp(String amount, String expected) {
        assertEquals(new BigDecimal(expected), Money.round(new BigDecimal(amount)));
    }
}
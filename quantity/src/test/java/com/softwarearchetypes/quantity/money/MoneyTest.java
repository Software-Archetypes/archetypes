package com.softwarearchetypes.quantity.money;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void shouldCreateMoneyFromIntegerAmount() {
        //given
        int amount = 100;

        //when
        Money money = Money.pln(amount);

        //then
        assertEquals(new BigDecimal("100"), money.value());
    }

    @Test
    void shouldCreateMoneyFromBigDecimalAmount() {
        //given
        BigDecimal amount = new BigDecimal("99.99");

        //when
        Money money = Money.pln(amount);

        //then
        assertEquals(amount, money.value());
    }

    @Test
    void shouldCreateMoneyFromNumberAmount() {
        //given
        Number amount = 50.5;

        //when
        Money money = Money.pln(amount);

        //then
        assertEquals(new BigDecimal("50.5"), money.value());
    }

    @Test
    void shouldCreateZeroPlnMoney() {
        //when
        Money money = Money.zeroPln();

        //then
        assertEquals(BigDecimal.ZERO, money.value());
        assertTrue(money.isZero());
    }

    @Test
    void shouldCreateOnePlnMoney() {
        //when
        Money money = Money.onePln();

        //then
        assertEquals(BigDecimal.ONE, money.value());
    }

    @Test
    void shouldAddTwoMoneyAmounts() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(50);

        //when
        Money result = first.add(second);

        //then
        assertEquals(new BigDecimal("150"), result.value());
    }

    @Test
    void shouldSubtractTwoMoneyAmounts() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(30);

        //when
        Money result = first.subtract(second);

        //then
        assertEquals(new BigDecimal("70"), result.value());
    }

    @Test
    void shouldNegateMoneyAmount() {
        //given
        Money money = Money.pln(50);

        //when
        Money result = money.negate();

        //then
        assertEquals(new BigDecimal("-50"), result.value());
        assertTrue(result.isNegative());
    }

    @Test
    void shouldReturnAbsoluteValueOfNegativeMoney() {
        //given
        Money negativeMoney = Money.pln(-100);

        //when
        Money result = negativeMoney.abs();

        //then
        assertEquals(new BigDecimal("100"), result.value());
        assertFalse(result.isNegative());
    }

    @Test
    void shouldReturnAbsoluteValueUsingStaticMethod() {
        //given
        Money negativeMoney = Money.pln(-75);

        //when
        Money result = Money.abs(negativeMoney);

        //then
        assertEquals(new BigDecimal("75"), result.value());
    }

    @Test
    void shouldDivideAndReturnQuotientAndRemainder() {
        //given
        Money money = Money.pln(100);
        BigDecimal divisor = new BigDecimal("3");

        //when
        Money[] result = money.divideAndRemainder(divisor);

        //then
        assertEquals(2, result.length);
        assertEquals(new BigDecimal("33"), result[0].value());
        assertEquals(BigDecimal.ONE, result[1].value());
    }

    @Test
    void shouldReturnTrueWhenMoneyIsZero() {
        //given
        Money money = Money.zeroPln();

        //when & then
        assertTrue(money.isZero());
    }

    @Test
    void shouldReturnFalseWhenMoneyIsNotZero() {
        //given
        Money money = Money.pln(1);

        //when & then
        assertFalse(money.isZero());
    }

    @Test
    void shouldReturnTrueWhenMoneyIsNegative() {
        //given
        Money money = Money.pln(-10);

        //when & then
        assertTrue(money.isNegative());
    }

    @Test
    void shouldReturnFalseWhenMoneyIsPositive() {
        //given
        Money money = Money.pln(10);

        //when & then
        assertFalse(money.isNegative());
    }

    @Test
    void shouldReturnTrueWhenFirstMoneyIsGreaterThanSecond() {
        //given
        Money greater = Money.pln(100);
        Money lesser = Money.pln(50);

        //when & then
        assertTrue(greater.isGreaterThan(lesser));
    }

    @Test
    void shouldReturnFalseWhenFirstMoneyIsNotGreaterThanSecond() {
        //given
        Money lesser = Money.pln(50);
        Money greater = Money.pln(100);

        //when & then
        assertFalse(lesser.isGreaterThan(greater));
    }

    @Test
    void shouldReturnTrueWhenFirstMoneyIsGreaterThanOrEqualToSecond() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(100);

        //when & then
        assertTrue(first.isGreaterThanOrEqualTo(second));
    }

    @Test
    void shouldReturnTrueWhenFirstMoneyIsGreaterInGreaterThanOrEqualComparison() {
        //given
        Money greater = Money.pln(150);
        Money lesser = Money.pln(100);

        //when & then
        assertTrue(greater.isGreaterThanOrEqualTo(lesser));
    }

    @Test
    void shouldReturnMinimumOfTwoMoneyAmounts() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(50);

        //when
        Money result = Money.min(first, second);

        //then
        assertEquals(second, result);
    }

    @Test
    void shouldReturnMinimumFromSetOfMoneyAmounts() {
        //given
        Set<Money> amounts = Set.of(
                Money.pln(100),
                Money.pln(25),
                Money.pln(50),
                Money.pln(75)
        );

        //when
        Optional<Money> result = Money.min(amounts);

        //then
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("25"), result.get().value());
    }

    @Test
    void shouldReturnEmptyOptionalWhenMinCalledOnEmptySet() {
        //given
        Set<Money> emptySet = Set.of();

        //when
        Optional<Money> result = Money.min(emptySet);

        //then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnMaximumOfTwoMoneyAmounts() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(50);

        //when
        Money result = Money.max(first, second);

        //then
        assertEquals(first, result);
    }

    @Test
    void shouldCompareMoneyAmountsCorrectly() {
        //given
        Money smaller = Money.pln(50);
        Money larger = Money.pln(100);
        Money equal = Money.pln(50);

        //when & then
        assertTrue(smaller.compareTo(larger) < 0);
        assertTrue(larger.compareTo(smaller) > 0);
        assertEquals(0, smaller.compareTo(equal));
    }

    @Test
    void shouldBeEqualWhenMoneyHasSameAmountAndCurrency() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(100);

        //when & then
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenMoneyHasDifferentAmount() {
        //given
        Money first = Money.pln(100);
        Money second = Money.pln(50);

        //when & then
        assertNotEquals(first, second);
    }

    @Test
    void shouldNotBeEqualToNull() {
        //given
        Money money = Money.pln(100);

        //when & then
        assertNotEquals(null, money);
    }

    @Test
    void shouldBeEqualToItself() {
        //given
        Money money = Money.pln(100);

        //when & then
        assertEquals(money, money);
    }

    @Test
    void shouldHaveProperStringRepresentation() {
        //given
        Money money = Money.pln(new BigDecimal("123.45"));

        //when
        String result = money.toString();

        //then
        assertEquals("PLN 123.45", result);
    }

    @Test
    void shouldReturnCorrectValueAsBigDecimal() {
        //given
        BigDecimal expectedValue = new BigDecimal("99.99");
        Money money = Money.pln(expectedValue);

        //when
        BigDecimal result = money.value();

        //then
        assertEquals(expectedValue, result);
    }

    @Test
    void shouldHandleZeroInArithmeticOperations() {
        //given
        Money money = Money.pln(100);
        Money zero = Money.zeroPln();

        //when
        Money addResult = money.add(zero);
        Money subtractResult = money.subtract(zero);

        //then
        assertEquals(money, addResult);
        assertEquals(money, subtractResult);
    }

    @Test
    void shouldHandleNegativeAmountsInComparisons() {
        //given
        Money negative = Money.pln(-50);
        Money positive = Money.pln(50);

        //when & then
        assertTrue(negative.isNegative());
        assertFalse(positive.isNegative());
        assertTrue(positive.isGreaterThan(negative));
        assertFalse(negative.isGreaterThan(positive));
    }
}

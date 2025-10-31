package com.softwarearchetypes.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PairTest {

    @Test
    void shouldCreatePairWithBothValues() {
        //given
        String first = "first";
        String second = "second";

        //when
        Pair<String> pair = new Pair<>(first, second);

        //then
        assertNotNull(pair);
        assertEquals(first, pair.first());
        assertEquals(second, pair.second());
    }

    @Test
    void shouldCreatePairWithNullValues() {
        //given
        String first = null;
        String second = null;

        //when
        Pair<String> pair = new Pair<>(first, second);

        //then
        assertNotNull(pair);
        assertEquals(first, pair.first());
        assertEquals(second, pair.second());
    }

    @Test
    void shouldCreatePairWithDifferentTypes() {
        //given
        Integer first = 42;
        Integer second = 100;

        //when
        Pair<Integer> pair = new Pair<>(first, second);

        //then
        assertEquals(first, pair.first());
        assertEquals(second, pair.second());
    }

    @Test
    void shouldBeEqualWhenBothPairsHaveSameValues() {
        //given
        Pair<String> firstPair = new Pair<>("A", "B");
        Pair<String> secondPair = new Pair<>("A", "B");

        //when & then
        assertEquals(firstPair, secondPair);
        assertEquals(firstPair.hashCode(), secondPair.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenPairsHaveDifferentFirstValue() {
        //given
        Pair<String> firstPair = new Pair<>("A", "B");
        Pair<String> secondPair = new Pair<>("C", "B");

        //when & then
        assertNotEquals(firstPair, secondPair);
    }

    @Test
    void shouldNotBeEqualWhenPairsHaveDifferentSecondValue() {
        //given
        Pair<String> firstPair = new Pair<>("A", "B");
        Pair<String> secondPair = new Pair<>("A", "C");

        //when & then
        assertNotEquals(firstPair, secondPair);
    }

    @Test
    void shouldNotBeEqualWhenPairsHaveDifferentValues() {
        //given
        Pair<String> firstPair = new Pair<>("A", "B");
        Pair<String> secondPair = new Pair<>("C", "D");

        //when & then
        assertNotEquals(firstPair, secondPair);
    }

    @Test
    void shouldHaveProperToStringRepresentation() {
        //given
        Pair<String> pair = new Pair<>("first", "second");

        //when
        String result = pair.toString();

        //then
        assertNotNull(result);
        assertEquals("Pair[first=first, second=second]", result);
    }

    @Test
    void shouldCreatePairWithSameValueForBothElements() {
        //given
        String value = "same";

        //when
        Pair<String> pair = new Pair<>(value, value);

        //then
        assertEquals(value, pair.first());
        assertEquals(value, pair.second());
        assertEquals(pair.first(), pair.second());
    }
}

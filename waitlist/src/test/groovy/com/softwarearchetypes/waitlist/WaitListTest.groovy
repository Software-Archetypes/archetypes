package com.softwarearchetypes.waitlist


import spock.lang.Specification

import static com.softwarearchetypes.waitlist.WaitListFixture.polledElementsFrom
import static com.softwarearchetypes.waitlist.WaitListFixture.someValidCapacity
import static java.util.UUID.randomUUID

class WaitListTest extends Specification {

    def "should not allow to create FIFO wait list with no capacity"() {
        when:
            WaitList.waitListWithCapacityOf(0)

        then:
            thrown IllegalArgumentException
    }

    def "should not allow to create prioritized wait list with no capacity"() {
        when:
            WaitList.prioritizedWaitListWithCapacityOf(0)

        then:
            thrown IllegalArgumentException
    }

    def "should return null when polling empty wait list"() {
        given:
            WaitList waitList = WaitList.waitListWithCapacityOf(someValidCapacity())

        expect:
            !waitList.poll()
    }

    def "should return type of wait list according to one declared during creation"() {
        expect:
            waitList.type() == expectedType

        where:
            waitList                                       || expectedType
            WaitList.waitListWithCapacityOf(10)            || WaitListType.FIFO
            WaitList.prioritizedWaitListWithCapacityOf(10) || WaitListType.PRIORITIZED
    }

    def "should return capacity of wait list according to what was declared during creation"() {
        expect:
            waitList.capacity() == capacity

        where:
            capacity            | waitList
            someValidCapacity() | WaitList.waitListWithCapacityOf(capacity)
            someValidCapacity() | WaitList.prioritizedWaitListWithCapacityOf(capacity)
    }

    def "should return elements in the order they were inserted for FIFO wait list"() {
        given:
            List<Element> elements = [Element.of(randomUUID()), Element.of(randomUUID())]
            WaitList waitList = WaitList.waitListWithCapacityOf(elements.size())

        when:
            elements.each { waitList.add(it) }

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements == elements
    }

    def "should not return removed element for FIFO wait list"() {
        given:
            List<Element> elements = [Element.of(randomUUID()), Element.of(randomUUID()), Element.of(randomUUID())]
            WaitList waitList = WaitList.waitListWithCapacityOf(elements.size())
            elements.each { waitList.add(it) }

        when:
            waitList.remove(elements[1])

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements.size() == elements.size() - 1

        and:
            polledElements[0] == elements[0]
            polledElements[1] == elements[2]
    }

    def "should not return element removed by value for FIFO wait list"() {
        given:
            List<Element> elements = [Element.of(randomUUID()), Element.of(randomUUID()), Element.of(randomUUID())]
            WaitList waitList = WaitList.waitListWithCapacityOf(elements.size())
            elements.each { waitList.add(it) }

        when:
            waitList.removeByValue(elements[1].value())

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements.size() == elements.size() - 1

        and:
            polledElements[0] == elements[0]
            polledElements[1] == elements[2]
    }

    def "should return second element when the first one was removed from FIFO wait list"() {
        given:
            List<Element> elements = [Element.of(randomUUID()), Element.of(randomUUID()), Element.of(randomUUID())]
            WaitList waitList = WaitList.waitListWithCapacityOf(elements.size())
            elements.each { waitList.add(it) }

        when:
            waitList.remove(elements[0])

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements.size() == elements.size() - 1

        and:
            polledElements[0] == elements[1]
            polledElements[1] == elements[2]
    }

    def "should not allow to exceed wait list capacity"() {
        given:
            int capacity = 2
            WaitList waitList = WaitList.waitListWithCapacityOf(capacity)
            capacity.times { waitList.add(Element.of(randomUUID())) }

        when:
            waitList.add(Element.of(randomUUID()))

        then:
            thrown IllegalStateException
    }

    def "should return elements ordered by priority ascending for prioritized wait list"() {
        given:
            List<Element> elements = [
                    Element.of(randomUUID(), 3),
                    Element.of(randomUUID(), 1),
                    Element.of(randomUUID(), 2)]

            WaitList waitList = WaitList.prioritizedWaitListWithCapacityOf(elements.size())

        and:
            elements.each { waitList.add(it) }

        when:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements[0] == elements[1]
            polledElements[1] == elements[2]
            polledElements[2] == elements[0]
    }

    def "should not return removed element for prioritized wait list"() {
        given:
            List<Element> elements = [
                    Element.of(randomUUID(), 3),
                    Element.of(randomUUID(), 1),
                    Element.of(randomUUID(), 2)]

            WaitList waitList = WaitList.prioritizedWaitListWithCapacityOf(elements.size())
            elements.each { waitList.add(it) }

        when:
            waitList.remove(elements[1])

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements.size() == elements.size() - 1

        and:
            polledElements[0] == elements[2]
            polledElements[1] == elements[0]
    }

    def "should not return element removed by value for prioritized wait list"() {
        given:
            List<Element> elements = [
                    Element.of(randomUUID(), 3),
                    Element.of(randomUUID(), 1),
                    Element.of(randomUUID(), 2)]

            WaitList waitList = WaitList.prioritizedWaitListWithCapacityOf(elements.size())
            elements.each { waitList.add(it) }

        when:
            waitList.removeByValue(elements[1].value())

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements.size() == elements.size() - 1

        and:
            polledElements[0] == elements[2]
            polledElements[1] == elements[0]
    }

    def "should return second element when the first one was removed from prioritized wait list"() {
        given:
            List<Element> elements = [
                    Element.of(randomUUID(), 3),
                    Element.of(randomUUID(), 1),
                    Element.of(randomUUID(), 2)]

            WaitList waitList = WaitList.prioritizedWaitListWithCapacityOf(elements.size())
            elements.each { waitList.add(it) }

        when:
            waitList.remove(elements[0])

        and:
            List<Element> polledElements = polledElementsFrom(waitList)

        then:
            polledElements.size() == elements.size() - 1

        and:
            polledElements[0] == elements[1]
            polledElements[1] == elements[2]
    }
}

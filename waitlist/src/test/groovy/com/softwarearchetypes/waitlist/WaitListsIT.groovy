package com.softwarearchetypes.waitlist

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import static com.softwarearchetypes.waitlist.WaitListFixture.polledValuesFrom
import static com.softwarearchetypes.waitlist.WaitListFixture.someValidCapacity
import static java.util.UUID.randomUUID

@SpringBootTest
class WaitListsIT extends Specification {

    @Autowired
    private WaitLists waitLists;

    def "created fifo wait list should be retrievable"() {
        when:
            WaitList waitList = waitLists.registerFIFOWaitListFor(someValidCapacity())

        and:
            WaitList loadedWaitList = waitLists.loadWaitListBy(waitList.id())

        then:
            loadedWaitList.size() == waitList.size()
            loadedWaitList.type() == waitList.type()
    }


    def "created prioritized wait list should be retrievable"() {
        when:
            WaitList waitList = waitLists.registerPrioritizedWaitListFor(someValidCapacity())

        and:
            WaitList loadedWaitList = waitLists.loadWaitListBy(waitList.id())

        then:
            loadedWaitList.size() == waitList.size()
            loadedWaitList.type() == waitList.type()
    }


    def "should add element to wait list"() {
        given:
            WaitList waitList = waitLists.registerFIFOWaitListFor(someValidCapacity())

        and:
            UUID value = randomUUID()

        when:
            waitLists.addToWaitList(waitList.id, value)

        and:
            WaitList loadedWaitList = waitLists.loadWaitListBy(waitList.id())

        then:
            loadedWaitList.size() == 1
            loadedWaitList.poll().value() == value
    }

    def "should fail to add element to non-existing wait list"() {
        when:
            waitLists.addToWaitList(WaitListId.random(), randomUUID())

        then:
            thrown IllegalStateException
    }

    def "should add prioritized elements to wait list and return them sorted by priority ascending"() {
        given:
            def values = [
                    [value: randomUUID(), priority: 5],
                    [value: randomUUID(), priority: 2],
                    [value: randomUUID(), priority: 3],
            ]

        and:
            WaitList waitList = waitLists.registerPrioritizedWaitListFor(values.size())

        when:
            values.each { waitLists.addToWaitList(waitList.id, it.value, it.priority) }

        and:
            WaitList loadedWaitList = waitLists.loadWaitListBy(waitList.id())

        then:
            loadedWaitList.size() == 3
            polledValuesFrom(waitList) == [values[1], values[2], values[0]].collect { it.value }
    }

    def "should remove element from wait list"() {
        given:
            List<UUID> values = [randomUUID(), randomUUID(), randomUUID()]
            WaitList waitList = waitLists.registerFIFOWaitListFor(values.size())
            values.each { waitLists.addToWaitList(waitList.id(), it) }

        when:
            waitLists.removeFromWaitList(waitList.id, values[1])

        and:
            WaitList loadedWaitList = waitLists.loadWaitListBy(waitList.id())

        then:
            loadedWaitList.size() == values.size() - 1
            polledValuesFrom(loadedWaitList) == [values[0], values[2]]
    }
}

package com.softwarearchetypes.waitlist

class WaitListFixture {

    static int someValidCapacity() {
        new Random().nextInt(10) + 1
    }

    static List<Element> polledElementsFrom(WaitList waitList) {
        List<Element> polledElements = []
        for (int i in 1..waitList.size()) {
            polledElements.add(waitList.poll())
        }
        polledElements
    }

    static List<UUID> polledValuesFrom(WaitList waitList) {
        List<UUID> polledValues = []
        for (int i in 1..waitList.size()) {
            polledValues.add(waitList.poll().value())
        }
        polledValues
    }
}

package com.softwarearchetypes.waitlist;

import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static com.softwarearchetypes.waitlist.WaitListType.FIFO;
import static com.softwarearchetypes.waitlist.WaitListType.PRIORITIZED;

class WaitList {

    private final WaitListId id;

    private final int capacity;

    private final WaitListType type;

    private final Queue<Element> queue;

    // private int version = 0; TODO when adding db persistence

    WaitList(WaitListId id, WaitListType type, int capacity) {
        this.id = id;
        this.type = type;
        this.capacity = capacity;
        this.queue = FIFO.equals(type) ? new LinkedBlockingQueue<>(capacity) : new PriorityQueue<>(capacity);
    }

    static WaitList waitListWithCapacityOf(int capacity) {
        return new WaitList(WaitListId.random(), FIFO, capacity);
    }

    static WaitList prioritizedWaitListWithCapacityOf(int capacity) {
        return new WaitList(WaitListId.random(), PRIORITIZED, capacity);
    }

    boolean add(Element object) {
        return queue.add(object);
    }

    Element poll() {
        return queue.poll();
    }

    boolean remove(Element object) {
        return queue.remove(object);
    }

    boolean removeByValue(UUID value) {
        Optional<Element> elementToBeRemoved = queue.stream().filter(it -> it.value().equals(value)).findFirst();
        if (elementToBeRemoved.isPresent()) {
            remove(elementToBeRemoved.get());
            return true;
        }
        return false;
    }

    List<Element> content() { //for future db persistence
        return queue.stream().toList();
    }

    WaitListId id() {
        return id;
    }

    WaitListType type() {
        return type;
    }

    int capacity() {
        return capacity;
    }

    int size() {
        return queue.size();
    }


}

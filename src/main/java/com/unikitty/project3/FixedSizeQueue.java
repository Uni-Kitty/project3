package com.unikitty.project3;

import java.util.LinkedList;

public class FixedSizeQueue<E> extends LinkedList<E> {

    private static final long serialVersionUID = -1451187080492603238L;
    
    private int capacity;
    
    public FixedSizeQueue(int capacity) {
        this.capacity = capacity;
    }
    
    @Override
    public boolean add(E e) {
        if (this.size() == capacity)
            pop();
        return super.add(e);
    }

}

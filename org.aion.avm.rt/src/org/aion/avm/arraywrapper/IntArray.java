package org.aion.avm.arraywrapper;

public class IntArray extends Array {

    private int[] underlying;

    public IntArray(int[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public int get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, int val) {
        this.underlying[idx] = val;
    }
}
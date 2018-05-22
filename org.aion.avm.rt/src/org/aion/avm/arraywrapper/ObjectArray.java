package org.aion.avm.arraywrapper;

public class ObjectArray extends Array {

    private Object[] underlying;

    public static ObjectArray initArray(int c){
        return new ObjectArray(c);
    }

    public ObjectArray(int c) {
        this.underlying = new Object[c];
    }

    public ObjectArray(Object[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public Object get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, Object val) {
        this.underlying[idx] = val;
    }
}
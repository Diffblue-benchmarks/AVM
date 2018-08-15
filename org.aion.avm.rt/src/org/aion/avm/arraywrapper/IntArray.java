package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;

public class IntArray extends Array {

    private int[] underlying;

    /**
     * Static IntArray factory
     *
     * After instrumentation, NEWARRAY bytecode (with int as type) will be replaced by a INVOKESTATIC to
     * this method.
     *
     * @param size Size of the int array
     *
     * @return New empty int array wrapper
     */
    public static IntArray initArray(int size){
        IHelper.currentContractHelper.get().externalChargeEnergy(size * ArrayElement.INT.getEnergy());
        return new IntArray(size);
    }

    @Override
    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public int get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, int val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    @Override
    public IObject avm_clone() {
        lazyLoad();
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Internal Helper
    //========================================================

    public IntArray(int c) {
        this.underlying = new int[c];
    }

    public IntArray(int[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public int[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    @Override
    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (int[]) u;
    }

    @Override
    public java.lang.Object getUnderlyingAsObject(){
        lazyLoad();
        return underlying;
    }

    @Override
    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

    public IntArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(IntArray.class, deserializer);

        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new int[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readInt();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(IntArray.class, serializer);

        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeInt(this.underlying[i]);
        }
    }
}

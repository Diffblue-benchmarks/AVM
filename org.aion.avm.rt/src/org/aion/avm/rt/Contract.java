package org.aion.avm.rt;

public abstract class Contract {

    /**
     * Executes this start smart with the given input.
     *
     * @param input the input
     * @param rt    the runtime
     * @return the output
     */
    public abstract byte[] run(byte[] input, BlockchainRuntime rt);
}
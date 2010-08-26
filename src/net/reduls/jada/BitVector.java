package net.reduls.jada;

import java.io.IOException;

final class BitVector {
    public static final int PER_BLOCK_SIZE=32;
    private int blocks[];
    private int rankIndex[];

    public BitVector(int nodeSize) {
	blocks = new int[nodeSize/PER_BLOCK_SIZE+1]; // NOTE: -> ceiling
	for(int i=0; i < blocks.length; i++)
	    blocks[i] = 0;
    }

    public BitVector(FileMappedInputStream in) throws IOException {
	final int size = in.getInt();
	blocks = in.getIntArray(size);
	rankIndex = in.getIntArray(size);
    }

    public int dataSize() { return blocks.length * 4 * 2; }

    public void write(FileMappedOutputStream out) throws IOException {
	out.putInt(blocks.length);
	for(int i=0; i < blocks.length; i++)
	    out.putInt(blocks[i]);
	for(int i=0; i < rankIndex.length; i++)
	    out.putInt(rankIndex[i]);
    }

    public void set(int index, boolean is1bit) {
	final int idx = index/PER_BLOCK_SIZE;
	final int off = index%PER_BLOCK_SIZE;
	
	if(is1bit) blocks[idx] |=   1<<off;
	else       blocks[idx] &= ~(1<<off);
    }

    public void buildRankIndex() {
	rankIndex = new int[blocks.length];
	rankIndex[0] = 0;
	for(int i=1; i < blocks.length; i++)
	    rankIndex[i] = rankIndex[i-1] + Integer.bitCount(blocks[i-1]);
    }

    public int rank(int index) {
	final int idx = index/PER_BLOCK_SIZE;
	final int off = index%PER_BLOCK_SIZE;
	return off==0 ? rankIndex[idx] : rankIndex[idx] + Integer.bitCount(blocks[idx]&((1<<off)-1));
    }
}
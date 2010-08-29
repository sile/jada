package net.reduls.jada;

import java.io.IOException;

/**
 * rank操作を有するビット配列の実装。
 */
final class BitVector {
    private static final int PER_BLOCK_SIZE=32;
    private int blocks[];
    private int rankIndex[];

    /**
     * 指定されたサイズの空のビット配列を作成する。<br />
     * ビット配列の全ての要素は0bitに初期化される。<br />
     * {@link #set}メソッドで各ビット値を設定し、最後に{@link #buildRankIndex}メソッドを呼び出すことで、{@link #rank}メソッドが使用可能となる。
     *
     * @param nodeSize ビット配列のサイズ
     */
    public BitVector(int nodeSize) {
	blocks = new int[nodeSize/PER_BLOCK_SIZE+1];
	for(int i=0; i < blocks.length; i++)
	    blocks[i] = 0;
    }

    /**
     * 構築/書き出し済みのビット配列を入力ストリームから読み込む。
     * 
     * @param in ビット配列データを保持する入力ストリーム
     * @throws IOException 入出力エラーが生じた場合に送出される
     */
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
	
	if(is1bit) blocks[idx] |=   1<<off;  // set bit 1
	else       blocks[idx] &= ~(1<<off); // set bit 0
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
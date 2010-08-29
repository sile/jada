package net.reduls.jada;

import java.util.List;

/**
 * DoubleArray-Trie構築時に使用ノードの割り当てを行うクラス。
 */
final class NodeAllocator {
    private int freeNext[];
    private int freePrev[];
    
    /**
     * {@link NodeAllocator}インスタンスを生成する。<br />
     * このアロケータは、空きのノードをリンクリストにより管理する。<br />
     * そのリンクリスト用の領域は、トライのBASE及びCHECK配列と共有する。
     *
     * @param base トライのBASE配列。空きノード管理用リンクリストの後方へのリンク保持用として用いられる。
     * @param chck トライのCHECK配列。空きノード管理用リンクリストの前方へのリンク保持用として用いられる。
     * @param codeLimit トライ構築対象となる入力キーセットに含まれる文字のコード値の最大値
     */
    public NodeAllocator(int base[], int chck[], final int codeLimit) {
        freeNext = base;
        freePrev = chck;
	
	for(int i=0; i < codeLimit; i++) 
	    freePrev[i] = freeNext[i] = 1;

	for(int i=codeLimit; i < base.length; i++) {
	    freePrev[i]=-(i-1);
	    freeNext[i]=-(i+1);
	}
        
        freeNext[headIndex()] = -codeLimit;
	freePrev[codeLimit] = -headIndex();
    }
    
    /**
     * リンクリストの先端を返す
     * @return リンクリストの先端のインデックス
     */
    public static int headIndex() { return 1; }
    
    /**
     * 遷移に用いられるコードセットを受け取り、対応する有効なベースノードのインデックスを返す。<br />
     * 各コードセットに対応するノードは、このメソッドから処理が戻る前に、'未使用'から'使用済'にマークが変更される。
     * 
     * @param children 遷移に用いられるコードセット(= 子ノード(の遷移コード)セット)
     * @return ベースノードのインデックス
     */
    public int allocate(final List<Integer> children) {
	int cur = -freeNext[headIndex()];
	final int first = children.get(0);
	
	for(;; cur = -freeNext[-freeNext[-freeNext[cur]]]) {
	    int x = cur - first;
	    if(canAllocate(x, children)) {
		for(Integer code : children)
		    allocateImpl(x+code);
		return x;
	    }
	}
    }

    private boolean canAllocate(final int x, final List<Integer> children) {
	for(int i=1; i < children.size(); i++) 
	    if(freePrev[x+children.get(i)]>=0) 
		return false;
	return true;
    }

    private void allocateImpl(final int node) {
	freePrev[-freeNext[node]] = freePrev[node];
	freeNext[-freePrev[node]] = freeNext[node];
	freePrev[node] = freeNext[node] = 1;	
    }
}

package net.reduls.jada;

import java.io.IOException;

/**
 * DoubleArray Trieクラス。
 */
public final class Trie {
    private final int base[];     // BASE配列
    private final int chck[];     // CHECK配列
    private final String tail;    // TAIL配列
    private final int charcode[]; // 文字のコード値から、実際に遷移に用いる値へのマッピングテーブル
    private final BitVector bv;   // ノードに対応するID算出用のビット配列

    Trie(int base[], int chck[], String tail, int charcode[], BitVector bv) {
	this.base = base;
	this.chck = chck;
	this.tail = tail;
	this.charcode = charcode;
	this.bv = bv;
    }

    /**
     * {@link Trie}インスタンスをファイルに保存する。
     *
     * @param filepath 保存するファイルのパス
     * @throws IOException 出力エラーが生じた場合に送出される
     */
    public void save(final String filepath) throws IOException {
	FileMappedOutputStream out = 
	    new FileMappedOutputStream(filepath, 
				       bv.dataSize() +
				       2 * 4 + 
				       (base.length+chck.length+charcode.length)*4 +
				       tail.length()*2);
	try {
	    bv.write(out);
	    
	    out.putInt(base.length);
	    out.putInt(tail.length());
	    
	    for(int i=0; i < 0x10000;     i++) out.putInt(charcode[i]);
	    for(int i=0; i < base.length; i++) out.putInt(base[i]);
	    for(int i=0; i < chck.length; i++) out.putInt(chck[i]);
	    out.putString(tail);
	} finally {
	    out.close();
	}
    }

    /**
     * {@link Trie}インスタンスをファイルから読み込む。
     * 
     * @param filepath {@link Trie}インスタンスのデータを保持するファイルのパス
     * @return {@link Trie}インスタンス
     * @throws IOException 入力エラーが生じた場合に送出される
     */
    public static Trie load(final String filepath) throws IOException {
	FileMappedInputStream in = 
	    new FileMappedInputStream(filepath);
	try {
	    final BitVector bv = new BitVector(in);
	    
	    final int nodeSize = in.getInt();
	    final int tailSize = in.getInt();
	    
	    final int charcode[] = in.getIntArray(0x10000);
	    final int base[] = in.getIntArray(nodeSize);
	    final int chck[] = in.getIntArray(nodeSize);
	    final String tail = in.getString(tailSize);
	    
	    return new Trie(base, chck, tail, charcode, bv);
	} finally {
	    in.close();
	}
    }
 
    /**
     * トライに格納されているキーの数を取得する。
     * @return キー数
     */
    public int keyCount() { return bv.rank(base.length); }
    /**
     * トライを表現するために使用されているノードの数(BASE配列のサイズ)を取得する。
     * @return ノード数
     */
    public int nodeCount() { return base.length; }
    /**
     * TAIL配列のサイズを取得する。
     * @return TAIL配列のサイズ
     */
    public int tailLength() { return tail.length(); }
    
    /**
     * キーを検索する。
     *
     * @param key 検索対象のキーストリーム
     * @return キーのID。キーが存在しない場合は-1が返される。
     */
    public int search(CodeStream key) {
	int node = 0;
	int last = 0; // set arbitrary initial value but -1
	for(;;) {
	    if(base[node] < 0)
		return last==-1 || tailEqual(-base[node], key) ? bv.rank(node) : -1;
	    
	    final int next = base[node] + charcode[(last=key.read())+1];
	    if(chck[next] == node) node = next;
	    else                   return -1; 
	}
    }

    /**
     * キーを検索する。<br />
     * {@code search(new Trie.CharSequenceStream(key))}に等しい。
     *
     * @param key 検索対象のキー文字列。
     * @return キーのID。キーが存在しない場合は-1が返される。
     */
    public int search(final CharSequence key) {
        return search(new CharSequenceCodeStream(key));
    }

    /**
     * 入力キーに対して共通接頭辞検索を行う。<br />
     * 入力キーの接頭部分にマッチするキーがトライ内にある場合は、それが見つかった時点で、処理を呼び出し元に返す。<br />
     * その際には、{@code root}にマッチしたキーのIDおよび現在のノード情報がセットされる。<br />
     * 引き続き、前回用いた{@code key}および{@code root}を渡してメソッドを呼び出すことで、後続部分に対して、共通接頭辞検索が行われる。<br />
     * 全ての共通接頭辞の検索が終了した場合、このメソッドはfalseを返す。
     *
     * @param key 検索対象のキーストリーム
     * @param root 検索開始ノードとノードのIDを保持する{@link Node}インスタンス
     * @return キーに対する全てのマッチングが終了した場合はfalseを、まだマッチングがある可能性が残っている場合はtrueを返す
     */
    public boolean commonPrefixSearch(CodeStream key, Node root) {
        root.id = -1;
        int node = root.node;
        int last = 0; // set arbitrary initial value but -1
        for(;;) {
            if(base[node] < 0) {
                if(last==-1 || tailIncluding(-base[node], key))
                    root.id = bv.rank(node);
                return false;
            }

            if(node != root.node) {                
                final int terminal = base[node] + charcode[0];
                if(chck[terminal] == node) {
                    root.node = node;
                    root.id = bv.rank(terminal);
                    return key.peek()==-1 ? false : true;
                }
            }
	    
	    final int next = base[node] + charcode[(last=key.read())+1];
	    if(chck[next] == node) node = next;
	    else                   return false;
        }
    }

    private boolean tailEqual(final int tailHead, CodeStream in) {
	int i=0;
	for(;; i++, in.read())
	    if(in.peek() != tail.charAt(tailHead+i))
		break;
	return in.peek() ==-1 && tail.charAt(tailHead+i)=='\0';
    }

    private boolean tailIncluding(final int tailHead, CodeStream in) {
        int i = 0;
	for(;; i++, in.read())
	    if(in.peek() != tail.charAt(tailHead+i))
		break;
	return tail.charAt(tailHead+i)=='\0';
    }

    /**
     * Trieのノード。
     */
    public static final class Node {
        protected int node = 0;
        protected int id = -1;

        /**
         * ルートノード用の{@link Node}インスタンスを作成する。
         */
        public Node() {}
        
        /**
         * ノードのIDを返す。
         * @return ノードのID。対応するIDがない場合は-1が返される。
         */
        public int id() { return id; }
    }

    /**
     * トライの検索キーとして使われるストリームクラスのインターフェース。
     */
    public interface CodeStream {
        /**
         * コードを一つ分読み進める。<br />
         * ストリームの終端に達していない場合は、文字のコード値をそのまま返す。<br />
         * ストリームの終端に達している場合は、-1を返す。
         */
        public int read();
        /**
         * コードを一つ分先読みする。<br />
         * ストリームの終端に達していない場合は、文字のコード値をそのまま返す。<br />
         * ストリームの終端に達している場合は、-1を返す。
         */
        public int peek();
    }

    /**
     * {@link CodeStream}の{@link CharSequence}に対する実装。
     */
    public static class CharSequenceCodeStream implements CodeStream {
        private final CharSequence source;
        private int pos = 0;
        
        /**
         * 文字列をもとにストリームを作成する。
         * @param source ストリームのソースとなる文字列
         */
        public CharSequenceCodeStream(CharSequence source) {
            this.source = source;
        }
        
        public int read() {
            return pos < source.length() ? source.charAt(pos++) : -1;
        }
        
        public int peek() {
            return pos < source.length() ? source.charAt(pos) : -1;
        }

        /**
         * ストリーム内での現在位置を返す
         * @return ストリーム内での現在位置
         */
        public int offset() { return pos; }
    }
}
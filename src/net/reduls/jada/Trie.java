package net.reduls.jada;

import java.io.IOException;

/**
 * DoubleArray-Trieクラス
 */
public final class Trie {
    private final int base[];     // BASE配列
    private final int chck[];     // CHECK配列
    private final String tail;    // TAIL配列
    private final int charcode[]; // 文字のコード値から、実際に遷移に用いる値へのマッピングテーブル
    private final BitVector bv;   // ノードに対応するID算出用のビット配列

    /**
     * コンストラクタ
     */
    Trie(int base[], int chck[], String tail, int charcode[], BitVector bv) {
	this.base = base;
	this.chck = chck;
	this.tail = tail;
	this.charcode = charcode;
	this.bv = bv;
    }

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
 


    public int nodeCount() { return base.length; }
    public int tailLength() { return tail.length(); }
    public int keyCount() { return bv.rank(base.length); }

    public int search(CodeStream key) {
	int node = 0;
	int last = 0; // NOTE: set arbitrary initial value but -1
	for(;;) {
	    if(base[node] < 0)
		return last==-1 || tailEqual(-base[node], key) ? bv.rank(node) : -1;
	    
	    final int next = base[node] + charcode[(last=key.read())+1];
	    if(chck[next] == node) node = next;
	    else                   return -1; 
	}
    }

    public int search(final CharSequence key) {
        return search(new CharSequenceCodeStream(key));
    }

    private boolean tailEqual(final int tailHead, CodeStream in) {
	int i=0;
	for(;; i++, in.read())
	    if(in.peek() != tail.charAt(tailHead+i))
		break;
	return in.peek() ==-1 && tail.charAt(tailHead+i)=='\0';
    }

    public boolean commonPrefixSearch(CodeStream key, Node root) {
        int node = root.node;
        int last = 0; // NOTE: set arbitrary initial value but -1
        for(;;) {
	    if(base[node] < 0) {
		if(last==-1 || tailIncluding(-base[node], key))
                    root.id = bv.rank(node);
                return false;
            }

            final int terminal = base[node] + charcode[0];
            if(chck[terminal] == node) {
                root.node = node;
                root.id = bv.rank(node);
                return key.peek()==-1 ? false : true;
            }
	    
	    final int next = base[node] + charcode[(last=key.read())+1];
	    if(chck[next] == node) node = next;
	    else                   return false;
        }
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
        
        public int id() { return id; }
    }

    /**
     * トライの検索キーとして使われるストリームクラスのインターフェース。
     */
    public interface CodeStream {
        public int read();
        public int peek();
    }

    /**
     * {@link CodeStream}の{@link CharSequence}に対する実装。
     */
    public static class CharSequenceCodeStream implements CodeStream {
        private final CharSequence source;
        private int pos = 0;
        
        public CharSequenceCodeStream(CharSequence source) {
            this.source = source;
        }
        
        public int read() {
            return pos < source.length() ? source.charAt(pos++) : -1;
        }
        
        public int peek() {
            return pos < source.length() ? source.charAt(pos) : -1;
        }

        public int offset() { return pos; }
    }
}
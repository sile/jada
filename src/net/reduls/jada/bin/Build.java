package net.reduls.jada.bin;

import net.reduls.jada.TrieBuilder;
import net.reduls.jada.Trie;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * トライ構築用のコマンドクラス。
 */
public final class Build {
    /**
     * ソート済みのキーセットを標準入力から読み込み、トライを構築し、ファイルに保存する。<br />
     * 入力キーセットがソート済みではない、あるいは、各要素がユニークではない場合の動作は未定義。<br />
     * <br />
     * <h><b>【コマンドライン引数】</b></h><br />
     * {@code $ java net.reduls.jada.bin.Build [--shrink] [--bench] index < unique-sorted-key-set}<br />
     * <table border="1">
     * <tr><td><b>--shrink:</b></td><td>指定された場合は、TAIL配列の圧縮を行う。</td></tr>
     * <tr><td><b>--bench:</b></td><td>指定された場合は、実際のトライ構築の前にVMのウォームアップを行う。</td></tr>
     * <tr><td><b>index:</b></td><td>構築したトライを保存するファイルのパス。</td></tr>
     * <tr><td><b>unique-sorted-key-set:</b></td><td>トライ構築対象となるキーセット。ソート済みかつユニーク。標準入力から読み込む。</td></tr>
     * </table>
     *
     * @param args コマンドライン引数
     * @throws IOException 入出力エラーが生じた場合に送出される
     */
    public static void main(String[] args) throws IOException {
        final Argument arg = new Argument(args);
        if(arg.valid==false) {
	    System.err.println("Usage: java net.reduls.jada.bin.Build [--shrink] [--bench] index < unique-sorted-key-set");
	    System.exit(1);
	}

	Time t;

        // read key set
	System.err.println("= Read key set");
	t = new Time();
	List<String> keys = new ArrayList<String>();
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	for(String line=br.readLine(); line!=null; line=br.readLine())
	    keys.add(line);
	System.err.println("  == "+keys.size()+" keys");
	System.err.println("DONE ("+t.elapsed()+" ms passed)");
	System.err.println("");

        // warn-up
        if(arg.bench) {
            System.err.println("= Build warm-up");
            for(int i=0; i < 5; i++) {
                System.err.print("  == loop#"+(i+1)+" ... ");
                t = new Time();
                TrieBuilder bld = new TrieBuilder(keys);
                Trie trie = bld.build(arg.shrink);
                trie.save(arg.indexFilePath);
                System.err.println(t.elapsed()+" ms");
            }
            System.err.println("DONE");
            System.err.println("");
        }

        // build trie
        System.err.println("= Build trie ");
        t = new Time();
        System.err.println("  == initialize");
        TrieBuilder bld = new TrieBuilder(keys);
        System.err.println("  == build");
        Trie trie = bld.build(arg.shrink);
        System.err.println("    === node count:  "+trie.nodeCount());
        System.err.println("    === tail length: "+trie.tailLength());
        
        // save trie
        System.err.println("  == save: "+arg.indexFilePath);
        trie.save(arg.indexFilePath);
        System.err.println("DONE ("+t.elapsed()+" ms passed)");
        System.err.println("");
    }

    private static class Time {
	private final long beg_t = System.currentTimeMillis();
	public long elapsed() { return System.currentTimeMillis()-beg_t; }
    }

    private static class Argument {
        public boolean shrink=false;
        public boolean bench=false;
        public String indexFilePath;
        public boolean valid=false;

        public Argument(String[] args) {
            int i=0;
            for(; i < args.length; i++) {
                if(args[i].startsWith("--") == false)
                    break;
                if(args[i].equals("--shrink")) 
                    shrink=true;
                else if(args[i].equals("--bench"))
                    bench=true;
                else
                    return;
            }

            if(args.length-i != 1)
                return;
            indexFilePath = args[i];
            valid=true;
        }
    }
}
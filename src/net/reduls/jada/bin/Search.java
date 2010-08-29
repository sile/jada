package net.reduls.jada.bin;

import net.reduls.jada.Trie;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.io.Reader;
import java.io.StringReader;

/**
 * トライ検索用のコマンドクラス
 */
public final class Search {
    /**
     * 検索対象のキーリストを標準入力から読み込み、対応するIDを検索/出力する。<br />
     * キーに対応するIDが存在しない場合は、-1が出力される。<br />
     * <br />
     * <h><b>【コマンドライン引数】</b></h><br />
     * {@code $ java net.reduls.jada.bin.Search [--bench] index < key-list > id-list}<br />
     * <table border="1">
     * <tr><td><b>--bench:</b></td><td>指定された場合は、IDの出力ではなく検索処理のベンチマークを行う。</td></tr>
     * <tr><td><b>index:</b></td><td>検索に用いる構築済みトライが保存されているファイルのパス。</td></tr>
     * <tr><td><b>key-list:</b></td><td>検索対象となるキーのリスト。標準入力から読み込む。</td></tr>
     * <tr><td><b>id-list:</b></td><td>入力キーリストに対応するIDのリスト。標準出力に出力される。--benchオプションが指定されている場合は出力なし。</td></tr>
     * </table>
     *
     * @param args コマンドライン引数
     * @throws IOException 入出力エラーが生じた場合に送出される
     */
    public static void main(String[] args) throws IOException {
	if(!(args.length==1 || (args.length==2 && args[0].equals("--bench")))) {
	    System.err.println("Usage: java net.reduls.jada.bin.Search [-bench] index < key-list > key-id-list");
	    System.exit(1);
	}

	final Trie srch = Trie.load(args.length==1 ? args[0] : args[1]);
        final boolean bench = args.length==2;
        if(bench==false) {
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	    for(String line=br.readLine(); line!=null; line=br.readLine())
		System.out.println(srch.search(line));
        } else {
	    Time t;
            
            // read key list
	    System.err.println("= Read key list");
	    t = new Time(); 
            
	    List<String> keys = new ArrayList<String>();
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            double keyTotalLength = 0.0;
	    for(String line=br.readLine(); line!=null; line=br.readLine()) {
		keys.add(line);
                keyTotalLength += line.length();
            }
	    System.err.println("  == "+keys.size()+" keys");
	    System.err.println("  == key length average: "+ ((int)(keyTotalLength/keys.size()*1000)/1000.0));
	    System.err.println("DONE ("+t.elapsed()+" ms passed)");
	    System.err.println("");

            // warn-up
            System.err.println("= Search warn-up");
            for(int i=0; i < 5; i++) {
                System.err.print("  == loop#"+(i+1)+" ... ");
                t = new Time();
                for(String key : keys)
                    srch.search(key);
                System.err.println(t.elapsed()+" ms");
            }
            System.err.println("DONE");
            System.err.println("");

            // search
            System.err.println("= Search");
            t = new Time(); 
            int fails=0;
            for(String key : keys)
                if(srch.search(key)==-1)
                    fails++;
            System.err.println("  == failed: "+fails+"/"+keys.size());
            System.err.println("DONE ("+t.elapsed()+" ms passed)");
            System.err.println("");
	}
    }

    private static class Time {
	private final long beg_t = System.currentTimeMillis();
	public long elapsed() { return System.currentTimeMillis()-beg_t; }
    }
}
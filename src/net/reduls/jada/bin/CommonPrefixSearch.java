package net.reduls.jada.bin;

import net.reduls.jada.Trie;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * トライ共通接頭辞検索用のコマンドクラス
 */
public final class CommonPrefixSearch {
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
	if(args.length!=1) {
	    System.err.println("Usage: java net.reduls.jada.bin.CommonPrefixSearch index < key-list");
	    System.exit(1);
	}

	final Trie srch = Trie.load(args[0]);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        for(String line=br.readLine(); line!=null; line=br.readLine()) {
            System.out.println(line);
            Trie.CharSequenceCodeStream in = new Trie.CharSequenceCodeStream(line);
            Trie.Node node = new Trie.Node();
            while(srch.commonPrefixSearch(in, node))
                System.out.println("\t"+line.substring(0, in.offset())+"#"+node.id());
            if(node.id()!=-1)
                System.out.println("\t"+line.substring(0, in.offset())+"#"+node.id());
        }
    }
}
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
     * 検索対象のキーリストを標準入力から読み込み、共通接頭辞検索を行った結果を標準出力に出力する。<br />
     * <br />
     * <h><b>【出力フォーマット】</b></h><br />
     * 以下の繰り返し:<br />
     * <div style="background:#DDDDDD">
     * 検索対象キー[改行]<br />
     * [タブ文字][マッチした共通接頭文字列][タブ文字][ID値][改行]<br />
     * </div>
     * <br />
     * <h><b>【コマンドライン引数】</b></h><br />
     * {@code $ java net.reduls.jada.bin.CommonPrefixSearch index < key-list}<br />
     * <table border="1">
     * <tr><td><b>index:</b></td><td>検索に用いる構築済みトライが保存されているファイルのパス。</td></tr>
     * <tr><td><b>key-list:</b></td><td>検索対象となるキーのリスト。標準入力から読み込む。</td></tr>
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
                System.out.println("\t"+line.substring(0, in.offset())+"\t"+node.id());
            if(node.id()!=-1)
                System.out.println("\t"+line.substring(0, in.offset())+"\t"+node.id());
        }
    }
}
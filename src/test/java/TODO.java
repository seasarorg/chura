public interface TODO {

	// TODO : 複数のデータベースをUnitテストする方法。
	// TODO : 外部キーを取る。DatabaseMetadataの挙動がイマイチ？
	// FIXME : 接続先URLの設定を行う際に、デフォルト値が設定されていると良い感じ。
	// TODO : DatabaseMetaData検索後に、Elementをダブルクリックしても、ノードが展開しないのはイマイチ。
	// TODO : TypeMappingを選択するロジックを、インテリジェントにする。
	// nullを許容しない型には、プリミティブ型を最初に検索。
	// Numericな型には、桁数に応じた型を割り当てる。
	// TODO : Javaのメンバ変数を入力する画面で入力補完が利く様にする。
	// org.eclipse.jdt.ui.text.java.CompletionProposalCollector辺りを使えば出来るっぽい。
	// TODO : TypeMappingのSQL_TYPENAMESを、色んなRDB対応する為に拡充する事。
	// TODO : Entity と Daoを作成する際に、デフォルトのパッケージ名が入力されると良いんだけど…。
	// ダイアログの初期化の時に、DialogConfigから取ってくる様に、NewClassWizardPageをサブクラス化して実装する。
	// FIXME : S2コンテナの初期化及び、DBコネクションの取得は、接続設定ツリーが、最初に展開された時に行う。
	// FIXME : ConnectionConfig単位に保持しているS2Containerを破棄するタイミングを考える事。
}

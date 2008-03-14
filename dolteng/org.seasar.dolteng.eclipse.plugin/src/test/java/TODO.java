public interface TODO {

    // TODO : 複数のデータベースをUnitテストする方法。
    // TODO : 外部キーを取る。DatabaseMetadataの挙動がイマイチ？
    // FIXED : 接続先URLの設定を行う際に、デフォルト値が設定されていると良い感じ。jdbc.diconを探して使う
    // FIXED :
    // プロジェクトのプロパティで、Entity,Daoのデフォルトの出力先が設定されていると良い感じ。convention.diconを探して使う。
    // FIXED : WizardでEntity と Daoを作成する際に、デフォルトのパッケージ名が入力されると良いんだけど…。
    // ダイアログの初期化の時に、DialogConfigから取ってくる様に、NewClassWizardPageをサブクラス化して実装する。
    // TODO : DatabaseMetaData検索後に、Elementをダブルクリックしても、ノードが展開しないのはイマイチ。
    // TODO : Javaのメンバ変数を入力する画面で入力補完が利く様にする。
    // org.eclipse.jdt.ui.text.java.CompletionProposalCollector辺りを使えば出来るっぽい。
    // FIXED : TypeMappingのSQL_TYPENAMESを、色んなRDB対応する為に拡充する事。
    // FIXED : S2コンテナの初期化及び、DBコネクションの取得は、接続設定ツリーが、最初に展開された時に行う。
    // FIXED : ConnectionConfig単位に保持しているS2Containerを破棄するタイミングを考える事。
    // FIXME : jdbc.diconが変更された時に、DatabaseViewをRefreshする方法を見つける。
    // FIXED : ENVを基準に設定をロードする。
    // FIXED : ChuraProjectWizardのルートパッケージ名に、正規表現として妥当な文字列が入るとヲカシナ事になる。
    // FIXME : DatabaseView で接続を失敗した時に、もっとエラーメッセージが出る様にする。
}

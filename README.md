# 工業関数電卓

工業系の公式プロファイルを内蔵したAndroid向け関数電卓アプリです。Casio fx-375ES系のボタン配置を意識しつつ、公式呼び出しやプリセット保存を使いやすくまとめています。

## Features

- 関数電卓入力
  - 四則演算
  - 三角関数
  - 対数
  - 平方根
  - 累乗
  - Ans入力
- 数学表示
  - 点滅カーソル
  - タップによるカーソル移動
  - 十字キーによるカーソル移動
  - 分数の横棒表示
- 工業公式プロファイル
  - 機械
  - 電気
  - 土木・熱
- プリセット
  - よく使う式や公式を保存
  - 保存済み式の呼び出し

## APK

署名付きの最新版APKはリポジトリ直下の `calculator-release-signed.apk` からダウンロードできます。

過去バージョンの署名付きAPKは `releases/` に保存します。

```text
releases/
  calculator-release-v1.0.0.apk
  calculator-release-v1.0.1.apk
  calculator-release-v1.0.2.apk
```

## Build

Android Studioでこのプロジェクトを開き、通常のDebugビルドを実行してください。

コマンドでビルドする場合:

```powershell
.\gradlew.bat :app:assembleDebug
```

生成されるAPK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Changelog

### v1.0.2

#### 追加

- SHIFT / ALPHA キーを実装
- MODEで RAD / DEG 切り替えに対応
- SETUPで表示形式とカーソル点滅速度を変更可能に
- 計算履歴を追加
  - `↑` / `↓` で履歴表示
  - `ON` で履歴消去

#### 改善

- `S⇔D` で小数 / 分数表示を切り替え可能に
- 分数結果を横棒表示に変更
- テンキー周りの余白とボタンサイズを調整

#### 修正

- `÷` が分数横棒として表示される問題を修正
- 隠しコマンドを `114514` 入力後に `=` で実行するよう変更

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle

## License

This project is licensed under the MIT License.

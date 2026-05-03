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

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Gradle

## License

This project is licensed under the MIT License.

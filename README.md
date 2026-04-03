# game-server

Strategy Games サーバーのゲームロジック実装リポジトリ。

Minecraft (Paper) のブリッジプラグインと、ゲームロジックを担う Spring Boot API の2モジュール構成。

```
game-server/
├── api/        # Spring Boot ゲームロジックAPI (Java 21)
├── plugin/     # MCブリッジプラグイン (Paper 1.21)
└── .github/workflows/
    ├── build-api.yml     # api/ 変更時のみビルド → PVC配置
    └── build-plugin.yml  # plugin/ 変更時のみビルド → PVC配置
```

---

## アーキテクチャ概要

```
【MCブリッジプラグイン (Paper JAR)】      【ゲームロジックAPI (Spring Boot)】
─────────────────────────────────       ────────────────────────────────────
・プレイヤーのコマンド受付               ・研究ツリー進捗・解禁判定
・インベントリGUI表示                    ・メガエンジニアリング状態管理
・Towny連携 (国家情報の取得)             ・ICBM 射程/ダメージ計算
・ワールドへの副作用実行                  ・ディシジョン効果計算
・RCONで受け取ったコマンドの実行          ・タイマー / クールダウン管理
                                        ・国家統計・資源集計
```

プラグインは HTTP で API を呼び出し、API は RCON でMinecraftに副作用を返す。

---

## api/ — ゲームロジックAPI

### 技術スタック

| 項目 | 内容 |
|------|------|
| フレームワーク | Spring Boot 3.3 |
| Java | 21 |
| DB | MariaDB 11 (JPA + Flyway) |
| マイグレーション | `V1__init_schema.sql` / `V2__seed_technologies.sql` |
| ヘルスチェック | `/actuator/health/readiness` `/actuator/health/liveness` |

### エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| `GET`  | `/research/candidates?nationId=` | Stellaris風 研究候補リスト取得 (国家特性で重み付け) |
| `POST` | `/research/start` | 研究開始 |
| `GET`  | `/icbm/status?nationId=` | ICBMサイロ状態確認 |
| `POST` | `/icbm/build?nationId=` | サイロ建設開始 (~7日) |
| `POST` | `/icbm/launch` | ICBM発射 → 非破壊デバフ付与 |
| `GET`  | `/decision/available?nationId=` | クールダウン中でないディシジョン一覧 |
| `GET`  | `/decision/list` | 全ディシジョン定義 (CD・コスト) |
| `POST` | `/decision/execute` | ディシジョン実行 |
| `GET`  | `/nation/{id}/stats` | 国家統計 (実効デバフ込み) |

### ビルド

```bash
./gradlew :api:bootJar
# → api/build/libs/game-logic-api.jar
```

### 設定

環境変数で上書き可能 (デフォルト値はローカル開発用):

| 環境変数 | デフォルト | 説明 |
|---------|-----------|------|
| `DB_HOST` | `localhost` | MariaDB ホスト |
| `DB_PORT` | `3306` | MariaDB ポート |
| `DB_NAME` | `strategy_games` | DB名 |
| `DB_USER` | `sgames` | DBユーザー |
| `DB_PASSWORD` | `changeme` | DBパスワード |
| `RCON_HOST` | `localhost` | Minecraft RCON ホスト |
| `RCON_PORT` | `25575` | RCON ポート |
| `RCON_PASSWORD` | `changeme` | RCON パスワード |
| `RCON_ENABLED` | `true` | RCON送信を無効化する場合は `false` |

debug環境では `SPRING_PROFILES_ACTIVE=debug` を設定すると `application-debug.yml` が有効になり、クラスタ内ホスト名が自動適用される。

---

## plugin/ — MCブリッジプラグイン

### 技術スタック

| 項目 | 内容 |
|------|------|
| API | Paper 1.21.4 |
| Java | 21 |
| HTTP クライアント | Java 21 標準 `HttpClient` |
| JSON | Gson (Paper同梱) |
| Towny連携 | リフレクションによるsoft-depend (コンパイル時依存なし) |

### コマンド

| コマンド | 権限 | 説明 |
|---------|------|------|
| `/research candidates` | `strategygames.research` | 研究候補一覧 |
| `/research start <techId>` | `strategygames.research` | 研究開始 |
| `/icbm status` | `strategygames.icbm` | サイロ状態確認 |
| `/icbm build` | `strategygames.icbm` | サイロ建設開始 |
| `/icbm launch <targetNation>` | `strategygames.icbm` | ICBM発射 (op限定) |
| `/decision list` | `strategygames.decision` | 利用可能ディシジョン一覧 |
| `/decision execute <id> [targetNation]` | `strategygames.decision` | ディシジョン実行 |
| `/nation` | `strategygames.nation` | 自国の統計表示 |

### 設定 (plugins/GameServerPlugin/config.yml)

```yaml
api:
  url: "http://game-logic-api:8080"  # ← k8s ClusterIP
```

### ビルド

```bash
./gradlew :plugin:jar
# → plugin/build/libs/game-server-plugin.jar
```

---

## CI/CD

GitHub Actions でパス変更を検知し、該当モジュールのみビルド・デプロイ。

| ワークフロー | トリガーパス | 説明 |
|-------------|------------|------|
| `build-api.yml` | `api/**` | API JAR をビルド。`debug` ブランチは k8s Pod に自動配置 + rollout restart |
| `build-plugin.yml` | `plugin/**` | Plugin JAR をビルド。`debug` ブランチは MC Pod の `/data/plugins/` に自動配置 |

### 必要なGitHub Secrets

| Secret | 説明 |
|--------|------|
| `KUBECONFIG_DEBUG` | debug k8s クラスタの kubeconfig を base64 エンコードしたもの |

### デプロイ先 (Namespace: `minecraft-debug`)

| モジュール | デプロイ先 PVC | Pod ラベル |
|-----------|--------------|-----------|
| API JAR | `game-logic-api-jar` (1Gi RWX) | `app=game-logic-api` |
| Plugin JAR | `minecraft-debug-plugins` (5Gi RWX) | `app=minecraft-debug` |

Plugin 反映には `/reload confirm` またはPod再起動が必要。

---

## ローカル開発

### 前提条件

- Java 21+
- MariaDB 11 (ローカルまたはDocker)

### MariaDB をDockerで起動

```bash
docker run -d \
  --name sgames-db \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=strategy_games \
  -e MYSQL_USER=sgames \
  -e MYSQL_PASSWORD=changeme \
  -p 3306:3306 \
  mariadb:11-bookworm
```

### API 起動

```bash
./gradlew :api:bootRun
# Flyway が起動時に V1/V2 migration を自動適用
```

### Plugin ビルドして MCサーバーへ配置

```bash
./gradlew :plugin:jar
cp plugin/build/libs/game-server-plugin.jar /path/to/paper/plugins/
```

---

## ブランチ運用

| ブランチ | 用途 |
|---------|------|
| `main` | 安定版。本番デプロイ対象 |
| `debug` | debug k8s環境への自動デプロイ対象。ArgoCD が `minecraft-debug` namespace を監視 |

---

## 関連リポジトリ

| リポジトリ | 内容 |
|-----------|------|
| `infra-repo` (private) | k8s マニフェスト・ArgoCD ApplicationSet 定義 |

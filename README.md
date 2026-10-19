# hata — 機（conductor）

**「機」は機織りの metaphor である。** 織機そのもの —— 紋（DNA）に従って糸（source
chain）を織る、agent の常駐ランタイム。Holochain の **conductor** にあたる。名前は
amu（編む）・osaho（筬）・abi（経）・kototama（言霊）と同じ機織りの語系に並ぶ。

root ADR-2610082200（Kotoba の app 面を agent 中心にする）の P3。1 つの
principal の 1 台の device（または 1 bot）に 1 つ常駐する。

## 持つもの（ADR-2610082200 §2）

| 役割 | 実体 |
|---|---|
| agent 鍵の保管 | device 鍵（did:key、Ed25519）を預かる。署名は注入された sign-fn |
| cell の起動 | cell = 紋 CID × agent 鍵。genesis で membrane 証明を検査 |
| 糸 の記録 | 署名付き action の連鎖（hata.ito）。分岐・逆転は fail-closed |
| 布 への参加 | P4（未実装） |
| UI への局所 API | P3 後続 |
| cell 間の呼び出し・remote call | P5 |

## 持たないもの

- **権限の判断（grant）** — しない。admit の判定は注入する（kotoba-lang/grant）。
- **配置（sahai）** — しない（kotoba-lang/sahai）。
- **VM の定義** — しない（kotoba-lang/kototama）。**実行は kototama の 宿
  （kototama-native / kototama-component / wasm-webcomponent）に渡す。** 1 つの
  機 が複数の 宿 を使ってよい。宿 とは別物である。
- engine・loader・provider をこの repo に積まない（kototama の hosts.md と同じ規則）。

## 形態

pure な `.cljc`。暗号・CID・実行はすべて注入（sign-fn / verify-fn / cid-fn /
host-fn / membrane-check-fn）で、この repo は決定の形だけを持つ。

    kbb -M:test

P3 合否条件 (4)「bundle CID から再構築」は実 digest（DAG-CBOR CIDv1）と実
CAR（CARv2）で測る。codec が classpath に無いと読めない別 runner に置いて
ある — CID と CAR のない「再構築できた」は測ってはいけない主張だから:

    kbb --backend sci --classpath \
      src:test:../io-ipld-car/src:../io-ipld/src:../io-multiformats/src:../bytes/src:../coll/src:../edn/src:../error/src:../hash/src:../io/src:../pprint/src:../text/src:../org-nist-sha2/src:../org-ietf-cbor/src \
      test/hata/run_rebuild.cljk

## UI への局所 API

`hata.ui/serve-plan` は bundle の UI entry と型・path を返すだけ。表示・DOM・
ネットは 宿 の仕事で、origin は 機 の loopback（app origin は static 配信のみ、
共通アーキテクチャ契約どおり）。

## loopback 通し（実 HTTP transport）

P3 合否条件 (1)(2)(3) + UI を実 HTTP（loopback）で通す実演:

    kbb --backend sci --classpath src:test:<codec paths> bin/loopback_demo.cljk

2026-10-19 実測で OPENED → RECEIPT → ITO [:open :commit] → UI-SERVED まで
全経路が通る。実 IPFS/IPNS plane だけが DNS 不通で未測定。

## 実ネットワーク harness

app DID だけで bundle を開く経路の実走査:

    kbb --backend sci --classpath src:<codec paths> bin/open_bundle.cljk \
        --app-did did:key:... [--name k51...] [--gateway https://<host>]

`--name` を省けば `hata.didkey` が app DID から IPNS name を導出する
（ed25519 鍵 → tech-ipfs-specs-ipns の pubkey->name。実測ベクタは
didkey-test）。導出できない DID は「測れなかった」で止める。

exit は 3 値（content-address の CLI と同じ規則）: 0 = 開けた、1 = 開けない
（**応答有り**の拒否、HTTP status を印字）、2 = **測れなかった**（名前解決
不通・接続不能）。測れなかったことを合格にしない。

2026-10-19 実測（導出済み name `k51qzi5...7127` で走査）:
- `kotobase.net` → `UNANSWERED ... exit 2`（A レコードが無い。yataverse.com /
  itonami.app も同じ）
- `ipfs.io` → `REFUSED 429 ... exit 1`（応答は有る。rate limit）
- `kotoba.cloud` は応答するが `PUT 405` の static origin（app origin は
  static 配信のみの契約どおり）

bytes 面の名前解決・publish plane が戻るまで実走査は exit 2 のまま。

## ステータス

P3 の最小形の骨組み（糸・cell・機 の record/execute の分離）。布・UI API・
remote call・bundle 起動は未実装。合否条件（ドメイン無しで app DID だけから
bundle を開く）は後続 PR。

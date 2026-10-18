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

## ステータス

P3 の最小形の骨組み（糸・cell・機 の record/execute の分離）。布・UI API・
remote call・bundle 起動は未実装。合否条件（ドメイン無しで app DID だけから
bundle を開く）は後続 PR。

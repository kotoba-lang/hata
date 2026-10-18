(ns hata.conductor
  "機（hata）—— conductor。紋 に従って 糸 を織る常駐のランタイム。

  root ADR-2610082200（§2）。1 つの principal の 1 台の device（または 1
  bot）に 1 つ。持つもの: 鍵の預かり（署名は注入 sign-fn）、cell の起動、
  糸 の記録、UI への局所 API（P3 後続）、cell 間・remote call（P5）。

  持たないもの: 権限の判断（grant = 注入 admit-fn）、配置（sahai）、VM の
  定義（kototama）。実行は 宿 に渡す — host-fn が 宿
  （kototama-native / kototama-component / wasm-webcomponent）への入口。"
  (:require [hata.cell :as cell]))

(defn resident
  "One 機 per device. Every seam is injected; this repo owns no crypto,
  no engine and no placement."
  [{:keys [cid-fn sign-fn verify-fn membrane-check-fn host-fn admit-fn]
    :as seams}]
  (assoc seams :cells (atom {})))

(defn start-cell!
  "Open a cell under this 機. Refused membranes leave no state behind. The
  membrane check comes from the 機's seams, not from the caller's pocket."
  [machine {:keys [mon-cid agent-key] :as opts}]
  (let [result (cell/open (merge machine opts))]
    (if (:ok result)
      (do (swap! (:cells machine) assoc [mon-cid agent-key] (:ok result))
          {:ok [mon-cid agent-key]})
      result)))

(defn record!
  "Record a signed action on a cell's 糸. Nothing is recorded when the chain
  refuses it — the return names why."
  [machine cell-id entry]
  (let [cell (get @(:cells machine) cell-id)]
    (if (nil? cell)
      {:problems [{:problem :no-such-cell :cell cell-id}]}
      (let [result (cell/append machine cell entry)]
        (if (:ok result)
          (do (swap! (:cells machine) assoc cell-id (:ok result))
              {:ok cell-id})
          result)))))

(defn execute
  "Delegate execution to the 宿. The 機 does not decide permission: when an
  admit-fn is present it is consulted first and a refusal sends nothing to
  the host — but the decision itself belongs to grant."
  [machine cell-id call]
  (let [cell (get @(:cells machine) cell-id)]
    (cond
      (nil? cell) {:problems [{:problem :no-such-cell :cell cell-id}]}

      (and (:admit-fn machine)
           (not ((:admit-fn machine) cell call)))
      {:problems [{:problem :admit-refused :call call}]}

      :else {:ok ((:host-fn machine) cell call)})))

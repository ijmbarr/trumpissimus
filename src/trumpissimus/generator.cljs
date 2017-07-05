(ns trumpissimus.generator
  (:require [ajax.core :as aj]
            [clojure.string :as st]))

(defn get-weighted-nth
  [chain n]
  (ffirst
    (drop-while
      #(< (second %) n)
      (reductions
        (fn [e1 e2] [(first e2) (+ (second e1) (second e2))])
        chain))))

(defn get-next-token
  [chain state]
  (let [possible-states (get chain state)
        n (reduce + (vals possible-states))
        n-choice (rand-int n)]
    (get-weighted-nth possible-states n-choice)))


(defn choose-chain
  [c1 c2 state mix-param]
  (cond
    (nil? c2) c1
    (and (contains? c1 state) (not (contains? c2 state))) c1
    (and (not (contains? c1 state)) (contains? c2 state)) c2
    (and (contains? c1 state) (contains? c2 state)) (if (<= mix-param 0) c1 c2)
    :else (.error js/console (str "State not in either chain: " state))))

(defn generate-raw
  [c1 c2 state mix-param]
  (let [current-chain (choose-chain c1 c2 state mix-param)
        next-token (get-next-token current-chain state)
        new-mix-param (if (= current-chain c1) 1 -1)
        new-state (conj (vec (rest state)) next-token)]
    (cons
      next-token
      (lazy-seq
        (generate-raw c1 c2 new-state new-mix-param)))))

(defn generate
  [c1 c2 & {:keys [length mix-param initial-state]
            :or {length 300
                 mix-param 0
                 initial-state nil}}]
  (let [state (if (nil? initial-state)
                (rand-nth (keys c1))
                initial-state)]
    (take length
          (concat
            state
            (generate-raw c1 c2 state mix-param)))))


(defn fix-key [k]
  (as-> k v
        (st/split v #"\|")))

(defn fix-keys
  [mp]
  (into {} (for [[k v] mp] [(fix-key k) v])))

(defn in?
  [coll elm]
  (some #(= elm %) coll))

(def no-space-space ["." ","])
(def no-space-no-space ["-" "..."])

(defn detokenizer
  ([tokens] (detokenizer "" tokens))

  ([current [f & r]]
   (cond
     (nil? f) current
     (in? no-space-no-space f) (detokenizer (str current f) r)
     (in? no-space-space f) (detokenizer (str (apply str (drop-last current)) f " ") r)
     :else (detokenizer (str current f " ") r))))


(defn tidy [tokens] (st/split
                      (str "..."
                          (detokenizer tokens)
                          "...")
                      #"\n"))

(defn doit [c1 c2] (tidy (generate c1 c2)))

;(defn handler-load-atom
;  [atom]
;  (fn [response]
;    (println (take 50 response))
;    (if true
;      (reset! atom (fix-keys (js->clj (.parse js/JSON response))))
;      (.error js/console (str response)))))

(defn handler-load-atom
    [atom button-atom]
    (fn [response]
      (reset! atom (fix-keys response))
      (reset! button-atom "Generate")))


(defn load-text
  [filename handler]
  (aj/GET (str "texts/" filename) {:handler handler}))

; (def a (atom nil))
; (load-text "trump.json" (handler-load-atom a))


;(defn load-text
;  [filename handler]
;  (aj/ajax-request
;    {:uri     (str "texts/" filename)
;     :method  :get
;     :handler handler
;     ;     :format          (aj/json-request-format)
;     ; :response-format (aj/json-response-format {:keywords? false})
;     }))
(ns trumpissimus.core
  (:require [trumpissimus.generator :as gen]
            [rum.core :as rum]))

(enable-console-print!)

(def chain-one (atom nil))
(def chain-two (atom nil))
(def generated (atom ""))
(def selections [{:name "Just Trump" :file "trump"}
                 {:name "Trump vs P.G. Wodehouse" :file "pgw.json"}
                 {:name "Trump vs Harry Potter" :file "hp.json"}
                 {:name "Trump vs Metallica" :file "metallica.json"}])

(gen/load-text "trump.json" (gen/handler-load-atom chain-one))

(defn update-second-chain []
  (let [selected (js/document.getElementById "sel")
        selected-value (.-value selected)]
    (if (= selected-value "trump")
      (reset! chain-two nil)
      (gen/load-text selected-value (gen/handler-load-atom chain-two)))))

(rum/defc generated-placehold []
          [:div
           nil
           (map #(vector :p nil %) @generated)])

(rum/defc selector []
          [:div nil
           [:select
            {:id "sel" :on-change update-second-chain}
            (map #(vector :option {:value (:file %1)} (:name %1)) selections)]
           [:button
            {:on-click (fn [_] (reset! generated
                                       (gen/doit @chain-one @chain-two)
                                       (rum/mount (generated-placehold) (. js/document getElementById "result"))
                                       ))}
            "Generate"]])




(rum/mount (selector) (. js/document getElementById "selector"))
(rum/mount (generated-placehold) (. js/document getElementById "result"))







(ns trumpissimus.core
  (:require [trumpissimus.generator :as gen]
            [rum.core :as rum]))

(enable-console-print!)

(def button (atom "Loading"))
(def chain-one (atom nil))
(def chain-two (atom nil))
(def generated (atom ""))
(def selections [{:name "Just Trump" :file "trump"}
                 {:name "Trump vs P.G. Wodehouse" :file "pgw.json"}
                 {:name "Trump vs Harry Potter" :file "hp.json"}
                 {:name "Trump vs Metallica" :file "metallica.json"}])

(gen/load-text "trump.json" (gen/handler-load-atom chain-one button))

(defn update-second-chain []
  (let [selected (js/document.getElementById "sel")
        selected-value (.-value selected)]
    (reset! button "Loading")
    (if (= selected-value "trump")
      (do
        (reset! chain-two nil)
        (reset! button "Generate"))
      (gen/load-text selected-value (gen/handler-load-atom chain-two button)))))

(defn clicked []
  (reset! generated (gen/doit @chain-one @chain-two)))

(rum/defc generated-placehold < rum/reactive []
          [:div
           nil
           (map #(vector :p nil %) (rum/react generated))])

(rum/defc selector < rum/reactive []
          [:div nil
           [:select
            {:id "sel" :on-change update-second-chain}
            (map #(vector :option {:value (:file %1)} (:name %1)) selections)]
           [:button
            {:on-click clicked
             :disabled (if (= (rum/react button) "Loading")
                         true
                         false)}
            (rum/react button)]])




(rum/mount (selector) (. js/document getElementById "selector"))
(rum/mount (generated-placehold) (. js/document getElementById "result"))







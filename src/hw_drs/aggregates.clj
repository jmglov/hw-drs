(ns hw-drs.aggregates
  (:require [hw-drs.core :as core]
            [clojure.string :as string]))

(defn amount->bucket [amount]
  (cond
    (< amount 10) "<10"
    (and (>= amount 10) (< amount 50)) "10-50"
    (and (>= amount 50) (< amount 100)) "50-100"
    (and (>= amount 100) (< amount 500)) "100-500"
    :default ">500"))

(def aggregators {:hour-amount (fn [{:keys [date amount]}]
                                 (string/join "|" [(-> date core/parse-ts core/render-hours)
                                                   (amount->bucket amount)]))
                  :hour-amount-pm (fn [{:keys [date amount paymentMethod]}]
                                    (string/join "|" [(-> date core/parse-ts core/render-hours)
                                                      (amount->bucket amount)
                                                      paymentMethod]))
                  :amount-pm (fn [{:keys [amount paymentMethod]}]
                               (string/join "|" [(amount->bucket amount)
                                                 paymentMethod]))
                  :day-merchant (fn [{:keys [date merchantId]}]
                                  (string/join "|" [(-> date core/parse-ts core/render-days)
                                                    merchantId]))
                  :merchant-pm (fn [{:keys [merchantId paymentMethod]}]
                                 (string/join "|" [merchantId
                                                   paymentMethod]))})

(defn event->datapoint [event report-type]
  ((aggregators report-type) event))

(defn events->aggregates [events report-type]
  {:aggregates
   (->> events
        (reduce (fn [agg event]
                  (update agg (event->datapoint event report-type) #((fnil inc 0) %)))
                {})
        (map (fn [[datapoint events]] {:datapoint datapoint, :events events})))})

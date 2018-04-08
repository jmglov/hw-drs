(ns hw-drs.aggregates
  (:require [hw-drs.core :as core]
            [clojure.string :as string]))

(def aggregators {:hour-amount (fn [{:keys [date amount]}]
                                 (string/join "|" [(-> date core/parse-ts core/render-hours)
                                                   amount]))
                  :hour-amount-pm (fn [{:keys [date amount paymentMethod]}]
                                    (string/join "|" [(-> date core/parse-ts core/render-hours)
                                                      amount
                                                      paymentMethod]))
                  :amount-pm (fn [{:keys [amount paymentMethod]}]
                               (string/join "|" [amount
                                                 paymentMethod]))
                  :day-merchant (fn [{:keys [date merchantId]}]
                                  (string/join "|" [(-> date core/parse-ts core/render-days)
                                                    merchantId]))
                  :merchant-pm (fn [{:keys [merchantId paymentMethod]}]
                                 (string/join "|" [merchantId
                                                   paymentMethod]))})

(defn event->aggregate [event report-type]
  ((aggregators report-type) event))

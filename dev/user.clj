(ns user
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sns :as sns]
            [amazonica.aws.sqs :as sqs]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]
            [hw-drs.config :as config]
            [hw-drs.core :as core]
            [hw-drs.models :as models])
  (:import (java.time Instant
                      ZonedDateTime
                      ZoneOffset)))

(defn refresh-aws-credentials []
  (amazonica/defcredential (amazonica/get-credentials {:profile "aws-dojo"})))

(defn render-ts [ts]
  (.format models/ts-formatter (.atOffset (Instant/ofEpochMilli ts) ZoneOffset/UTC)))

(def datetime-generator (gen/fmap
                         (fn [[year month day hour minute sec]]
                           (.format models/ts-formatter
                                    (ZonedDateTime/of year month day hour minute sec 0 (ZoneOffset/UTC))))
                         (s/gen (s/cat :year (s/int-in 0 2019)
                                       :month (s/int-in 1 13)
                                       :day (s/int-in 1 29)
                                       :hour (s/int-in 0 24)
                                       :minute (s/int-in 0 60)
                                       :sec (s/int-in 0 60)))))

(def event-generator (s/gen :purchase/event
                            {:purchase/date (constantly datetime-generator)
                             :purchase/amount (constantly (s/gen (s/int-in 1 50000)))
                             :purchase/merchantId (constantly (gen/fmap str (gen/uuid)))}))

(def report-generators {:hw-drw-report-hour-amount [(s/gen (s/int-in 0 24))
                                                    (s/gen (s/int-in 1 50000))]
                        :hw-drw-report-hour-amount-pm [(s/gen (s/int-in 0 24))
                                                       (s/gen (s/int-in 1 50000))
                                                       (s/gen :purchase/paymentMethod)]
                        :hw-drw-report-amount-pm [(s/gen (s/int-in 1 50000))
                                                  (s/gen :purchase/paymentMethod)]
                        :hw-drw-report-day-merchant [(s/gen (s/int-in 1 29))
                                                     (gen/fmap str (gen/uuid))]
                        :hw-drw-report-merchant-pm [(gen/fmap str (gen/uuid))
                                                    (s/gen :purchase/paymentMethod)]})

(def datapoint-generator (gen/fmap (fn [report-type]
                                     (->> (report-generators report-type)
                                          (map gen/generate)
                                          (string/join "|")))
                                   (s/gen #{:hw-drw-report-hour-amount
                                            :hw-drw-report-hour-amount-pm
                                            :hw-drw-report-amount-pm
                                            :hw-drw-report-day-merchant
                                            :hw-drw-report-merchant-pm})))

(def aggregates-generator (s/gen :aggregate/aggregates
                                 {:aggregate/datapoint (constantly datapoint-generator)}))

(defn publish-event []
  (sns/publish :topic-arn config/sns-purchase-events
               :message (json/generate-string (gen/generate event-generator))))

(defn publish-aggregates []
  (sns/publish :topic-arn config/sns-aggregates
               :message (json/generate-string {:aggregates (gen/generate aggregates-generator)})))

(ns user
  (:require [amazonica.core :as amazonica]
            [amazonica.aws.sns :as sns]
            [amazonica.aws.sqs :as sqs]
            [cheshire.core :as json]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [hw-drs.config :as config]
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
                                       :day (s/int-in 1 28)
                                       :hour (s/int-in 0 24)
                                       :minute (s/int-in 0 60)
                                       :sec (s/int-in 0 60)))))

(def event-generator (s/gen :purchase/event
                            {:purchase/date (constantly datetime-generator)
                             :purchase/amount (constantly (s/gen (s/int-in 1 50000)))
                             :purchase/merchantId (constantly (gen/fmap str (gen/uuid)))}))

(defn publish-event []
  (sns/publish :topic-arn config/sns-purchase-events
               :message (json/generate-string (gen/generate event-generator))))

(defn read-q [q]
  (map (fn [{:keys [body]}] (-> body
                                (json/parse-string true)
                                :Message
                                (json/parse-string true)))
       (reduce
        (fn [acc _]
          (let [{:keys [messages]} (sqs/receive-message :queue-url q
                                                        :max-number-of-messages 10
                                                        :delete true)]
            (if (not-empty messages)
              (concat acc messages)
              (reduced acc))))
        []
        (range))))

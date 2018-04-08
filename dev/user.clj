(ns user
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [hw-drs.models :as models])
  (:import (java.time Instant
                      ZonedDateTime
                      ZoneOffset)))

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
                             :purchase/merchantId (constantly (gen/fmap str (gen/uuid)))}))

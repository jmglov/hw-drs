(ns hw-drs.models
  (:require [clojure.spec.alpha :as s])
  (:import (java.time ZonedDateTime)
           (java.time.format DateTimeFormatter)
           (java.util UUID)))

(def ts-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ssX"))

(defn iso-8601-instant? [s]
  (boolean
   (try (ZonedDateTime/parse s ts-formatter)
        (catch Throwable _))))

(defn uuid-str? [s]
  (boolean
   (try (UUID/fromString s)
        (catch Throwable _))))

(s/def :purchase/date iso-8601-instant?)
(s/def :purchase/amount nat-int?)
(s/def :purchase/paymentMethod #{"PAY_NOW" "PAY_LATER" "SLICE_IT"})
(s/def :purchase/merchantId uuid-str?)

(s/def :purchase/event (s/keys :req-un [:purchase/date
                                        :purchase/amount
                                        :purchase/paymentMethod
                                        :purchase/merchantId]))

(s/def :aggregate/datapoint string?)
(s/def :aggregate/events nat-int?)

(s/def :aggregate/aggregate (s/keys :req-un [:aggregate/datapoint
                                             :aggregate/events]))

(s/def :aggregate/aggregates (s/coll-of :aggregate/aggregate))

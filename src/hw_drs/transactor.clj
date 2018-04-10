(ns hw-drs.transactor
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [hw-drs.aggregates :as aggregates]
            [hw-drs.config :as config]
            [hw-drs.core :as core]
            [uswitch.lambada.core :refer [deflambdafn]]))

(defn consume-aggregates! [msg ctx]
  (core/consume-q! config/sqs-aggregates config/max-aggregates))

(defn persist-aggregates! [{:keys [aggregates]} ctx]
  (doseq [datapoint aggregates]
    (let [cur-events (get (core/read-item config/aggregates-table datapoint)
                          :events 0)]
      (core/write-item! config/aggregates-table
                        (update datapoint :events + cur-events)))))

(deflambdafn se.helloworld.drs.Transactor [in out ctx]
  (-> (json/parse-stream (io/reader in) true)
      (consume-aggregates! ctx)
      (persist-aggregates! ctx)
      (json/generate-stream (io/writer out))))

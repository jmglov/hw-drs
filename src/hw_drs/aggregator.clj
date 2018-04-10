(ns hw-drs.aggregator
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [hw-drs.aggregates :as aggregates]
            [hw-drs.config :as config]
            [hw-drs.core :as core]
            [uswitch.lambada.core :refer [deflambdafn]]))

(defn lambda-name [ctx]
  (.getFunctionName ctx))

(defn q-uri [ctx]
  (str "https://sqs.eu-west-1.amazonaws.com/166399666252/" (lambda-name ctx)))

(defn report-type [ctx]
  (-> (lambda-name ctx)
      (string/replace #"^hw-drs-report-" "")
      keyword))

(defn consume-events! [msg ctx]
  (core/consume-q! (q-uri ctx) config/max-events))

(defn aggregate [events ctx]
  (aggregates/events->aggregates events (report-type ctx)))

(defn publish-aggregates [aggregates])

(deflambdafn se.helloworld.drs.Aggregator [in out ctx]
  (-> (json/parse-stream (io/reader in) true)
      (consume-events! ctx)
      (aggregate ctx)
      (publish-aggregates ctx)
      (json/generate-stream (io/writer out))))

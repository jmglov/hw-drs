(ns hw-drs.core
  (:require [amazonica.aws.sqs :as sqs]
            [cheshire.core :as json]))

(defn read-q [q max-num]
  (->> (reduce
        (fn [{:keys [remaining] :as acc} _]
          (let [{:keys [messages]} (sqs/receive-message :queue-url q
                                                        :max-number-of-messages (min remaining 10)
                                                        :delete true)]
            (let [acc (-> acc
                          (update :remaining - (count messages))
                          (update :messages concat messages))]
              (if (and (not-empty messages) (> remaining 0))
                acc
                (reduced acc)))))
        {:remaining max-num, :messages []}
        (range))
       :messages
       (map (fn [{:keys [body]}] (-> body
                                     (json/parse-string true)
                                     :Message
                                     (json/parse-string true))))))

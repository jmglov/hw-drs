(ns hw-drs.config)

(def max-events 100)

(def sqs-report-hour-amount "https://sqs.eu-west-1.amazonaws.com/166399666252/hw-drs-report-hour-amount")
(def sqs-report-hour-amount-pm "https://sqs.eu-west-1.amazonaws.com/166399666252/hw-drs-report-hour-amount-pm")
(def sqs-report-amount-pm "https://sqs.eu-west-1.amazonaws.com/166399666252/hw-drs-report-amount-pm")
(def sqs-report-day-merchant "https://sqs.eu-west-1.amazonaws.com/166399666252/hw-drs-report-day-merchant")
(def sqs-report-merchant-pm "https://sqs.eu-west-1.amazonaws.com/166399666252/hw-drs-report-merchant-pm")

(def sqs-aggregates "https://sqs.eu-west-1.amazonaws.com/166399666252/hw-drs-aggregates")

(def sns-purchase-events "arn:aws:sns:eu-west-1:166399666252:hw-drs-purchase-events")
(def sns-aggregates "arn:aws:sns:eu-west-1:166399666252:hw-drs-aggregates")

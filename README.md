# Hello World Distributed Reporting System

Demo distributed worker system for Hello World leaders.

## What it does

You are a developer at a company that provides an checkout system for many online stores. In order to make smart business decisions, the analysts at your company need detailed reports on purchases.

When a customer completes a purchase, an event is generated by the checkout system containing the following information:
* Date: ISO 8601 instant; e.g. 2011-12-03T10:15:30Z
* Amount: total amount of purchase in cents; e.g. 4285 (€42.85)
* Payment method: Pay Now (credit card or bank transfer), Pay Later (invoice at the end of the month), or Slice It (pay in convenient monthly installments)
* Merchant ID: unique identifier of merchant; e.g. 1bb53ed1-787b-4543-9def-ea18eef7902e

Your business people want the following reports:
* Number of purchases per hour, broken down by amount bracket (less than €10, €10 - €50, €50-€100, €100-€500, more than €500)
* Number of purchases per hour, broken down by amount bracket and payment method
* Number of purchases, broken down by amount bracket and payment method
* Number of purchases per day, broken down by merchant
* Number of purchases, broken down by merchant and payment method

## How it works

When a purchase is completed, the checkout system publishes an event to an SNS topic called `hw-drw-purchase-events`. An event is a JSON object:

```json
{
  "date": "2011-12-03T10:15:30Z",
  "amount": 4285,
  "paymentMethod": "SLICE_IT",
  "merchantId": "1bb53ed1-787b-4543-9def-ea18eef7902e"
}
```

Subscribed to this topic are SQS queues for each report that need to be generated:
* `hw-drw-report-hour-amount`
* `hw-drw-report-hour-amount-pm`
* `hw-drw-report-amount-pm`
* `hw-drw-report-day-merchant`
* `hw-drw-report-merchant-pm`

Each of these queues is serviced by a Lambda function that runs once per minute, reads as many messages as are on the queue (up to 100, as the Lambda should finish quickly), and generates a list of aggregates. For example, given the event above, the `hw-drw-report-hour-amount-pm` function would generate an aggregate datapoint `2011-12-03:10|10-50|SLICE_IT`. All datapoints are then combined into a JSON object:

```json
{
  "aggregates": [
    {
      "datapoint": "2011-12-03:10|10-50|SLICE_IT",
      "events": 3
    },
    {
      "datapoint": "2011-12-03:10|500|SLICE_IT",
      "events": 1
    },
    {
      "datapoint": "2011-12-03:10|10|PAY_LATER",
      "events": 16
    }
  ]
}
```

The function then publishes the aggregates to an SNS topic called `hw-drw-aggregates`, to which an SQS queue called `hw-drw-aggregates` is subscribed. The SNS message also triggers a Lambda function called `hw-drw-writer`, which reads each datapoint from a DynamoDB table called `hw-drw-reporting`, increments it by the number of events in the datapoint, then writes it back. The fact that there is only one thing writing to the database at a time means that we will never have a race condition which could lead to inconsistencies in our data.

## Setup

### Install prerequisites

* [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Nightcode](https://github.com/oakes/Nightcode/releases/download/2.5.10/Nightcode-2.5.10.jar)
* [Leiningen](https://leiningen.org/#install)

### Configure environment

#### MacOS / Linux

```bash
export AWS_ACCESS_KEY_ID=AKIAIMBQP7CODRROQGIA
export AWS_SECRET_ACCESS_KEY=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
export AWS_REGION=eu-west-1
```

#### Windows

```
set AWS_ACCESS_KEY_ID=AKIAIMBQP7CODRROQGIA
set AWS_SECRET_ACCESS_KEY=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
set AWS_REGION=eu-west-1
```

### Start Nightcode

#### MacOS / Linux

```bash
java -jar /path/to/Nightcode-2.5.10.jar &
```

#### Windows

```
java -jar \path\to\Nightcode-2.5.10.jar
```
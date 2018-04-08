(defproject hw-drs "0.1.0-SNAPSHOT"
  :description "Hello World distributed reporting system"
  :url "https://github.com/jmglov/hw-drs"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [amazonica "0.3.114" :exclusions [com.amazonaws/aws-java-sdk
                                                   com.amazonaws/amazon-kinesis-client
                                                   com.amazonaws/dynamodb-streams-kinesis-adapter]]
                 [com.amazonaws/aws-java-sdk-core "1.11.274" :exclusions [com.fasterxml.jackson.dataformat/jackson-dataformat-cbor]]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.274"]
                 [cheshire "5.8.0"]
                 [environ "1.1.0"]

                 [uswitch/lambada "0.1.2"]]
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[aero "1.1.2"]
                                  [org.clojure/test.check "0.10.0-alpha2"]
                                  [pjstadig/humane-test-output "0.8.1"]]
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]
                   :source-paths ["dev"]}}
  :plugins [[lein-exec "0.3.7"]
            [lein-cljfmt "0.5.7"]]
  :uberjar-name "sls.jar"
  :aliases {"format" ["cljfmt" "fix"]
            "deploy" ["exec", "-ep", "(use 'deploy) (deploy!)"]}

  ;; Run only integration tests with "lein test :integration", only unit tests
  ;; with "lein test", and both with "lein test :all".
  ;; See http://stackoverflow.com/a/23017734/58994
  :test-selectors {:default (fn [metadata]
                              (not-any? #(metadata %) [:integration]))
                   :integration :integration
                   :all (constantly true)})

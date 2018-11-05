(ns web3.test-runner
  (:require [print.foo :include-macros true]
            [cljs.test :refer-macros [run-tests]]
            [web3.tests]))

(defn run-all-tests []
  (.clear js/console)
  (run-tests 'web3.tests))

#_ (run-all-tests)

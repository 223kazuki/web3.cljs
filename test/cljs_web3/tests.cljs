(ns cljs-web3.tests
  (:require [cljs.core.async :refer [<! >! chan]]
            [cljs-web3.core :as web3]
            [cljs-web3.utils :as web3-utils]
            [cljs-web3.db :as web3-db]
            [cljs-web3.eth :as web3-eth]
            [cljs-web3.net :as web3-net]
            [cljs-web3.personal :as web3-personal]
            [cljs-web3.shh :as web3-shh]
            [cljs-web3.async.eth :as web3-eth-async]
            [cljs.test :refer-macros [deftest is testing run-tests use-fixtures async]]
            [print.foo :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def w3 (web3/create-web3 "ws://localhost:8549/"))
(def gas-limit 4500000)

(def contract-source "
  pragma solidity ^0.4.6;

  contract test {
    function multiply(uint a) returns(uint d) {
      return a * 7;
    }
  }")

(deftest web3-test
  (is (string? (web3/version w3)))
  (is (web3/current-provider w3))

  #_ (is (web3-personal/unlock-account w3 (web3-eth/default-account w3) "m" 999999))

  #_ (let [create-contract-ch (chan)]
       (async done
              (let [compiled (web3-eth/compile-solidity w3 contract-source)]
                (is (map? compiled))
                (is (number? (web3-eth/estimate-gas w3 compiled)))
                (web3-eth/contract-new
                 w3
                 (:abi-definition (:info compiled))
                 {:data (:code compiled)
                  :gas gas-limit
                  :from (first (web3-eth/accounts w3))}
                 #(go (>! create-contract-ch [%1 %2]))))

              (go
                (let [[err Contract] (<! create-contract-ch)]
                  (is (not err))
                  (is Contract)
                  (is (not (:address Contract)))
                  (is (map? (web3-eth/get-transaction w3 (aget Contract "transactionHash")))))

                (let [[err Contract] (<! create-contract-ch)]
                  (is (not err))
                  (is (aget Contract "address"))
                  (is (string? (web3-eth/contract-call Contract :multiply 5)))))
              (done))))

(deftest web3-utils-test
  (is (web3-utils/hex? (web3-utils/random-hex 32)))
  (is (web3-utils/bn? (web3-utils/bn "1000000000000000000")))
  (is (= (.toString (web3-utils/bn 1234)) "1234"))
  (is (web3-utils/bn? (web3-utils/bn 1234)))
  (is (= (web3-utils/sha3 "1")
         "0xc89efdaa54c0f20c7adf612882df0950f5a951637e0307cdcb4c672f298b8bc6"))
  (is (= (web3-utils/solidity-sha3 "234564535" "0xfff23243" true -10)
         "0x3e27a893dc40ef8a7f0841d96639de2f58a132be5ae466d40087a2cfa83b7179"))
  (is (web3-utils/hex? "0xc1912"))
  (is (web3-utils/hex-strict? "0x41"))
  (is (not (web3-utils/hex-strict? "41")))
  (is (web3-utils/address? "0x6fce64667819c82a8bcbb78e294d7b444d2e1a29"))
  (is (not (web3-utils/address? "0x6fce64667819c82a8bcbb78e294d7b444d2e1a294")))
  (is (= (web3-utils/to-checksum-address "0XC1912FEE45D61C87CC5EA59DAE31190FFFFF232D")
         "0xc1912fEE45d61C87Cc5EA59DaE31190FFFFf232d"))
  (is (web3-utils/check-address-checksum "0xc1912fEE45d61C87Cc5EA59DaE31190FFFFf232d"))
  (is (= (web3-utils/to-hex js/Web3 "A") "0x41"))
  (is (.eq (web3-utils/to-bn 1) (web3-utils/bn 1)))
  (is (= (web3-utils/hex-to-number-string "0xFF") "255"))
  (is (= (web3-utils/hex-to-number "0xFF") 255))
  (is (= (web3-utils/number-to-hex 255) "0xff"))
  (is (= (web3-utils/hex-to-utf8 "0x49206861766520313030e282ac") "I have 100€"))
  (is (= (web3-utils/hex-to-ascii "0x41") "A"))
  (is (= (web3-utils/utf8-to-hex "I have 100€") "0x49206861766520313030e282ac"))
  (is (= (web3-utils/ascii-to-hex "A") "0x41"))
  (is (= (web3-utils/hex-to-bytes "0x000000ea") [0 0 0 234]))
  (is (= (web3-utils/bytes-to-hex [72 101 108 108 111 33 36]) "0x48656c6c6f2124"))
  (is (= (web3-utils/to-wei "1" :ether) "1000000000000000000"))
  (is (.eq (web3-utils/to-wei (web3-utils/bn 1) :ether) (web3-utils/bn "1000000000000000000")))
  (is (= (web3-utils/from-wei "1000000000000000000" :ether) "1"))
  (is (= (web3-utils/from-wei (web3-utils/bn "1000000000000000000") :ether) "1"))
  (is (web3-utils/unit-map))
  (is (= (web3-utils/pad-left "1" 5 "A") "AAAA1"))
  (is (= (web3-utils/pad-right "1" 5 "A") "1AAAA"))
  (is (= (web3-utils/to-twos-complement "-1")
         "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")))

(deftest web3-eth-test
  (async done
    (go
      (let [[err accounts] (<! (web3-eth-async/get-accounts w3))]
        (is (seq accounts))
        (web3-eth/set-default-account! w3 (first accounts))
        (is (= (web3-eth/default-account w3) (first accounts))))
      (is (web3-eth/default-block w3))
      (let [[err syncing?] (<! (web3-eth-async/syncing? w3))]
        (is (not syncing?))) ;; TODO: Check syncing.
      (let [[err coinbase] (<! (web3-eth-async/get-coinbase w3))]
        (is coinbase))
      (let [[err hashrate] (<! (web3-eth-async/get-hashrate w3))]
        (is (number? hashrate)))

      #_ (is (web3-net/listening? w3))
      #_ (is (number? (web3-net/peer-count w3)))

      (let [[err gas-price] (<! (web3-eth-async/get-gas-price w3))]
        (is (number? (js/parseInt gas-price))))
      (let [[err coinbase] (<! (web3-eth-async/get-coinbase w3))
            [err balance] (<! (web3-eth-async/get-balance w3 coinbase))]
        (is (number? (js/parseInt balance))))

      (let [[err block] (<! (web3-eth-async/get-block w3 "latest"))]
        (is (map? block)))
      #_ (is (seq (web3-eth/get-compilers w3)))

      (done))))

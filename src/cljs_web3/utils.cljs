(ns cljs-web3.utils
  (:require [camel-snake-kebab.core :as cs :include-macros true]
            [camel-snake-kebab.extras :refer [transform-keys]]
            [cljs.core.async :refer [>! chan]]
            [clojure.string :as string])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn safe-case [case-f]
  (fn [x]
    (cond-> (subs (name x) 1)
      true (string/replace "_" "*")
      true case-f
      true (string/replace "*" "_")
      true (->> (str (first (name x))))
      (keyword? x) keyword)))

(def camel-case (safe-case cs/->camelCase))
(def kebab-case (safe-case cs/->kebab-case))

(def js->cljk #(js->clj % :keywordize-keys true))

(def js->cljkk
  "From JavaScript to Clojure with kekab-cased keywords."
  (comp (partial transform-keys kebab-case) js->cljk))

(def cljkk->js
  "From Clojure with kebab-cased keywords to JavaScript."
  (comp clj->js (partial transform-keys camel-case)))

(defn callback-js->clj [x]
  (if (fn? x)
    (fn [err res]
      (when (and res (aget res "v"))
        (aset res "v" (aget res "v"))) ;; Prevent weird bug in advanced optimisations
      (x err (js->cljkk res)))
    x))

(defn args-cljkk->js [args]
  (map (comp cljkk->js callback-js->clj) args))

(defn js-apply
  ([this method-name]
   (js-apply this method-name nil))
  ([this method-name args]
   (let [method-name (camel-case (name method-name))]
     (if (aget this method-name)
       (js->cljkk (apply js-invoke this method-name (args-cljkk->js args)))
       (throw (str "Method: " method-name " was not found in object."))))))

(defn js-prototype-apply [js-obj method-name args]
  (js-apply (aget js-obj "prototype") method-name args))

(defn prop-or-clb-fn
  "Constructor to create an fn to get properties or to get properties and apply a
  callback fn."
  [& ks]
  (fn [web3 & args]
    (if (fn? (first args))
      (js-apply (apply aget web3 (butlast ks))
                (str "get" (cs/->PascalCase (last ks)))
                args)
      (js->cljkk (apply aget web3 ks)))))

(defn create-async-fn [f]
  (fn [& args]
    (let [[ch args] (if (instance? cljs.core.async.impl.channels/ManyToManyChannel (first args))
                      [(first args) (rest args)]
                      [(chan) args])]
      (apply f (concat args [(fn [err res]
                               (go (>! ch [err res])))]))
      ch)))

(defn utils
  "Gets utils object from web3-instance.

  Parameter:
  web3 - web3 instance"
  [web3]
  (aget web3 "utils"))

(defn random-hex
  "Returns a string representing the generated random HEX string.

  Parameters:
  number - The byte size for the HEX string, e.g. 32 will result in a 32
           bytes HEX string with 64 characters preficed with “0x”.
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(random-hex 32)`
  \"0xa5b9d60f32436310afebcfda832817a68921beb782fabf7915cc0460b443116a\"
  user> `(random-hex 4)`
  \"0x6892ffc6\"
  user> `(random-hex 2)`
  \"0x99d6\"
  user> `(random-hex 1)`
  \"0x9a\"
  user> `(random-hex 0)`
  \"0x\""
  ([size] (random-hex js/Web3 size))
  ([Web3 size]
   (js-apply (utils Web3) "randomHex" [size])))


(defn bn
  "Returns the BN.js instance.

  Parameters:
  string|number - A number, number string or HEX string to convert to a BN object.
  Web3          - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(.toString (bn 1234))`
  \"1234\"
  user> `(.toString (.add (bn '1234') (bn '1')))`
  \"1235\"
  user> `(.toString (bn '0xea'))`
  \"234\""
  ([mixed] (bn js/Web3 mixed))
  ([Web3 mixed]
   (let [constructor (aget Web3 "utils" "BN")]
     (new constructor mixed))))


(defn bn?
  "Returns boolean.

  Parameters:
  object - An BN.js instance.
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(big-number? (bn 10))`
  true"
  ([big-number] (bn? js/Web3 big-number))
  ([Web3 big-number]
   (js-invoke (utils Web3) "isBN" big-number)))


(def big-number? bn?)


(defn sha3
  "Returns a string representing the Keccak-256 SHA3 of the given data.

  Parameters:
  String - The string to hash using the Keccak-256 SHA3 algorithm
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(sha3 \"234\")` // taken as string
  \"0xc1912fee45d61c87cc5ea59dae311904cd86b84fee17cc96966216f811ce6a79\"
  user> `(sha3 (bn \"234\"))
  \"0xbc36789e7a1e281436464229828f817d6612f7b477d66591ff96a9e064bcc98a\"
  user> `(sha3 234)
  null // can\"t calculate the has of a number
  user> `(sha3 0xea)` // same as above, just the HEX representation of the number
  null
  user> `(sha3 \"0xea\")` // will be converted to a byte array first, and then hashed
  \"0x2f20677459120677484f7104c76deb6846a2c071f9b3152c103bb12cd54d1a4a\""
  ([string] (sha3 js/Web3 string))
  ([Web3 string]
   (js-apply (utils Web3) "sha3" [string])))


(def keccak256 sha3)


(defn solidity-sha3
  "Returns a string representing the SHA3 of the given data in the same way solidity would.
  This means arguments will be ABI converted and tightly packed before being hashed.

  Parameters:
  Mixed - Any type, or an object with {type: 'uint', value: '123456'} or
          {t: 'bytes', v: '0xfff456'}. Basic types are autodetected as follows:
           * String non numerical UTF-8 string is interpreted as string.
           * String|Number|BN|HEX positive number is interpreted as uint256.
           * String|Number|BN negative number is interpreted as int256.
           * Boolean as bool.
           * String HEX string with leading 0x is interpreted as bytes.
           * HEX HEX number representation is interpreted as uint256.
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(solidity-sha3 \"234564535\" \"0xfff23243\" true -10)`
  ;; auto detects:      uint256     bytes        bool int256
  \"0x3e27a893dc40ef8a7f0841d96639de2f58a132be5ae466d40087a2cfa83b7179\"
  user> `(solidity-sha3 \"Hello!%\")` ;; auto detects: string
  \"0x661136a4267dba9ccdf6bfddb7c00e714de936674c4bdb065a531cf1cb15c7fc\"
  user> `(solidity-sha3 \"234\")` ;; auto detects: uint256
  \"0x61c831beab28d67d1bb40b5ae1a11e2757fa842f031a2d0bc94a7867bc5d26c2\"
  user> `(solidity-sha3 0xea)` ;; same as above
  \"0x61c831beab28d67d1bb40b5ae1a11e2757fa842f031a2d0bc94a7867bc5d26c2\"
  user> `(solidity-sha3 (bn \"234\"))` ;; same as above
  \"0x61c831beab28d67d1bb40b5ae1a11e2757fa842f031a2d0bc94a7867bc5d26c2\"
  user> `(solidity-sha3 {:type \"uint256\" :value \"234\"}))` ;; same as above
  \"0x61c831beab28d67d1bb40b5ae1a11e2757fa842f031a2d0bc94a7867bc5d26c2\"
  user> `(solidity-sha3 {:t \"uint\" :v (bn \"234\")}))` ;; same as above
  \"0x61c831beab28d67d1bb40b5ae1a11e2757fa842f031a2d0bc94a7867bc5d26c2\"
  user> `(solidity-sha3 \"0x407D73d8a49eeb85D32Cf465507dd71d507100c1\")`
  \"0x4e8ebbefa452077428f93c9520d3edd60594ff452a29ac7d2ccc11d47f3ab95b\"
  user> `(solidity-sha3 {:t \"bytes\" :v \"0x407D73d8a49eeb85D32Cf465507dd71d507100c1\"})`
  \"0x4e8ebbefa452077428f93c9520d3edd60594ff452a29ac7d2ccc11d47f3ab95b\" ;; same result as above
  user> `(solidity-sha3 {:t \"address\" :v \"0x407D73d8a49eeb85D32Cf465507dd71d507100c1\"})`
  \"0x4e8ebbefa452077428f93c9520d3edd60594ff452a29ac7d2ccc11d47f3ab95b\"
  ;; same as above but will do a checksum check if its multi case
  user> `(solidity-sha3 {:t \"bytes32\" :v \"0x407D73d8a49eeb85D32Cf465507dd71d507100c1\"})`
  \"0x3c69a194aaf415ba5d6afca734660d0a3d45acdc05d54cd1ca89a8988e7625b4\"
  ;; different result as above
  user> `(solidity-sha3 {:t \"string\" :v \"Hello!%\"} {:t \"int8\" :v -23}
                        {:t \"address\" :v \"0x85F43D8a49eeB85d32Cf465507DD71d507100C1d\"})`
  \"0xa13b31627c1ed7aaded5aecec71baf02fe123797fffd45e662eac8e06fbe4955\""
  [& args]
  (if (instance? js/Web3 (first args))
    (js-apply (utils (first args)) "soliditySha3" (rest args))
    (js-apply (utils js/Web3) "soliditySha3" args)))


(defn hex?
  "Returns a boolean representing if a given string is a HEX string.

  Parameters:
  string|hex - The given HEX string.
  Web3       - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex? \"0xc1912\")`
  true
  user> `(hex? 0xc1912)`
  true
  user> `(hex? \"c1912\")`
  true
  user> `(hex? 345)`
  true // this is tricky, as 345 can be a a HEX representation or a number,
          be careful when not having a 0x in front!
  user> `(hex? \"0xZ1912\")`
  false
  user> `(hex? \"Hello\")`
  false"
  ([hex] (hex? js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "isHex" [hex])))


(defn hex-strict?
  "Returns a boolean representing if a given string is a HEX string.
  Difference to hex? is that it expects HEX to be prefixed with 0x.

  Parameters:
  string|hex - The given HEX string.
  Web3       - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex-strict? \"0xc1912\")`
  true
  user> `(hex-strict? 0xc1912)`
  false
  user> `(hex-strict? \"c1912\")`
  false
  user> `(hex-strict? 345)`
  false // this is tricky, as 345 can be a a HEX representation or a number,
        // be careful when not having a 0x in front!
  user> `(hex-strict? \"0xZ1912\")`
  false
  user> `(hex? \"Hello\")`
  false"
  ([hex] (hex-strict? js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "isHexStrict" [hex])))


(defn address?
  "Returns a boolean indicating if the given string is an address.

  Parameters:
  address - An HEX string.
  Web3    - (Optional first argument) Web3 JavaScript object

  Returns false if it's not on a valid address format. Returns true if it's an
  all lowercase or all uppercase valid address. If it's a mixed case address, it
  checks using web3's isChecksumAddress().

  Example:
  user> `(address? \"0x8888f1f195afa192cfee860698584c030f4c9db1\")`
  true

  ;; With first f capitalized
  user> `(web3/address? \"0x8888F1f195afa192cfee860698584c030f4c9db1\")`
  false"
  ([address] (address? js/Web3 address))
  ([Web3 address]
   (js-apply (utils Web3) "isAddress" [address])))


(defn to-checksum-address
  "Returns the checksum address.

  Parameters:
  address - An HEX address string.
  Web3    - (Optional first argument) Web3 JavaScript object

  Will convert an upper or lowercase Ethereum address to a checksum address.

  Example:
  user> `(to-checksum-address \"0xc1912fee45d61c87cc5ea59dae31190fffff2323\")`
  \"0xc1912fEE45d61C87Cc5EA59DaE31190FFFFf232d\"
  user> `(to-checksum-address \"0XC1912FEE45D61C87CC5EA59DAE31190FFFFF232D\")`
  \"0xc1912fEE45d61C87Cc5EA59DaE31190FFFFf232d\" ;; same as above"
  ([address] (to-checksum-address js/Web3 address))
  ([Web3 address]
   (js-apply (utils Web3) "toChecksumAddress" [address])))


(defn check-address-checksum
  "Returns true if the checksum of the address is valid, false if its not a
  checksum address, or the checksum is invalid.

  Parameters:
  address - An HEX address string.
  Web3    - (Optional first argument) Web3 JavaScript object

  Example:
  user> `(check-address-checksum \"0xc1912fEE45d61C87Cc5EA59DaE31190FFFFf232d\")`
  true"
  ([address] (to-checksum-address js/Web3 address))
  ([Web3 address]
   (js-apply (utils Web3) "toChecksumAddress" [address])))


(defn to-hex
  "Returns hexadecimal string representation of any value
  string|number|map|set|BigNumber.

  Parameters:
  Any  - The value to parse
  Web3 - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(to-hex \"foo\")`
  \"0x666f6f\" "
  ([any] (to-hex js/Web3 any))
  ([Web3 any]
   (js-apply (utils Web3) "toHex" [any])))


(defn to-bn
  "Returns the BN.js instance.

  Parameters:
  string|number|hex  - A number to convert to a big number.
  Web3               - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(.toString (to-bn 1234))`
  \"1234\""
  ([number] (to-bn js/Web3 number))
  ([Web3 number]
   (js-invoke (utils Web3) "toBN" number)))


(defn hex-to-number-string
  "Returns the number representation of a given HEX value as a string.

  Parameters:
  string|hex  - A hex string to hash.
  Web3        - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex-to-number \"0xea\")`
  \"234\""
  ([hex] (hex-to-number-string js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "hexToNumberString" [hex])))


(defn hex-to-number
  "Returns the number representation of a given HEX value.

  Parameters:
  string|hex  - A hex string to hash.
  Web3        - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex-to-number \"0xea\")`
  234"
  ([hex] (hex-to-number js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "hexToNumber" [hex])))


(defn number-to-hex
  "Returns the HEX representation of a given number value.

  Parameters:
  string|number|bigNumber - A number as string or number.
  Web3                    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(number-to-hex \"234\")`
  \"0xea\""
  ([number] (number-to-hex js/Web3 number))
  ([Web3 number]
   (js-apply (utils Web3) "numberToHex" [number])))


(defn hex-to-utf8
  "Returns the UTF-8 string representation of a given HEX value.

  Parameters:
  string - A HEX string to convert to a UTF-8 string.
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex-to-utf8 \"0x49206861766520313030e282ac\")`
  \"I have 100€\""
  ([hex] (hex-to-utf8 js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "hexToUtf8" [hex])))


(def hex-to-string hex-to-utf8)


(defn hex-to-ascii
  "Returns the UTF-8 string representation of a given HEX value.

  Parameters:
  string  - A HEX string to convert to a UTF-8 string.
  Web3    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex-to-utf8 \"0x49206861766520313030e282ac\")`
  \"I have 100€\""
  ([hex] (hex-to-ascii js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "hexToAscii" [hex])))


(defn utf8-to-hex
  "Returns the HEX representation of a given UTF-8 string.

  Parameters:
  string  - A HEX string.
  Web3    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(ascii-to-hex \"I have 100€\")`
  \"0x49206861766520313030e282ac\""
  ([string] (utf8-to-hex js/Web3 string))
  ([Web3 string]
   (js-apply (utils Web3) "utf8ToHex" [string])))


(def string-to-hex utf8-to-hex)


(defn ascii-to-hex
  "Returns the HEX representation of a given ASCII string.

  Parameters:
  string  - A ASCII string to convert to a HEX string.
  Web3    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(ascii-to-hex \"I have 100!\")`
  \"0x4920686176652031303021\""
  ([string] (ascii-to-hex js/Web3 string))
  ([Web3 string]
   (js-apply (utils Web3) "asciiToHex" [string])))


(defn hex-to-bytes
  "Returns a byte array from the given HEX string.

  Parameters:
  string  - A HEX string to convert to a UTF-8 string.
  Web3    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(hex-to-bytes \"0x000000ea\")`
  [0 0 0 234]"
  ([hex] (hex-to-bytes js/Web3 hex))
  ([Web3 hex]
   (js-apply (utils Web3) "hexToBytes" [hex])))


(defn bytes-to-hex
  "Returns a HEX string from a byte array.

  Parameters:
  array - A byte array to convert.
  Web3  - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(bytes-to-hex [72 101 108 108 111 33 36])`
  \"0x48656c6c6f2124\""
  ([byte-array] (bytes-to-hex js/Web3 byte-array))
  ([Web3 byte-array]
   (js-apply (utils Web3) "bytesToHex" [byte-array])))


(defn to-wei
  "Converts an Ethereum unit into Wei.

  Parameters:
  string|BN -  The value.A number or BigNumber instance.
  unit   - One of :noether :wei :kwei :Kwei :babbage :femtoether :mwei :Mwei
           :lovelace :picoether :gwei :Gwei :shannon :nanoether :nano :szabo
           :microether :micro :finney :milliether :milli :ether :kether :grand
           :mether :gether :tether
  Web3   - (optional first argument) Web3 JavaScript object.

  Returns either a number string, or a BigNumber instance, depending on the
  given number parameter.

  Example:
  user> `(web3/to-wei 10 :ether)`
  \"10000000000000000000\""
  ([number unit] (to-wei js/Web3 number unit))
  ([Web3 number unit]
   (js-apply (utils Web3) "toWei" [number (name unit)])))


(defn from-wei
  "Converts a number of Wei into an Ethereum unit.

  Parameters:
  string|BN - A number as a string or BigNumber instance.
  unit   - One of :noether :wei :kwei :Kwei :babbage :femtoether :mwei :Mwei
           :lovelace :picoether :gwei :Gwei :shannon :nanoether :nano :szabo
           :microether :micro :finney :milliether :milli :ether :kether :grand
           :mether :gether :tether
  Web3   - (optional first argument) Web3 JavaScript object.

  Returns either a number string, or a BigNumber instance, depending on the
  given number parameter.

  Example:
  user> `(web3/from-wei 10 :ether)`
  \"0.00000000000000001\""
  ([number unit] (from-wei js/Web3 number unit))
  ([Web3 number unit]
   (js-apply (utils Web3) "fromWei" [number (name unit)])))


(defn unit-map
  "Shows all possible ether value and their amount in wei."
  ([] (unit-map js/Web3))
  ([Web3]
   (js->cljkk (aget (utils Web3) "unitMap"))))


(defn pad-left
  "Returns input string with zeroes or sign padded to the left.

  Parameters:
  string - String to be padded
  chars  - Amount of chars to address
  sign   - (optional) Char to pad with (behaviour with multiple chars is
                      undefined)
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(web3/pad-left \"foo\" 8)`
  \"00000foo\"
  user> `(web3/pad-left \"foo\" 8 \"b\")`
  \"bbbbbfoo\" "
  ([string chars] (pad-left string chars nil))
  ([string chars sign] (pad-left js/Web3 string chars sign))
  ([Web3 string chars sign]
   (js-apply (utils Web3) "padLeft" [string chars sign])))


(def left-pad pad-left)


(defn pad-right
  "Returns input string with zeroes or sign padded to the right.

  Parameters:
  string - String to be padded
  chars  - Amount of total chars
  sign   - (optional) Char to pad with (behaviour with multiple chars is
                      undefined)
  Web3   - (optional first argument) Web3 instance

  Example:
  user> `(web3/pad-right \"foo\" 8)`
  \"foo00000\"
  user> `(web3/pad-right \"foo\" 8 \"b\")`
  \"foobbbbb\" "
  ([string chars] (pad-right string chars nil))
  ([string chars sign] (pad-right js/Web3 string chars sign))
  ([Web3 string chars sign]
   (js-apply (utils Web3) "padRight" [string chars sign])))


(def right-pad pad-right)

(defn to-twos-complement
  "Converts a negative numer into a two’s complement.

  Parameters:
  number|string|bigNumber - The number to convert.
  Web3                    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(to-twos-complement \"-1\")`
  \"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\"
  user> `(to-twos-complement -1)`
  \"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\"
  user> `(to-twos-complement \"0x1\")`
  \"0x0000000000000000000000000000000000000000000000000000000000000001\"
  user> `(to-twos-complement -15)`
  \"0xfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1\"
  user> `(to-twos-complement \"-0x1\")`
  \"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff\""
  ([number] (to-twos-complement js/Web3 number))
  ([Web3 number]
   (js-apply (utils Web3) "toTwosComplement" [number])))

(ns cljs-web3.core
  "ClojureScript wrapper around Web3 JavaScript API methods on the Web3 object.

  A `web3-instance` can be obtained in two ways:

  1. Via the user's browser (when using Mist or MetaMask)

  `(defn web3-instance []
     (new (aget js/window \"Web3\")
          (current-provider (aget js/window \"web3\"))))`

  2. Created via `create-web3` (when running a local Ethereum node)

  `(def web3-instance
     (create-web3 \"http://localhost:8545/\"))`

  The Web3 JavaScript object is provided on the browser window."
  (:require [cljs-web3.utils :as u :refer [js-apply js-prototype-apply]]
            [goog.object]
            [web3-1.0]))

(def version-api
  "Returns a string representing the Ethereum js api version.

  Parameters:
  Web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Example:
  user> `(web3/version-node web3-instance
           (fn [err res] (when-not err (println res))))`
  nil
  user> 0.2.0"
  (u/prop-or-clb-fn "version"))


(def version-node
  "Returns a string representing the client/node version.

  Parameters:
  Web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Example:
  user> `(version-node web3-instance
           (fn [err res] (when-not err (println res))))`
  nil
  user> MetaMask/v3.10.8"
  (u/prop-or-clb-fn "version" "node"))


(def version-network
  "Returns a string representing the network protocol version.

  \"1\"  is Main Net or Local Net
  \"2\"  is (Deprecated) Morden Network
  \"3\"  is Ropsten Test Net
  \"4\"  is Rinkeby Test Net
  \"42\" is Kovan Test Net

  Parameters:
  Web3        - Web3 instance
  callback-fn - callback with two parameters, error and result

  Example:
  user> `(version-network web3-instance
           (fn [err res] (when-not err (println res))))`
  nil
  user> 3"
  (u/prop-or-clb-fn "version" "network"))

(def version-ethereum
  "Returns a hexadecimal string representing the Ethereum protocol version.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Example:
  user> `(version-ethereum web3-instance
           (fn [err res] (when-not err (println res))))`
  nil
  user> 0x3f"
  (u/prop-or-clb-fn "version" "ethereum"))


(def version-whisper
  "Returns a string representing the Whisper protocol version.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Example:
  user> `(version-whisper
           web3-instance
           (fn [err res] (when-not err (println res))))`
  nil
  user> 20"
  (u/prop-or-clb-fn "version" "whisper"))


(defn random-hex
  "Returns a string representing the generated random HEX string.

  Parameters:
  number - The byte size for the HEX string, e.g. 32 will result in a 32
           bytes HEX string with 64 characters preficed with “0x”.

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
   (js-apply (aget Web3 "utils") "randomHex" [size])))


(defn bn
  "Returns the BN.js instance.

  Parameters:
  string|number - A number, number string or HEX string to convert to a BN object.

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


(defn big-number?
  "Returns boolean.

  Parameters:
  object - An BN.js instance.

  Example:
  user> `(big-number? (bn 10))`
  true"
  ([big-number] (big-number? js/Web3 big-number))
  ([Web3 big-number]
   (js-invoke (aget Web3 "utils") "isBN" big-number)))


(defn hex?
  "Returns boolean.

  Parameters:
  string|HEX: The given HEX string.

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
   (js-apply (aget Web3 "utils") "isHex" [hex])))


(defn is-hex-strict [])


(defn sha3
  "Returns a string representing the Keccak-256 SHA3 of the given data.

  Parameters:
  String - The string to hash using the Keccak-256 SHA3 algorithm
  Map    - (optional) Set encoding to hex if the string to hash is encoded
                      in hex. A leading 0x will be automatically ignored.
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user> (def hash \"Some string to be hashed\")
  #'user/hash
  user> `(sha3 hash)
  \"0xed973b234cf2238052c9ac87072c71bcf33abc1bbd721018e0cca448ef79b379\"`
  user> `(sha3 hash {:encoding :hex})`
  \"0xbd83a94d23235dd7dfcf67a5a0d9e9643a715cd5b528083a2cf944d61f8e7b51\"

  NOTE: This differs from the documented result of the Web3 JavaScript API,
  which equals
  \"0x85dd39c91a64167ba20732b228251e67caed1462d4bcf036af88dc6856d0fdcc\""
  ([string] (sha3 string nil))
  ([string options] (sha3 js/Web3 string options))
  ([Web3 string options]
   (js-apply (aget Web3 "utils") "sha3" [string options])))


(defn keccak256 [])
(defn solidity-sha3 [])


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
   (js-apply (aget Web3 "utils") "isAddress" [address])))


(defn check-address-checksum [])
(defn to-checksum-address [])


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
   (js-apply (aget Web3 "utils") "toHex" [any])))


(defn to-big-number
  "Converts a given number into a BigNumber instance.

  Parameters:
  number-or-hex-string - A number, number string or HEX string of a number.
  Web3                 - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(to-big-number \"10000000000000000000\")`
  <An instance of BigNumber>"
  ([number-or-hex-string] (to-big-number js/Web3 number-or-hex-string))
  ([Web3 number-or-hex-string]
   (bn number-or-hex-string)))


(defn bytes-to-hex [])
(defn hex-to-bytes [])
(defn hex-to-number-string [])
(defn hex-to-number [])


(defn to-decimal
  "Returns the number representing a HEX string in its number representation.

  Parameters:
  hex-string - An HEX string to be converted to a number.
  Web3       - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(to-decimal \"0x15\")`
  21"
  ([hex-string] (to-decimal js/Web3 hex-string))
  ([Web3 hex-string]
   (js-apply (aget Web3 "utils") "toDecimal" [hex-string])))


(defn number-to-hex [])

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
   (js-apply (aget Web3 "utils") "fromWei" [number (name unit)])))


(defn hex-to-utf8 [])
(defn hex-to-string [])
(defn to-utf8 [])
(defn utf8-to-hex [])
(defn string-to-hex [])
(defn from-utf8 [])
(defn hex-to-ascii [])


(defn to-ascii
  "Converts a HEX string into a ASCII string.

  Parameters:
  hex-string - A HEX string to be converted to ASCII.
  Web3       - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(to-ascii \"0x666f6f\")`
  \"foo\" "
  ([hex-string] (to-ascii js/Web3 hex-string))
  ([Web3 hex-string]
   (js-apply (aget Web3 "utils") "toAscii" [hex-string])))


(defn ascii-to-hex [])


(defn from-ascii
  "Converts any ASCII string to a HEX string.

  Parameters:
  string  - An ASCII string to be converted to HEX.
  padding - (optional) The number of bytes the returned HEX string should have.
  Web3    - (optional first argument) Web3 JavaScript object.

  Example:
  user> `(from-ascii \"ethereum\")`
  \"0x657468657265756d\"
  user> `(from-ascii \"ethereum\")`
  \"0x657468657265756d000000000000000000000000000000000000000000000000\"

  NOTE: The latter is intended behaviour. Because of a bug in Web3 the padding
        is not added. See https://github.com/ethereum/web3.js/issues/337"
  ([string] (from-ascii string nil))
  ([string padding] (from-ascii js/Web3 string padding))
  ([Web3 string padding]
   (js-apply (aget Web3 "utils") "fromAscii" [string padding])))


(defn unit-map [])


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
   (js-apply (aget Web3 "utils") "toWei" [number (name unit)])))


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
   (js-apply (aget Web3 "utils") "padLeft" [string chars sign])))


(defn left-pad [])


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
   (js-apply (aget Web3 "utils") "padRight" [string chars sign])))


(defn right-pad [])
(defn to-twos-complement [])


(defn from-decimal
  "Converts a number or number string to its HEX representation.

  Parameters:
  number - A number to be converted to a HEX string.
  Web3   - (optional first argument) Web3 JavaScript object.

  Example:
  user-> `(web3/from-decimal 21)`
  \"0x15\""
  ([number] (from-decimal js/Web3 number))
  ([Web3 number]
   (js-apply (aget Web3 "utils") "fromDecimal" [number])))


(defn reset
  "Should be called to reset the state of web3. Resets everything except the manager.
  Uninstalls all filters. Stops polling.

  Parameters:
  web3             - An instance of web3
  keep-is-syncing? - If true it will uninstall all filters, but will keep the
                     web3.eth.isSyncing() polls

  Returns nil.

  Example:
  user> `(reset web3-instance true)`
  nil"
  ([web3]
   (reset web3 false))
  ([web3 keep-is-syncing?]
   (js-apply web3 "reset" [keep-is-syncing?])))

(defn set-provider
  "Should be called to set provider.

  Parameters:
  web3     - Web3 instance
  provider - the provider

  Available providers in web3-cljs:
  - `http-provider`
  - `ipc-provider`

  Example:
  user> `(set-provider web3-instance
                       (http-provider web3-instance \"http://localhost:8545\"))`
  nil"
  [web3 provider]
  (js-apply web3 "setProvider" [provider]))

(defn current-provider
  "Will contain the current provider, if one is set. This can be used to check
  if Mist etc. already set a provider.

  Parameters:
  web3 - web3 instance

  Returns the provider set or nil."
  [web3]
  (aget web3 "currentProvider"))

(defn web3
  "Return the web3 instance injected via Mist or Metamask"
  []
  (let [ethereum-instance (goog.object/getValueByKeys js/window "ethereum")
        web3-instance (goog.object/getValueByKeys js/window "web3")]
    (cond
      (some? ethereum-instance)
      (js/Web3. ethereum-instance)

      (some? web3-instance)
      (js/Web3. (current-provider web3-instance)))))

;;; Providers

(defn http-provider [Web3 uri]
  (let [constructor (aget Web3 "providers" "HttpProvider")]
    (constructor. uri)))

(defn ipc-provider [Web3 uri]
  (let [constructor (aget Web3 "providers" "IpcProvider")]
    (constructor. uri)))

(defn create-web3
  "Creates a web3 instance using an `http-provider`.

  Parameters:
  url  - The URL string for which to create the provider.
  Web3 - (Optional first argument) Web3 JavaScript object

  Example:
  user> `(create-web3 \"http://localhost:8545/\")`
  <web3 instance>"
  ([url] (create-web3 js/Web3 url))
  ([Web3 url]
   (new Web3 (http-provider Web3 url))))

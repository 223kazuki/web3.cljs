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

(ns web3.core
  "ClojureScript wrapper around Web3 JavaScript API methods on the Web3 object.

  A `web3-instance` can be obtained in two ways:

  1. Via the user's browser (when using Mist or MetaMask)

  `(defn web3-instance []
     (new (aget js/window \"Web3\")
          (current-provider (aget js/window \"web3\"))))`

  2. Created via `create-web3` (when running a local Ethereum node)

  `(def web3-instance
     (create-web3 \"ws://localhost:8545/\"))`

  The Web3 JavaScript object is provided on the browser window."
  (:require [web3.utils :as u :refer [js-apply js-prototype-apply]]
            [goog.object]
            [web3-cljs]))

(defn version
  "Returns the current version."
  [web3]
  (aget web3 "version"))

(defn modules
  "Return an object with the classes of all major sub modules, to be able to instantiate them manually.

  Eth - Function: the Eth module for interacting with the Ethereum network see web3.eth for more.
  Net - Function: the Net module for interacting with network properties see web3.eth.net for more.
  Personal - Function: the Personal module for interacting with the Ethereum accounts see web3.eth.personal for more.
  Shh - Function: the Shh module for interacting with the whisper protocol see web3.shh for more.
  Bzz - Function: the Bzz module for interacting with the swarm network see web3.bzz for more."
  [web3]
  (aget web3 "modules"))

(defn utils
  "Utility functions are also exposes on the Web3 class object directly."
  [web3]
  (aget web3 "utils"))

(defn set-provider!
  "Should be called to set provider.

  Parameters:
  web3     - Web3 instance
  provider - the provider

  Available providers in web3-cljs:
  - `http-provider`
  - `ipc-provider`
  - `websocket-provider`

  Example:
  user> `(set-provider! web3-instance
                        (websocket-provider web3-instance \"ws://localhost:8545\"))`
  nil"
  [web3 provider]
  (js-apply web3 "setProvider" [provider]))

(defn providers
  "Returns the current available providers."
  [web3]
  (aget web3 "providers"))

(defn given-provider
  "Returns the given provider set or nil"
  [web3]
  (aget web3 "givenProvider"))

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

(defn websocket-provider [Web3 uri]
  (let [constructor (aget Web3 "providers" "WebsocketProvider")]
    (constructor. uri)))

(defn create-web3
  "Creates a web3 instance using an `websocket-provider`.

  Parameters:
  url  - The URL string for which to create the websocket provider.
  Web3 - (Optional first argument) Web3 JavaScript object

  Example:
  user> `(create-web3 \"ws://localhost:8545/\")`
  <web3 instance>"
  ([url] (create-web3 js/Web3 url))
  ([Web3 url]
   (new Web3 (websocket-provider Web3 url))))

(defn batch-request
  "Create and execute batch requests.

  Parameters:
  web3 - web3 instance"
  [Web3]
  (let [constructor (aget Web3 "BatchRequest")]
    (constructor.)))

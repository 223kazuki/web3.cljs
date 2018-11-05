(ns web3.eth
  "Contains the ethereum blockchain related methods."
  (:require [web3.utils :as u :refer [js-apply]]))

(defn eth
  "Gets eth object from web3-instance.

  Parameter:
  web3 - web3 instance"
  [web3]
  (aget web3 "eth"))

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
  (js-apply (eth web3) "setProvider" [provider]))

(defn providers
  "Returns the current available providers."
  [web3]
  (aget (eth web3) "providers"))

(defn given-provider
  "Returns the given provider set or nil"
  [web3]
  (aget (eth web3) "givenProvider"))

(defn current-provider
  "Will contain the current provider, if one is set. This can be used to check
  if Mist etc. already set a provider.

  Parameters:
  web3 - web3 instance

  Returns the provider set or nil."
  [web3]
  (aget (eth web3) "currentProvider"))

(defn batch-request
  "Create and execute batch requests.

  Parameters:
  web3 - web3 instance"
  [Web3]
  (let [constructor (aget Web3 "eth" "BatchRequest")]
    (constructor.)))

#_ (defn extend [])

(defn default-account
  "Gets the default address that is used for the following methods (optionally
  you can overwrite it by specifying the :from key in their options map):

  - `send-transaction!`
  - `call!`
  - `web3.eth.contract/call!`
  - `web3.eth.contract/send!`

  Parameters:
  web3 - web3 instance

  Returns the default address HEX string.

  Example:
  user> `(default-account web3-instance)`
  \"0x85d85715218895ae964a750d9a92f13a8951de3d\""
  [web3]
  (aget web3 "eth" "defaultAccount"))

(defn set-default-account!
  "Sets the default address that is used for the following methods (optionally
  you can overwrite it by specifying the :from key in their options map):

  - `send-transaction!`
  - `call!`
  - `web3.eth.contract/call!`
  - `web3.eth.contract/send!`

  Parameters:
  web3    - web3 instance
  hex-str - Any 20 bytes address you own, or where you have the private key for.

  Returns a 20 bytes HEX string representing the currently set address.

  Example:
  user> (set-default-account! web3-instance
                              \"0x85d85715218895ae964a750d9a92f13a8951de3d\")
  \"0x85d85715218895ae964a750d9a92f13a8951de3d\""
  [web3 hex-str]
  (aset (eth web3) "defaultAccount" hex-str))

(defn default-block
  "This default block is used for the following methods (optionally you can
  override it by passing the default-block parameter):

  - `get-balance`
  - `get-code`
  - `get-transactionCount`
  - `get-storageAt`
  - `call!`
  - `estimate-gas`
  - `web3.eth.contract/call!`

  Parameters:
  web3 - web3 instance

  Returns one of:
  - a block number
  - \"genesis\", the genisis block
  - \"latest\", the latest block (current head of the blockchain)
  - \"pending\", the currently mined block (including pending transactions)

  Example:
  user> `(default-block web3-instance)`
  \"latest\""
  [web3]
  (aget web3 "eth" "defaultBlock"))

(defn set-default-block!
  "Sets default block that is used for the following methods (optionally you can
  override it by passing the default-block parameter):

  - `get-balance`
  - `get-code`
  - `get-transactionCount`
  - `get-storageAt`
  - `call!`
  - `estimate-gas`
  - `web3.eth.contract/call!`

  Parameters:
  web3  - web3 instance
  block - one of:
            - a block number
            - \"genesis\", the genisis block
            - \"latest\", the latest block (current head of the blockchain)
            - \"pending\", the currently mined block (including pending
              transactions)

  Example:
  user> `(set-default-block! web3-instance \"genesis\")`
  \"genesis\""
  [web3 block]
  (aset (eth web3) "defaultBlock" block))

(defn get-protocol-version
  "Get the ethereum protocol version of the node.

  Parameters:
  callback-fn   - callback with two parameters, error and result

  Returns a Promise that returns String: the protocol version.

  Example:
  user> `(get-protocol-version w3 (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `63`"
  [web3 & args]
  (js-apply (eth web3) "getProtocolVersion" args))

(defn syncing?
  "Checks if the node is currently syncing and returns either a syncing object,
  or false.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns a Promise that returns Object|Boolean - A sync object when the node is
  currently syncing or false:
  A sync object includes
  - starting-block: Number - The block number where the sync started.
  - current-block: Number - The block number where at which block the node currently synced to already.
  - highest-block: Number - The estimated block number to sync to.
  - known-states: Number - The estimated states to download
  - pulled-states: Number - The already downloaded states

  Example:
  user> `(syncing? web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `false`"
  [web3 & args]
  (js-apply (eth web3) "isSyncing" args))

(defn get-coinbase
  "Get the coinbase address to which mining rewards will go.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns a Promise that returns String - bytes 20: The coinbase address set in
  the node for mining rewards.

  Example:
  user> `(get-coinbase web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `\"0x85d85715218895ae964a750d9a92f13a8951de3d\"`"
  [web3 & args]
  (js-apply (eth web3) "getCoinbase" args))

(defn mining?
  "Checks whether the node is mining or not.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns a Promise that returns Boolean: true if the node is mining, otherwise
  false.

  Example:
  user> `(mining? web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `false`"
  [web3 & args]
  (js-apply (eth web3) "isMining" args))

(defn get-hashrate
  "Get the number of hashes per second that the node is mining with.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns a Promise that returns Number: Number of hashes per second.

  Example:
  user> `(get-hashrate web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `0`"
  [web3 & args]
  (js-apply (eth web3) "getHashrate" args))

(defn get-gas-price
  "Get the current gas price oracle. The gas price is determined by the last
  few blocks median gas price.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns Promise that returns String - Number string of the current gas price in wei.

  Example:
  user> `(get-gas-price web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `\"20000000000\"`"
  [web3 & args]
  (js-apply (eth web3) "getGasPrice" args))

(defn get-accounts
  "Get a list of accounts the node controls.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns a Promise that returns Array - An array of addresses controlled by node.

  Example:
  user> `(get-accounts web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `[0x85d85715218895ae964a750d9a92f13a8951de3d]`"
  [web3 & args]
  (js-apply (eth web3) "getAccounts" args))

(defn get-block-number
  "Get the current block number.

  Parameters:
  web3        - web3 instance
  callback-fn - callback with two parameters, error and result

  Returns a Promise that returns Number - The number of the most recent block.

  Example:
  `(get-block-number web3-instance (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `1783426`"
  [web3 & args]
  (js-apply (eth web3) "getBlockNumber" args))

(defn get-balance
  "Get the balance of an address at a given block.

  Parameters:
  web3          - web3 instance
  address       - The address to get the balance of.
  default-block - If you pass this parameter it will not use the default block
                  set with set-default-block.
  callback-fn   - Optional callback, returns an error object as first parameter and the result as second.

  Returns a Promise that returns String - The current balance for the given address in wei.

  Example:
  user> `(get-balance web3-instance
                      \"0x407d73d8a49eeb85d32cf465507dd71d507100c1\"
                      \"latest\"
                      (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `\"1000000000000\"`"
  [web3 & [address default-block :as args]]
  (js-apply (eth web3) "getBalance" args))

(defn get-storage-at
  "Get the storage at a specific position of an address.

  Parameters:
  web3          - web3 instance
  address       - The address to get the storage from.
  position      - The index position of the storage.
  default-block - If you pass this parameter it will not use the default block
                  set with web3.eth.defaultBlock.
  callback-fn   - callback with two parameters, error and result

  Returns a Promise returns String - The value in storage at the given position.

  Example:
  user> `(get-storage-at web3-instance
                         \"0x85d85715218895ae964a750d9a92f13a8951de3d\"
                         0
                         \"latest\"
                         (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `\"0x0000000000000000000000000000000000000000000000000000000000000000\"`"
  [web3 & [address position default-block :as args]]
  (js-apply (eth web3) "getStorageAt" args))

(defn get-code
  "Get the code at a specific address.

  Parameters:
  web3          - web3 instance
  address       - The address to get the code from.
  default-block - If you pass this parameter it will not use the default block set
                  with `get-default-block!`.
  callback-fn   - callback with two parameters, error and result

  Returns a Promise that returns String - The data at given address address.

  Example:
  user> (get-code web3-instance
                  \"0x85d85715218895ae964a750d9a92f13a8951de3d
                  0
                  \"latest\"
                  (fn [err res] (when-not err (println res))))
  nil
  user> `0x`"
  [web3 & [address default-block :as args]]
  (js-apply (eth web3) "getCode" args))

(defn get-block
  "Get a block matching the block number or block hash.

  Parameters:
  web3                        - web3 instance
  block-hash-or-number        - The block number or hash. Or the string
                                \"genesis\", \"latest\" or \"pending\"
                                as in the default block parameter.
  return-transaction-objects? - If true, the returned block will contain all
                                transactions as objects, if false it will
                                only contains the transaction hashes.
  callback-fn                 - callback with two parameters, error and result

  Returns a Promise that returns Object - The block object:
  - number: Number - The block number. null when its pending block.
  - hash: 32 Bytes,String - Hash of the block. null when its pending block.
  - parent-hash: 32 Bytes,String - Hash of the parent block.
  - nonce: 8 Bytes,String - Hash of the generated proof-of-work. null when its
    pending block.
  - sha3-uncles: 32 Bytes,String - SHA3 of the uncles data in the block.
  - logs-bloom: 256 Bytes,String - The bloom filter for the logs of the block.
    null when its pending block.
  - transactions-root: 32 Bytes,String - The root of the transaction trie of the block
  - state-root: 32 Bytes,String - The root of the final state trie of the block.
  - miner: String - The address of the beneficiary to whom the mining rewards were given.
  - difficulty: String - Integer of the difficulty for this block.
  - total-difficulty: String - Integer of the total difficulty of the chain until this block.
  - extra-data: String - The “extra data” field of this block.
  - size: Number - Integer the size of this block in bytes.
  - gas-limit: Number - The maximum gas allowed in this block.
  - gas-used: Number - The total used gas by all transactions in this block.
  - timestamp: Number - The unix timestamp for when the block was collated.
  - transactions: Array - Array of transaction objects, or 32 Bytes transaction hashes
    depending on the returnTransactionObjects parameter.
  - uncles: Array - Array of uncle hashes.

  Example:
  user> `(get-block web3-instance
                    0
                    false
                    (fn [err res] (when-not err (println res))))`
  nil
  user> {:state-root 0x.., :hash 0x.., :number 0, :difficulty #object[e 1048576],
         ...}"
  [web3 & [block-hash-or-number return-transaction-objects? :as args]]
  (js-apply (eth web3) "getBlock" args))

(defn get-block-transaction-count
  "Get the number of transaction in a given block.

  Parameters
  web3                 - web3 instance
  block-hash-or-number - The block number or hash. Or the string \"genesis\",
                         \"latest\" or \"pending\" as in the default block
                         parameter.
  callback-fn          - callback with two parameters, error and result

  Returns a Promise that returns Number - The number of transactions in the given block.

  Example:
  user> `(get-block-transaction-count
           web3-instance
           0
           (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `0`"
  [web3 & [block-hash-or-number :as args]]
  (js-apply (eth web3) "getBlockTransactionCount" args))

(defn get-uncle
  "Get a blocks uncle by a given uncle index position.

  Parameters:
  web3                        - web3 instance
  block-hash-or-number        - The block number or hash. Or the string
                                \"genesis\", \"latest\" or \"pending\" as in
                                the default block parameter
  uncle-number                - The index position of the uncle
  return-transaction-objects? - If true, the returned block will contain all
                                transactions as objects, if false it will only
                                contains the transaction hashes
  default-block               - If you pass this parameter it will not use the
                                default block set with (set-default-block!)
  callback-fn                 - callback with two parameters, error and result

  Returns a Promise that returns Object - the returned uncle.
  For a return value see `get-block`.

  Note: An uncle doesn’t contain individual transactions"
  [web3 & [block-hash-or-number uncle-number return-transaction-objects? :as args]]
  (js-apply (eth web3) "getUncle" args))

(defn get-transaction
  "Get a transaction matching the given transaction hash.

  Parameters:
  web3             - web3 instance
  transaction-hash - The transaction hash.
  callback-fn      - callback with two parameters, error and result

  Returns a Promise that returns Object - A transaction object its hash transactionHash:
  - hash: 32 Bytes, String - Hash of the transaction.
  - nonce: Number - The number of transactions made by the sender prior to this one.
  - block-hash: 32 Bytes, String - Hash of the block where this transaction was in. null when its pending.
  - block-number: Number - Block number where this transaction was in. null when its pending.
  - transaction-index: Number - Integer of the transactions index position in the block. null when its pending.
  - from: String - Address of the sender.
  - to: String - Address of the receiver. null when its a contract creation transaction.
  - value: String - Value transferred in wei.
  - gas-price: String - Gas price provided by the sender in wei.
  - gas: Number - Gas provided by the sender.
  - input: String - The data sent along with the transaction.

  Example:
  user> `(get-transaction
           web3-instance
           \"0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b\"
           (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `{
    :hash \"0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b\",
    :nonce 2,
    :block-hash \"0xef95f2f1ed3ca60b048b4bf67cde2195961e0bba6f70bcbea9a2c4e133e34b46\",
    :block-number 3,
    :transaction-index 0,
    :from \"0xa94f5374fce5edbc8e2a8697c15331677e6ebf0b\",
    :to \"0x6295ee1b4f6dd65047762f924ecd367c17eabf8f\",
    :value \"123450000000000000\",
    :gas 314159,
    :gas-price \"2000000000000\",
    :input \"0x57cb2fc4\"
  }`"
  [web3 & [transaction-hash :as args]]
  (js-apply (eth web3) "getTransaction" args))

(defn get-transaction-from-block
  "Get a transaction based on a block hash or number and the transactions index position.

  Parameters:
  web3                 - web3 instance
  block-hash-or-number - A block number or hash. Or the string \"genesis\",
                         \"latest\" or \"pending\" as in the default block
                         parameter.
  index                - The transactions index position.
  callback-fn          - callback with two parameters, error and result
  Number               - The transactions index position.

  Returns a Promise that returns Object - A transaction object, see `get-transaction`.

  Example:
  user> `(get-transaction-from-block
           web3-instance
           \"0x4534534534\"
           2
           (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> ;; see `get-transaction`"
  [web3 & [block-hash-or-number index :as args]]
  (js-apply (eth web3) "getTransactionFromBlock" args))

(defn get-transaction-receipt
  "Get the receipt of a transaction by transaction hash.

  Parameters:
  web3              - web3 instance
  transaction-hash  - The transaction hash.
  callback-fn       - callback with two parameters, error and result

  Returns a Promise that returns Object - A transaction receipt object,
  or null when no receipt was found:
  - status: Boolean - TRUE if the transaction was successful, FALSE, if the EVM reverted
    the transaction.
  - block-hash: 32 Bytes, String - Hash of the block where this transaction was in.
  - block-number: Number - Block number where this transaction was in.
  - transaction-hash: 32 Bytes, String - Hash of the transaction.
  - transaction-index: Number - Integer of the transactions index position in the block.
  - from: String - Address of the sender.
  - to: String - Address of the receiver. null when its a contract creation transaction.
  - contract-address: String - The contract address created, if the transaction was a
    contract creation, otherwise null.
  - cumulative-gas-used: Number - The total amount of gas used when this transaction
    was executed in the block.
  - gas-used: Number - The amount of gas used by this specific transaction alone.
  - logs: Array - Array of log objects, which this transaction generated.

  Example:
  user> `(get-transaction-receipt
           web3-instance
           \"0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b\"
           (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> {
  :status true,
  :transaction-hash \"0x9fc76417374aa880d4449a1f7f31ec597f00b1f6f3dd2d66f4c9c6c445836d8b\",
  :transaction-index 0,
  :block-hash \"0xef95f2f1ed3ca60b048b4bf67cde2195961e0bba6f70bcbea9a2c4e133e34b46\",
  :block-number 3,
  :contract-address \"0x11f4d0A3c12e86B4b5F39B213F7E19D048276DAe\",
  :cumulative-gas-used 314159,
  :gas-used 30234,
  :logs [{
         ;; logs as returned by `get-past-logs`, etc.
     } ...]
  }"
  [web3 & [transaction-hash :as args]]
  (js-apply (eth web3) "getTransactionReceipt" args))

(defn get-transaction-count
  "Get the numbers of transactions sent from this address.

  Parameters:
  web3          - web3 instance
  address       - The address to get the numbers of transactions from.
  default-block - If you pass this parameter it will not use the default block
                  set with set-default-block.
  callback-fn   - callback with two parameters, error and result

  Returns a Promise that returns Number - The number of transactions sent from
  the given address.

  Example:
  user> `(get-transaction-count web3-instance \"0x11f4d0A3c12e86B4b5F39B213F7E19D048276DAe\"
           (fn [err res] (when-not err (println res))))`
  #object[Promise [object Promise]]
  user> `1`"
  [web3 & [address default-block :as args]]
  (js-apply (eth web3) "getTransactionCount" args))

(defn send-transaction!
  "Sends a transaction to the network.

  Parameters:
  web3               - web3 instance
  transaction-object - The transaction object to send:
    :from: String - The address for the sending account. Uses the
                    `default-account` property, if not specified.
    :to: String   - (optional) The destination address of the message, left
                               undefined for a contract-creation
                               transaction.
    :value        - (optional) The value transferred for the transaction in
                               Wei, also the endowment if it's a
                               contract-creation transaction.
    :gas:         - (optional, default: To-Be-Determined) The amount of gas
                    to use for the transaction (unused gas is refunded).
    :gas-price:   - (optional, default: To-Be-Determined) The price of gas
                    for this transaction in wei, defaults to the mean network
                    gas price.
    :data:        - (optional) Either a byte string containing the associated
                    data of the message, or in the case of a contract-creation
                    transaction, the initialisation code.
    :nonce:       - (optional) Integer of a nonce. This allows to overwrite your
                               own pending transactions that use the same nonce.
  callback-fn   - callback with two parameters, error and result, where result
                  is the transaction hash

  Returns the PromiEvent: A promise combined event emitter.Will be resolved when
  the transaction receipt is available. Additionally the following events are
  available:
  - `:transaction-hash`: returns String: Is fired right after the transaction is
    sent and a transaction hash is available.
  - `:receipt`: returns Object: Is fired when the transaction receipt is available.
  - `:confirmation`: returns Number, Object: Is fired for every confirmation up to
    the 12th confirmation. Receives the confirmation number as the first and the
    receipt as the second argument. Fired from confirmation 0 on, which is the block
    where its minded.
  - `:error`: returns Error: Is fired if an error occurs during sending. If a out of
    gas error, the second parameter is the receipt.

  Example:
  user> (send-transaction! web3-instance {:to \"0x..\"}
          (fn [err res] (when-not err (println res))))
  nil
  user> 0x..."
  [web3 & [transaction-object :as args]]
  (js-apply (eth web3) "sendTransaction" args))

(defn send-signed-transaction!
  [web3 & args]
  (js-apply (eth web3) "sendSignedTransaction" args))

(defn sign
  [web3 & args]
  (js-apply (eth web3) "sign" args))

(defn sign-transaction
  "Sign a transaction. Method is not documented in the web3.js docs. Not sure if it is safe.

  Parameters:
  web3           - web3 instance
  sign-tx-params - Parameters of transaction
                   See `send-transaction!`
  private-key    - Private key to sign the transaction with
  callback-fn    - callback with two parameters, error and result

  Returns signed transaction data."
  [web3 & [sign-tx-params private-key signed-tx :as args]]
  (js-apply (eth web3) "signTransaction" args))

(defn call!
  "Executes a message call transaction, which is directly executed in the VM of
  the node, but never mined into the blockchain.

  Parameters:
  web3          - web3 instance
  call-object   - A transaction object see `send-transaction`, with the
                  difference that for calls the from property is optional as
                  well.
  default-block - If you pass this parameter it will not use the default block
                  set with set-default-block.
  callback-fn   - callback with two parameters, error and result

  Returns the returned data of the call as string, e.g. a codes functions return
  value.

  Example:
  user> `(call! web3-instance {:to   \"0x\"
                               :data \"0x\"}
                (fn [err res] (when-not err (println res))))`
  nil
  user> 0x"
  [web3 & [call-object default-block :as args]]
  (js-apply (eth web3) "call" args))

(defn estimate-gas
  "Executes a message call or transaction, which is directly executed in the VM
  of the node, but never mined into the blockchain and returns the amount of the
  gas used.

  Parameters:
  web3          - web3 instance
  call-object   - See `(send-transaction!)`, except that all properties are
                  optional.
  callback-fn   - callback with two parameters, error and result

  Returns the used gas for the simulated call/transaction.

  Example:
  user> `(estimate-gas web3-instance
           {:to   \"0x135a7de83802408321b74c322f8558db1679ac20\",
            :data \"0x135a7de83802408321b74c322f8558db1679ac20\"}
           (fn [err res] (when-not err (println res))))`
  nil
  user> 22361"
  [web3 & [call-object :as args]]
  (js-apply (eth web3) "estimateGas" args))

(defn get-past-logs
  [web3 & args]
  (js-apply (eth web3) "getPastLogs" args))

(defn get-work
  [web3 & args]
  (js-apply (eth web3) "getWork" args))

(defn submit-work!
  [web3 & args]
  (js-apply (eth web3) "submitWork" args))

(defproject web3.cljs "1.0.0-beta.36-1-SNAPSHOT"
  :description "Clojurescript API for Ethereum Web3 API."
  :url "https://github.com/223kazuki/web3.cljs"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojurescript "1.10.339"]
                 [camel-snake-kebab "0.4.0"]
                 [org.clojure/core.async "0.4.474"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}
  :profiles
  {:dev
   {:dependencies [[org.clojure/clojure "1.9.0"]
                   [binaryage/devtools "0.9.10"]
                   [cider/piggieback "0.3.10"]
                   [figwheel-sidecar "0.5.16"]
                   [org.clojure/tools.nrepl "0.2.13"]
                   [print-foo-cljs "2.0.3"]
                   [web3-cljs "1.0.0-beta.36-1"]]
    :plugins [[lein-figwheel "0.5.16"]]
    :source-paths ["env/dev"]
    :resource-paths ["resources"]
    :cljsbuild
    {:builds [{:id "dev"
               :source-paths ["src" "test"]
               :figwheel {:on-jsload web3.test-runner/run-all-tests}
               :compiler {:main web3.test-runner
                          :output-to "resources/public/js/compiled/app.js"
                          :output-dir "resources/public/js/compiled/out"
                          :asset-path "/js/compiled/out"
                          :source-map-timestamp true
                          :optimizations :none
                          :preloads [print.foo.preloads.devtools]}}]}}})

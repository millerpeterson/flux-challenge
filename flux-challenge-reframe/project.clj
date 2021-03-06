(defproject flux-challenge-reframe "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.908"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.2"]
                 [cljs-ajax "0.5.8"]
                 [haslett "0.1.1"]]

  :plugins [[lein-cljsbuild "1.1.5"]
            [lein-doo "0.1.8"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [day8.re-frame/trace "0.1.7"]
                   [figwheel-sidecar "0.5.13"]
                   [re-frisk "0.5.3"]
                   [com.cemerick/piggieback "0.2.2"]]

    :plugins      [[lein-figwheel "0.5.13"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "flux-challenge-reframe.core/mount-root"}
     :compiler     {:main                 flux-challenge-reframe.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           re-frisk.preload]
                    :optimizations :none
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "test"
     :source-paths ["src/cljs" "test"]
     :compiler     {:output-to "resources/public/js/tests/testable.js"
                    :output-dir "resources/public/js/tests"
                    :optimizations :none
                    :main flux-challenge-reframe.test-runner
                    :target :nodejs
                    }}


    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            flux-challenge-reframe.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}

  )

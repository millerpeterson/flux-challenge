(ns flux-challenge-reframe.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as rf]
            [flux-challenge-reframe.events :as events]
            [flux-challenge-reframe.views :as views]
            [flux-challenge-reframe.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  (rf/dispatch [::events/begin-obiwan-monitoring]))

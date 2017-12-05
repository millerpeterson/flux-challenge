(ns flux-challenge-reframe.views
  (:require [re-frame.core :as re-frame]
            [flux-challenge-reframe.subs :as subs]
            ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div "Whuts good from " @name]))

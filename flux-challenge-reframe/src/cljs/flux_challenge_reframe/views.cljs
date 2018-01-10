(ns flux-challenge-reframe.views
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.events :as ev]
            [flux-challenge-reframe.subs :as subs]))

(defn obiwan-location
  [location]
  [:h1.css-planet-monitor (str "Obi-Wan currently on " location)])

(defn sith-list
  [sith-with-homeworlds]
  [:ul.css-slots
   (map-indexed (fn [i sith]
                  [:li.css-slot {:key (get sith :name i)}
                   (when-let [name (:name sith)]
                     [:h3 name])
                   (when-let [home (:homeworld sith)]
                     [:h6 (str "Homeworld: " home)])])
                sith-with-homeworlds)])

(defn sith-list-scroll-controls
  []
  [:div.css-scroll-buttons
   [:button.css-button-up
    {:on-click (fn [_] (rf/dispatch [::ev/scroll ::ev/up]))}]
   [:button.css-button-down
    {:on-click (fn [_] (rf/dispatch [::ev/scroll ::ev/down]))}]])

(defn sith-list-with-controls
  [sith]
  [:section.css-scrollable-list
   [sith-list sith]
   [sith-list-scroll-controls]])

(defn main-panel []
  (let [missing-sith @(rf/subscribe [::subs/missing-sith])
        known-sith @(rf/subscribe [::subs/known-sith])
        obi-wan-loc @(rf/subscribe [::subs/obi-wan-location])]
    [:div.app-container
     [:div.css-root
      [obiwan-location obi-wan-loc]
      [sith-list-with-controls known-sith]]]))

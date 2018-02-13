(ns flux-challenge-reframe.views
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.events :as ev]
            [flux-challenge-reframe.subs :as subs]))

(defn obiwan-location
  [location]
  [:h1.css-planet-monitor (str "Obi-Wan currently on " location)])

(defn sith-list-slot
  [id]
  (let [sith @(rf/subscribe [::subs/sith-by-id id])
        homeworld (get-in sith [:homeworld :name])
        obi-wan-on-homeworld? @(rf/subscribe [::subs/obi-wan-on-siths-homeworld? id])]
    [:li.css-slot {:key id
                   :class [(when obi-wan-on-homeworld? "alert")]}
     [:h3 (get sith :name)]
     [:h6 (when (seq homeworld)
            (str "Homeworld: " homeworld))]]))

(defn sith-list
  [view-slots]
  (into [:ul.css-slots]
        (mapv (fn [slot-id]
                (if (nil? slot-id) [:li.css-slot]
                    (sith-list-slot slot-id)))
              view-slots)))

(defn sith-list-scroll-controls
  []
  [:div.css-scroll-buttons
   [:button.css-button-up
    {:on-click (fn [_] (rf/dispatch [::ev/scroll ::ev/up]))}]
   [:button.css-button-down
    {:on-click (fn [_] (rf/dispatch [::ev/scroll ::ev/down]))}]])

(defn sith-list-with-controls
  [view-slots]
  [:section.css-scrollable-list
   [sith-list view-slots]
   [sith-list-scroll-controls]])

(defn main-panel []
  (let [obi-wan-loc @(rf/subscribe [::subs/obi-wan-location])
        view-slots @(rf/subscribe [::subs/view-slots])]
    [:div.app-container
     [:div.css-root
      [obiwan-location (get obi-wan-loc :name)]
      [sith-list-with-controls view-slots]]]))

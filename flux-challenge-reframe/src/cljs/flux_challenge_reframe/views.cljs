(ns flux-challenge-reframe.views
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.subs :as subs]))

(defn obiwan-location
  [location]
  [:h1.css-planet-monitor (str "Obi-Wan currently on " location)])

(defn sith-list
  [sith-with-homeworlds]
  [:ul.css-slots
   (map (fn [sith]
           [:li.css-slot {:key (:name sith)}
            [:h3 (:name sith)]
            [:h6 (str "Homeworld: " (:homeworld sith))]])
         sith-with-homeworlds)])

(defn sith-list-scroll-controls
  []
  [:div.css-scroll-buttons
   [:button.css-button-up
    {:on-click (fn [_] (rf/dispatch [:scroll :up]))}]
   [:button.css-button-down
    {:on-click (fn [_] (rf/dispatch [:scroll :down]))}]])

(defn sith-list-with-controls
  [sith]
  [:section.css-scrollable-list
   [sith-list sith]
   [sith-list-scroll-controls]])

(def test-sith
  [{:name "Billy Coolguy" :homeworld "Earth"}
   {:name "Willy Niceman" :homeworld "Mercury"}])

(defn main-panel []
  [:div.app-container
   [:div.css-root
    [obiwan-location "Tatooine"]
    [sith-list-with-controls test-sith]]])

(comment
  (.log js/console "Whut?")
  )

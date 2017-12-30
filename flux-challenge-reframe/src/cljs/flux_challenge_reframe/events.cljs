(ns flux-challenge-reframe.events
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(defn scrolled-by-steps
  [num-steps step-fn view]
  (last (take (+ num-steps 1) (iterate step-fn view))))

(defn scrolled-step-up
  [view]
  (into [nil] (take 3 view)))

(defn scrolled-step-down
  [view]
  (conj (subvec view 1) nil))

(defn view-scrolled
  [view direction num-steps]
  (scrolled-by-steps num-steps
                     (case direction
                       ::down scrolled-step-down
                       ::up scrolled-step-up)
                     view))

(rf/reg-event-fx
 ::scroll
 (fn [cofx [_ direction]]
   {:db (update-in (get cofx :db) [:view]
                   #(view-scrolled % direction 1))}))

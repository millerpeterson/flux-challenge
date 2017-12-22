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
  (pop (conj view nil)))

(defn scrolled-step-down
  [view]
  (into #queue [nil] (take 3 view)))

(defn view-scrolled
  [view direction num-steps]
  (scrolled-by-steps num-steps
                     (case direction
                       ::down scrolled-step-down
                       ::up scrolled-step-up)
                     view))

(rf/reg-event-db
 ::scroll
 (fn [db [_ direction]]
   (update-in db [:view] #(view-scrolled % direction 1))))

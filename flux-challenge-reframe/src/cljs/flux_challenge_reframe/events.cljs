(ns flux-challenge-reframe.events
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]
            [devtools.defaults :as d]))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(defn scrolled-by-steps
  "The view scrolled by a step function a number of times."
  [num-steps step-fn view]
  (last (take (+ num-steps 1) (iterate step-fn view))))

(defn scrolled-step-up
  "The view scrolled a step up."
  [view]
  (into [nil] (take (- db/num-view-slots 1) view)))

(defn scrolled-step-down
  "The view scrolled a step down."
  [view]
  (conj (subvec view 1) nil))

(defn view-scrolled
  "The view scrolled in a direction (up or down) by a set number of steps."
  [view direction num-steps]
  (scrolled-by-steps num-steps
                     (case direction
                       ::down scrolled-step-down
                       ::up scrolled-step-up)
                     view))

(rf/reg-event-fx
 ::scroll
 (fn [cofx [_ direction]]
   {:db (update-in (get cofx :db) [:view-slots]
                   #(view-scrolled % direction 1))}))

(rf/reg-event-fx
 ::request-sith-fetch
 (fn [cofx [_ sith-id]]
   ;; If this request is already in flight, nothing needs to happen.
   ;; If there is a request in flight, and its ID is visible, nothing needs to
   ;; happen.
   ;; If there is a request in flight, and its not visible, cancel it, issue new
   ;; request.
   ;; If there is no request in flight, issue new request.
   (.log js/console "Fetch Sith" sith-id)
   {:db (get cofx :db)}))

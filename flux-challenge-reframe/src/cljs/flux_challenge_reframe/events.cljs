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

(defn next-missing-sith-id
  "The id of the next sith that should be retrieved. Either the master above the
   topmost sith in the view slots, or the apprentice below the lowest sith in the
   view slots. There needs to be a space for them to go in the view slots, and
   the master is preferred over the apprentice. When slots are full, returns nil."
  [db]
  (first (keep (fn [[master-id apprentice-id]]
                 (cond
                   ;; Missing master above a visible apprentice.
                   (and (nil? master-id) (some? apprentice-id))
                   (get (get-in db [:sith  apprentice-id]) :master)
                   ;; Missing apprentice below a visible master.
                   (and (some? master-id) (nil? apprentice-id))
                   (get (get-in db [:sith master-id]) :apprentice)
                   ;; Slots full; no missing sith.
                   :else nil))
               (partition 2 1 (get db :view-slots)))))

(rf/reg-event-fx
 ::scroll
 (fn [cofx [_ direction]]
   (println (next-missing-sith-id (get cofx :db)))
   {:db (update-in (get cofx :db) [:view-slots]
                   #(view-scrolled % direction 1))}))

(ns flux-challenge-reframe.events
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(rf/reg-event-db
 :scroll
 (fn [db [_ direction]]
   (case direction
     :up (update-in db [:view :top-slot-rank] #(+ % 2))
     :down (update-in db [:view :top-slot-rank] #(- % 2))
     db)))

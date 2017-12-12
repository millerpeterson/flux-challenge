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
   (.log js/console "scroll event" direction)))

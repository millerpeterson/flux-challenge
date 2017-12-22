(ns flux-challenge-reframe.events
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]))

(rf/reg-event-db
 ::initialize-db
 (fn  [_ _]
   db/default-db))

(defn view-scrolled
  [view direction scroll-step]
  (case direction
    :down (last (take (+ scroll-step 1)
                      (iterate (comp pop #(conj % nil)) view)))
    :up (last (take (+ scroll-step 1)
                    (iterate #(into #queue [nil] %) (take 3 view))))
    ))

(rf/reg-event-db
 :scroll
 (fn [db [_ direction]]
   (update-in db [:view]
              (fn [v]
                (view-scrolled v direction 1)))))

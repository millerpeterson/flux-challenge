(ns flux-challenge-reframe.events
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]
            [ajax.core :as ajax]))

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

(defonce request-in-flight (atom nil))

(defn cancel-request-in-flight!
  []
  (let [req @request-in-flight]
    (when (some? req)
      (do (ajax/abort req)
          (reset! request-in-flight nil)))))

(defn do-fetch-sith-request!
  [id]
  (ajax/ajax-request
   {:uri (str "http://localhost:3000/dark-jedis/" id)
    :method :get
    :response-format (ajax/json-response-format {:keywords? true})
    :handler (fn [ok res]
               (.log js/console ok res)
               (reset! request-in-flight nil))}))

(rf/reg-fx
 ::issue-sith-request
 (fn [id]
   (cancel-request-in-flight!)
   (reset! request-in-flight (do-fetch-sith-request! id))
   (.log js/console @request-in-flight)))

(rf/reg-event-fx
 ::request-sith-fetch
 (fn [cofx [_ sith-id]]
   (.log js/console "Fetch Sith" sith-id)
   {:db (get cofx :db)
    ::issue-sith-request sith-id}))

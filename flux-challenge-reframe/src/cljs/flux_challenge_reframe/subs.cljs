(ns flux-challenge-reframe.subs
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]))

(rf/reg-sub
 :visible-sith
 (fn [db]
   (mapv (partial db/sith-by-id db) (db/view-slots db))))

(rf/reg-sub
 :obi-wan-location
 (fn [db]
   (get-in db [:obi-wan :location])))

(ns flux-challenge-reframe.subs
  (:require [re-frame.core :as rf]))

(defn sith-by-id
  [db id]
  (get-in db [:sith id]))

(rf/reg-sub
 :visible-sith
 (fn [db]
   (let [visible-ids (get db :view)]
     (mapv (partial sith-by-id db) visible-ids))))

(rf/reg-sub
 :obi-wan-location
 (fn [db]
   (get-in db [:obi-wan :location])))

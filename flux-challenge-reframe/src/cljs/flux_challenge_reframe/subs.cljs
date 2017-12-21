(ns flux-challenge-reframe.subs
  (:require [re-frame.core :as rf]))

(defn sith
  [db id]
  (get-in db [:sith id]))

(rf/reg-sub
 :visible-sith
 (fn [db]
   (mapv #(sith db %) (get-in db [:view]))))

(rf/reg-sub
 :obi-wan-location
 (fn [db]
   (get-in db [:obi-wan :location])))

(ns flux-challenge-reframe.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]
            [flux-challenge-reframe.events :as ev]))

(rf/reg-sub
 ::sith
 (fn [db]
   (get db :sith)))

(rf/reg-sub
 ::obi-wan-location
 (fn [db]
   (get-in db [:obi-wan :location])))

(rf/reg-sub
 ::view-slots
 (fn [db]
   (get db :view-slots)))

(rf/reg-sub-raw
 ::sith-by-id
 (fn [app-db [_ id]]
   (when (not (db/known-sith? @app-db id))
     (rf/dispatch [::ev/sith-under-investigation id]))
   (reaction
    (let [sith @(rf/subscribe [::sith])]
      (or (get sith id)
          (db/make-unknown-sith id))))))

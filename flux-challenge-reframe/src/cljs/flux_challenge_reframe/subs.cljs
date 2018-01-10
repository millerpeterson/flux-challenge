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

;; A vector of the sith in the view slots, arranged according to the visible order.
(rf/reg-sub
 ::known-sith
 (fn []
   [(rf/subscribe [::sith])
    (rf/subscribe [::view-slots])])
 (fn [[sith view-slots] _]
   (mapv #(get sith %) view-slots)))

;; The id of the next sith that should be retrieved. Either the master above the
;; topmost sith in the view slots, or the apprentice below the lowest sith in the
;; view slots. There needs to be a space for them to go in the view slots, and
;; the master is preferred over the apprentice. When slots are full, returns nil.
(rf/reg-sub
 ::next-missing-sith-id
 (fn []
   (rf/subscribe [::known-sith]))
 (fn [visible-sith _]
   (first (keep (fn [[master apprentice]]
                  (cond
                    ;; Missing master above a visible apprentice.
                    (and (nil? master) (some? apprentice))
                    (get apprentice :master)
                    ;; Missing apprentice below a visible master.
                    (and (some? master) (nil? apprentice))
                    (get master :apprentice)
                    ;; Slots full; no missing sith.
                    :else nil))
                (partition 2 1 visible-sith)))))

;; Dispatches requests to fetch any missing sith from the server.
(rf/reg-sub
 ::missing-sith
 (fn []
   (rf/subscribe [::next-missing-sith-id]))
 (fn [next-id _]
   (rf/dispatch [::ev/request-sith-fetch next-id])))

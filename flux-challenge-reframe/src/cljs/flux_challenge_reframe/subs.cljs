(ns flux-challenge-reframe.subs
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]))

(rf/reg-sub ::sith db/sith)
(rf/reg-sub ::obi-wan-location db/obi-wan-location)
(rf/reg-sub ::view-slots db/view-slots)

;; A vector of the sith in the view slots, arranged according to the visible order.
(rf/reg-sub
 ::visible-sith
 (fn []
   (rf/subscribe [::sith ::view-slots]))
 (fn [[sith view-slots]]
   (mapv #(get sith %) view-slots)))

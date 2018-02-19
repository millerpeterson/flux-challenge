(ns flux-challenge-reframe.slots
  (:require [flux-challenge-reframe.db :as db]
            [flux-challenge-reframe.events :as ev]
            [re-frame.core :as rf]))

;; This namespace deals with the views slots - the vertically stacked boxes in the UI where
;; the sith are displayed hierarchically from top to bottom (a master appears above their
;; apprentice).

(rf/reg-sub
 :view-slots
 (fn [db]
   (get db :view-slots)))

(defn visible-sith?
  "Is a given sith visible in the slots?"
  [db id]
  (some #(= % id) (get db :view-slots)))

;; Slots may be scrolled up or down. This may cause some sith to fall out of view, and new empty
;; slots to become visible.

(def scroll-step 2)

(defn scrolled-by-steps
  "The view-slots scrolled by a step function a number of times."
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

(defn scrolled
  "The db's view scrolled in a direction (up or down) by a set number of steps."
  [db direction num-steps]
  (update-in db [:view-slots]
             (fn [view]
               (scrolled-by-steps num-steps
                                  (case direction
                                    :down scrolled-step-down
                                    :up scrolled-step-up)
                                  view))))

;; When there are empty slots, we want to try to fill them in based on what we know about the sith
;; hierarchy. For example, if there is an empty spot above a visible sith, we know that spot should
;; be occupied by the sith's master. A sith that is not visible in the slots, but whose slot position
;; can be inferred from the hierarchy, is considered a "missing" sith.

(defn missing-masters-filled
  "The view with missing master id's filled in based on the sith hierarchy."
  [view sith]
  (conj (mapv (fn [[master-id apprentice-id]]
                (if (and (nil? master-id) (some? apprentice-id))
                  (-> (get sith apprentice-id)
                      (get :master))
                  master-id))
              (partition 2 1 view))
        (last view)))

(defn missing-apprentices-filled
  "The view with missing apprentice id's filled in based on the sith
   hierarchy."
  [view sith]
  (into [(first view)]
        (map (fn [[master-id apprentice-id]]
               (if (and (some? master-id) (nil? apprentice-id))
                 (get-in sith [master-id :apprentice])
                 apprentice-id))
             (partition 2 1 view))))

(defn missing-slots-filled
  "The db with the highest missing master and apprentice ids filled in based
   on the sith known hierarchy."
  [db]
  (let [sith (get db :sith)]
    (update-in db [:view-slots]
               (fn [view]
                 (-> view
                     (missing-apprentices-filled sith)
                     (missing-masters-filled sith))))))

;; Fill in any slots that are empty but whose id we can infer from an
;; adjacent master or apprentice.
(def fill-missing-slots
  (rf/->interceptor
   :id ::fill-missing-slots
   :after (fn [ctx]
            (update-in ctx [:effects :db] missing-slots-filled))))

;; Scroll the view slots up or down.
(rf/reg-event-fx
 :scroll
 [ev/remove-non-slotted-sith
  fill-missing-slots
  ev/cancel-non-slotted-inquiries]
 (fn [cofx [_ direction]]
   {:db (-> (get cofx :db)
            (scrolled direction scroll-step))}))

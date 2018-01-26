(ns flux-challenge-reframe.events
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]
            [ajax.core :as ajax]
            [reagent.core :as r]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
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
  "The db's view scrolled in a direction (up or down) by a set number of steps."
  [db direction num-steps]
  (update-in db [:view-slots]
             (fn [view]
               (scrolled-by-steps num-steps
                                  (case direction
                                    ::down scrolled-step-down
                                    ::up scrolled-step-up)
                                  view))))

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
  "The view with missing apprentice id's filled in based on the sith hierarchy."
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

;; Fill in any slots that are empty but whose id we can infer from an adjacent
;; master or apprentice.
(def fill-missing-slots
  (rf/->interceptor
   :id ::fill-missing-slots
   :after (fn [ctx]
            (update-in ctx [:effects :db] missing-slots-filled))))

(defn non-slotted-sith-removed
  "The db stripped of knowledge of sith who are not in the view slots."
  [db]
  (update-in db [:sith]
             (fn [sith]
               (select-keys sith
                            (filter (partial db/slotted-sith? db)
                                    (keys sith))))))

;; Remove any sith not in view slots from our known sith db.
(def remove-non-slotted-sith
  (rf/->interceptor
   :id ::remove-non-slotted-sith
   :after (fn [ctx]
            (update-in ctx [:effects :db] non-slotted-sith-removed))))

(def scroll-step 2)

(rf/reg-event-fx
 ::scroll
 [remove-non-slotted-sith fill-missing-slots]
 (fn [cofx [_ direction]]
   {:db (-> (get cofx :db)
            (view-scrolled direction scroll-step))}))

(defonce request-in-flight (atom nil))

(defn cancel-request-in-flight!
  []
  (when-let [req @request-in-flight]
    (do (ajax/abort req)
        (reset! request-in-flight nil))))

(defn sith-from-fetch-response
  [resp]
  {:id (get resp :id)
   :name (get resp :name)
   :homeworld (get-in resp [:homeworld :name])
   :master (get-in resp [:master :id])
   :apprentice (get-in resp [:apprentice :id])})

(rf/reg-fx
 ::sith-inquiry
 (fn [sith-id]
   (ajax/ajax-request
    {:uri (str "http://localhost:3000/dark-jedis/" sith-id)
     :method :get
     :response-format (ajax/json-response-format {:keywords? true})
     :handler (fn [[ok resp]]
                (.log js/console ok resp)
                (reset! request-in-flight nil)
                (when ok
                  (js/console.log "OK!" (sith-from-fetch-response resp))
                  (rf/dispatch [::sith-details-learned (sith-from-fetch-response resp)])
                ;;   (rf/dispatch [::inquiry-failed sith-id]))
                ))})))

(rf/reg-event-db
 ::sith-details-learned
 [fill-missing-slots]
 (fn [db [_ learned-sith]]
   (-> db
       (assoc-in [:sith (get learned-sith :id)] learned-sith)
       (assoc-in [:inquiries :in-progress] nil)
       ;; (update-in [:inquiries :to-do] remove #{(get :id learned-sith)})
       )))

(rf/reg-event-fx
 ::sith-under-investigation
 (fn [cofx [_ sith-id]]
   (.log js/console "I'm investigating:" sith-id)
   {:db (update-in (get cofx :db) [:inquiries :to-do] conj sith-id)
    ::sith-inquiry sith-id}
   ))

(rf/reg-event-db
 ::sith-no-longer-under-investigation
 (fn [db [_ sith-id]]
   (.log js/console "I'm done with:" sith-id)
   ;; (update-in db [:inquiries :to-do] remove #{sith-id})
   ))

(rf/reg-event-fx
 ::continue-inquiries
 (fn [cofx _]
   (js/console.log "I'll continue my inquiries!")
   (let [db (get cofx :db)
         in-progress (get-in db [:inquiries :in-progress])
         to-do (get-in db [:inquiries :to-do])]
     (if (or (not (db/slotted-sith? db in-progress))
             (and (nil? in-progress) (not (empty? to-do))))
       (let [next-target (first to-do)]
         {:db (assoc-in db [:inquiries :in-progress] next-target)
          ::sith-inquiry next-target})
       {:db db}))))

(rf/reg-event-db
 ::inquiry-failed
 (fn [db _]
   (assoc-in db [:inquiries :in-progress] nil)))

(ns flux-challenge-reframe.events
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [re-frame.core :as rf]
            [flux-challenge-reframe.db :as db]
            [ajax.core :as ajax]
            [reagent.core :as r]
            [cljs.core.async :as a :refer [<! >!]]
            [haslett.client :as ws]))

(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

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

(defn view-scrolled
  "The db's view scrolled in a direction (up or down) by a set number
   of steps."
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

(defn inquiring-about-non-slotted-sith?
  "Is the request currently in flight for a sith that is no longer visible?"
  [db]
  (not (db/slotted-sith? db (get db :inquiry-in-progress))))

;; When we are inquiring about a sith that is no longer visible, stop the
;; inquiry.
(def cancel-non-slotted-inquiries
  (rf/->interceptor
   :id ::cancel-non-slotted-inquiries
   :after (fn [ctx]
            (let [db (get-in ctx [:effects :db])]
              (if (inquiring-about-non-slotted-sith? db)
                (assoc-in ctx [:effects ::cancel-sith-inquiry]
                          (get db :inquiry-in-progress))
                ctx)))))

(defn inquiry-targets
  "A lazy sequence of sith who are visible but whose details are unknown."
  [db]
  (filter #(and (some? %)
                (not (db/known-sith? db %)))
          (get db :view-slots)))

(def continue-inquiries
  (rf/->interceptor
   :id ::continue-inquiries
   :after (fn [ctx]
            (let [db (get-in ctx [:effects :db])
                  next-target (first (inquiry-targets db))]
              (if (and (nil? (get db :inquiry-in-progress))
                       (some? next-target))
                (update ctx :effects
                        (fn [fx]
                          (-> fx
                              (assoc-in [:db :inquiry-in-progress] next-target)
                              (assoc ::begin-sith-inquiry next-target))))
                ctx)))))

(rf/reg-event-db
 ::sith-became-visible
 [continue-inquiries]
 identity)

(def scroll-step 2)

;; Scroll the view slots up or down.
(rf/reg-event-fx
 ::scroll
 [remove-non-slotted-sith
  fill-missing-slots
  cancel-non-slotted-inquiries]
 (fn [cofx [_ direction]]
   {:db (-> (get cofx :db)
            (view-scrolled direction scroll-step))}))

;; The current request in flight, or nil if none in flight.
(defonce request-in-flight (atom nil))

(defn cancel-request-in-flight!
  "Cancel the current request in flight, if any."
  []
  (when-let [req @request-in-flight]
    (do (ajax/abort req)
        (reset! request-in-flight nil))))

(defn inquiry-response
  "Parse the response to a sith inquiry."
  [resp]
  {:id (get resp :id)
   :name (get resp :name)
   :homeworld (get-in resp [:homeworld :name])
   :master (get-in resp [:master :id])
   :apprentice (get-in resp [:apprentice :id])})

;; Make a request to retrieve a sith's details.
(rf/reg-fx
 ::begin-sith-inquiry
 (fn [sith-id]
   (ajax/ajax-request
    {:uri (str "http://localhost:3000/dark-jedis/" sith-id)
     :method :get
     :response-format (ajax/json-response-format {:keywords? true})
     :handler (fn [[ok resp]]
                (reset! request-in-flight nil)
                (when ok (rf/dispatch [::sith-details-learned
                                       (inquiry-response resp)]))
                (rf/dispatch [::inquiry-completed sith-id]))})))

;; Stop an inquiry in progress.
 (rf/reg-fx
  ::cancel-sith-inquiry
  (fn [_]
    (cancel-request-in-flight!)))

 ;; The details about a sith were learned.
(rf/reg-event-db
 ::sith-details-learned
 [fill-missing-slots]
 (fn [db [_ learned-sith]]
   (assoc-in db [:sith (get learned-sith :id)] learned-sith)))

;; An inquiry completed (succeeded or failed).
(rf/reg-event-db
 ::inquiry-completed
 [continue-inquiries]
 (fn [db [_ completed-id]]
   (assoc db :inquiry-in-progress nil)))

(defonce ws-connection (atom nil))

;; Open a websocket connection to monitor for receiving updates on Obi-Wan's location.
(rf/reg-fx
 ::open-obi-wan-location-channel
 (fn [channel-address]
   (js/console.log channel-address)
   (go
     (reset! ws-connection (<! (ws/connect channel-address)))
     (loop []
       (let [location-json (<! (get @ws-connection :source))
             location (js->clj (js/JSON.parse location-json))]
         (js/console.log location)
         (rf/dispatch [::obi-wan-location-changed {:id (get location "id")
                                                   :name (get location "name")}])
         (recur))))))

;; Close the websocket connection that was monitoring Obi-Wan's location.
(rf/reg-fx
 ::close-obi-wan-location-channel
 (fn [_]
   (ws/close @ws-connection)))

;; Begin monitoring Obi-Wan's location.
(rf/reg-event-fx
 ::begin-obiwan-monitoring
 []
 (fn [cofx _]
   {::open-obi-wan-location-channel "ws://localhost:4000"}))

;; Stop monitoring Obi-Wan's location.
(rf/reg-event-fx
 ::stop-obiwan-monitoring
 []
 (fn [cofx _]
   {::close-obi-wan-location-channel nil}))

(rf/reg-event-db
 ::obi-wan-location-changed
 []
 (fn [db [_ new-location]]
   []
   (assoc db :obi-wan-location new-location)))

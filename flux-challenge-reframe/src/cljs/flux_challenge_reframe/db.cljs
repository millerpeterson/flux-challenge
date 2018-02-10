(ns flux-challenge-reframe.db)

(def default-db
  {:obi-wan-location {:id 8
                      :location "Crematoria"}
   :sith {}
   :view-slots [nil 3616 nil nil]
   :inquiry-in-progress nil
   })

(def num-view-slots 4)

(defn known-sith?
  [db id]
  (contains? (get db :sith) id))

(defn slotted-sith?
  [db id]
  (some #(= % id) (get db :view-slots)))

(defn make-unknown-sith
  [id]
  {:id id
   :name ""
   :homeworld ""
   :master -1
   :apprentice -1})

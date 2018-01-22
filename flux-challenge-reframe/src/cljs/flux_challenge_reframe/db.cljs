(ns flux-challenge-reframe.db)

(def default-db
  {:obi-wan {:location "Crematoria"}
   :sith {30 {:id 30
              :name "Billy Coolguy"
              :homeworld "Earth"
              :master 65
              :apprentice 743}
          65 {:id 65
              :name "Wolly Woodberg"
              :homeworld "The Carb Nebula"
              :master 936
              :apprentice 30}
          743 {:id 743
               :name "Burt Sniffer"
               :homeworld "Jupiter"
               :master 30
               :apprentice 932}}
   :view-slots [65 30 743 932]
   })

(def num-view-slots 4)

(defn known-sith?
  [db id]
  (contains? (get db :sith) id))

(defn slotted-sith?
  [db id]
  (contains? (get db :view-slots) id))

(defn make-unknown-sith
  [id]
  {:id id
   :name ""
   :homeworld ""
   :master -1
   :apprentice -1})

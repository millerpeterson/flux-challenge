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
   :view-slots [65 30 nil nil]
   :sith-fetches {}})

(def num-view-slots 4)

(defn sith
  "A map of ids to sith."
  [db]
  (get db :sith))

(defn view-slots
  "A vector representing the view slots."
  [db]
  (get db :view-slots))

(defn obi-wan-location
  "The current location of Obi-Wan."
  [db]
  (get-in db [:obi-wan :location]))

(ns flux-challenge-reframe.db)

(def default-db
  {:obi-wan {:location "Crematoria"}
   :sith {30 {:name "Billy Coolguy"
              :homeworld "Earth"
              :master 65
              :apprentice 743}
          65 {:name "Wolly Woodberg"
              :homeworld "The Carb Nebula"
              :master 936
              :apprentice 30}
          743 {:name "Burt Sniffer"
               :homeworld "Jupiter"
               :master 30
               :apprentice 932}}
   :view [65 30 nil nil]
   })

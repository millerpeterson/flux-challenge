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
   :view-slots [65 30 nil nil]
   :sith-fetches {}})

(def num-view-slots 4)

(defn view-slots
  "Return a vector representing the view slots."
  [db]
  (get db :view-slots))

(defn sith-by-id
  "Retrieve a sith by its id."
  [db id]
  (get-in db [:sith id]))

(defn master-view-pos
  "The index of a sith's master in the view slots, or nil if the master is
   not present."
  [db sith-id]
  (let [sith (sith-by-id db sith-id)]
    (first (keep-indexed #(when (= %2 (get sith :master)) %1)
                         (view-slots db)))))

(defn apprentice-view-pos
  "The index of a sith's apprentice in the view slots, or nil if the
   apprentice is not present."
  [db sith-id]
  (let [sith (sith-by-id db sith-id)]
    (first (keep-indexed #(when (= %2 (get sith :apprentice)) %1)
                         (view-slots db)))))

(defn missing-master-id
  "Returns the id of the master of the sith in the view slots that has an
   open slot above them, or nil if there are no empty slots above the
   highest sith."
  [db]
  (println "master" (partition 2 1 (view-slots db)))
  (first (keep (fn [[m a]]
                 (when (and (nil? m) (some? a))
                   (get (sith-by-id db a) :master)))
               (partition 2 1 (view-slots db)))))

(defn missing-apprentice-id
  "Returns the id of the apprentice of the lowest sith in the view slots
   that has an open slot below them, or nil if there are no empty slots
   below the lowest sith."
  [db]
  (println "apprentice" (partition 2 1 (view-slots db)))
  (first (keep (fn [[m a]]
                 (when (and (some? m) (nil? a))
                   (get (sith-by-id db m) :apprentice)))
               (partition 2 1 (view-slots db)))))

(defn missing-sith-id
  "Returns the next missing master or apprentice, in that order of
   preference. When the view slots are full, returns nil."
  [db]
  (first (filter some? ((juxt missing-master-id
                              missing-apprentice-id)
                        db))))

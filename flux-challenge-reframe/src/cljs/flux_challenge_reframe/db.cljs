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
   :view {:scroll-ref-sith-id 30
          :scroll-top-pos 0}})

(defn ref-sith
  [db]
  (let [ref-sith-id (get-in db [:view :scroll-ref-sith-id])]
    (get-in db [:sith ref-sith-id])))

(defn apprentice-of
  [db sith]
  (when-let [apprentice-id (:apprentice sith)]
    (get-in db [:sith apprentice-id])))

(defn master-of
  [db sith]
  (when-let [apprentice-id (:master sith)]
    (get-in db [:sith apprentice-id])))

(defn known-apprentices
  [db sith]
  (lazy-seq
   (when-let [apprentice (apprentice-of db sith)]
     (cons apprentice (known-apprentices db apprentice)))))

(defn known-masters
  [db sith]
  (lazy-seq
   (when-let [master (master-of db sith)]
     (cons master (known-masters db master)))))

(def unknown-sith
  {})

(defn padded-with-unknowns
  [sith num-desired]
  (take num-desired (concat sith (repeat unknown-sith))))

(defn padded-visible-masters
  [db num-slots]
  (let [highest-visible-pos (max 0 (- (get-in db [:view :scroll-top-pos])))]
    (take (min highest-visible-pos num-slots)
          (reverse (padded-with-unknowns (known-masters db (ref-sith db))
                                         highest-visible-pos)))))

(defn padded-visible-apprentices
  [db sith]
  [])

(comment
  (defn visible-sith
    [db num-slots]
    (let [ref-sith (get-in db [:sith (get-in db [:view :scroll-ref-sith-id])])
          num-mstrs-of-ref (max 0 (- (:scroll-top-pos db)))
          scroll-bottom-pos (+ (:scroll-top-pos db) num-slots)
          num-apprs-of-ref (max 0 (- scroll-bottom-pos 1))
          padded-mstrs (padded-with-unknowns
                        (take num-mstrs-of-ref
                              (known-masters db ref-sith))
                        num-mstrs-of-ref)
          padded-apprs (padded-with-unknowns
                        (take num-apprs-of-ref
                              (known-apprentices db ref-sith)))]
      )))

(comment


  )

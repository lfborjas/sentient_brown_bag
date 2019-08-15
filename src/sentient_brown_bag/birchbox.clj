(ns sentient-brown-bag.birchbox
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]
            [clojure.core.logic.pldb :as r]))

(r/db-rel sample title concern)
(r/db-rel dislikes user concern)

(def facts0
  (r/db
   [sample "Protect & Detangle" :hair]
   [sample "Olivina All-In-One" :hair]
   [sample "Hanz de Fuko Natural Shampoo" :hair]
   [sample "Body Fuel" :skin]
   [sample "Turbo Wash" :skin]
   [sample "Rare Earth Mask" :skin]
   [sample "Smashbox Always On" :makeup]
   [sample "Arrow Color Enhancing" :makeup]
   [sample "Dr Jart Premium Beauty" :makeup]
   [sample "Judith has a musket" :fragrance]
   [sample "CLEAN acid rain" :fragrance]
   [sample "thisworks light sleep" :fragrance]

   [dislikes "Luis" :makeup]
   [dislikes "Romy" :fragrance]))

;; defining some rules (subgoals/axioms)
(defn likeso [u s]
  (logic/fresh [c d]
    (sample   s  c)
    (dislikes u  d)
    (logic/!= c  d)))

(defn dislikeso [u s]
  (logic/fresh [c]
    (sample   s c)
    (dislikes u c)))

(defn sampleo [v]
  (logic/fresh [s c]
    (sample s c)
    (logic/== s v)))

(defmacro ask-db [db vars query]
  `(r/with-db ~db (logic/run* ~vars ~query)))

;; if a variable is returned, it's because the rule can be satisfied
(comment
  (r/with-db facts0 (logic/run* [q] (sampleo "Smashbox Always On")))
  (ask-db facts0 [q] (sampleo "Smashbox Always Off"))
  (ask-db facts0 [q] (sampleo q))
  (ask-db facts0 [q] (dislikeso "Luis" "Smashbox Always On"))
  (ask-db facts0 [q] (likeso "Luis" "Smashbox Always On"))
  (ask-db facts0 [q] (likeso "Romy" "Smashbox Always On"))
  (ask-db facts0 [q] (likeso "Romy" q))
  (ask-db facts0 [q] (dislikeso "Luis" q))
  (ask-db facts0 [u q] (dislikeso u q)))

(defn allocations [u n]
  (let [vars (repeatedly 3 logic/lvar)]
    (r/with-db facts0
      (logic/run n [q]
        (logic/== q vars)
        (logic/distincto q)
        (logic/everyg sampleo vars)
        (logic/everyg (partial likeso u) vars)))))

(comment
  (allocations "Luis" 1)
  (allocations "Romy" 3))

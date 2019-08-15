(ns sentient-brown-bag.birchbox
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]
            [clojure.core.logic.pldb :as r]))


;; relations and facts: set up the "universe" of knowledge

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
(defn sampleo [v]
  (logic/fresh [s c]
    (sample s c)
    (logic/== [s c] v)))

(defn likeso [u [s c]]
  (logic/fresh [d]
    (sampleo   [s c])
    (dislikes   u  d)
    (logic/!=   c  d)))

(defn dislikeso [u [s c]]
  (logic/fresh [d]
    (sampleo   [s c])
    (dislikes   u  d)
    (logic/==   c  d)))

(defmacro ask-db [db vars query]
  `(r/with-db ~db (logic/run* ~vars ~query)))

;; if a variable is returned, it's because the rule can be satisfied
(comment
  (r/with-db facts0 (logic/run* [q] (sampleo ["Smashbox Always On" :makeup])))
  (ask-db facts0 [q] (sampleo ["Smashbox Always Off" :makeup]))
  (ask-db facts0 [q] (sampleo q))
  (ask-db facts0 [q] (dislikeso "Luis" ["Smashbox Always On" :makeup]))
  (ask-db facts0 [q] (likeso "Luis" ["Smashbox Always On" :makeup]))
  (ask-db facts0 [q] (likeso "Romy" ["Smashbox Always On" :makeup]))
  (ask-db facts0 [q p] (likeso "Romy" [q p]))
  (ask-db facts0 [q p] (dislikeso "Luis" [q p]))
  (ask-db facts0 [u q p] (dislikeso u [q p]))q)

;; use the rules to "solve" for allocations declaratively

(defn allocations [u sz]
  (let [vars (repeatedly sz #(vec [(logic/lvar) (logic/lvar)]))]
    (r/with-db facts0
      (logic/run* [q]
        (logic/== q vars)
        (logic/distincto (map second vars))
        (logic/everyg sampleo vars)
        (logic/everyg (partial likeso u) vars)))))


(comment
  (allocations "Luis" 3)
  (allocations "Romy" 3))

;; from https://github.com/clojure/core.logic/wiki/Features

;; finite domains (constraint logic programming)

(comment
  (logic/run* [q] (fd/in q (fd/interval 1 5)))
  (logic/run* [q]
    (logic/fresh [x y]
      (fd/in x y (fd/interval 1 10))
      (fd/+ x y 10)
      #_(fd/distinct [x y])
      (logic/== q [x y]))))

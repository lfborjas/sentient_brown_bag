(ns sentient-brown-bag.minilogic
  (:require [clojure.walk :as walk]))

;; Baby representation of what a logic solver could be

(defn lvar?
  "Is the given value a logic variable?"
  [x]
  (boolean
   (when (symbol? x)
     (re-matches #"^\?.*" (name x)))))

(comment
  (lvar? '?x)
  (lvar? 'a)
  (lvar? '2)
  (lvar? '(1 ?x)))

(defn satisfy1
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (= L R) knowledge
      (lvar? L) (assoc knowledge L R)
      (lvar? R) (assoc knowledge R L)
      :default nil)))

(comment
  ;; notice that now you can establish knowledge, and
  ;; even build upon it to make some rudimentary inferences
  (satisfy1 '?something 2 {})
  (satisfy1 2 '?something {})
  (satisfy1 '?x '?y {})
  (->> {}
       (satisfy1 '?x '?y)
       (satisfy1 '?x 1)))

(defn satisfy
  [l r knowledge]
  (let [L (get knowledge l l)
        R (get knowledge r r)]
    (cond
      (not knowledge) nil
      (= L R)         knowledge
      (lvar? L)       (assoc knowledge L R)
      (lvar? R)       (assoc knowledge R L)
      (every? seq? [L R])
      (satisfy (rest L)
               (rest R)
               (satisfy (first L)
                        (first R)
                        knowledge))
      :default nil)))

(comment
  ;;with the above definition, we can now satisfy recursive/nested patterns:
  (satisfy '(1 2 3) '(1 ?something 3) {})
  (satisfy '(?x ?y) '(?x 1) {})
  (satisfy '(?x 2 3  (4 5 ?z))
           '(1  2 ?y (4 5 6))
           {})
  (satisfy '(?x 666 3) '(1 2 ?y) {}))


(defn subst [term binds]
  (walk/prewalk
   (fn [expr]
     (if (lvar? expr)
       (or (binds expr) expr)
       expr))
   term))

(comment
  (subst '(1 ?x 3) '{?x 2})
  (subst '{:a ?x, :b [1 ?x 3]} '{?x 2}))

;; UNIFICATION
;; to unify is to take two terms, see if they contain information to satisfy each other's variables,
;; substitute those variables for which we've found terms, and return the new "unified" result

(defn unify [term1 term2]
  (->> {}
       (satisfy term1 term2)
       (subst term1)))

(comment
  (unify '(1 ?x 3) '(1 2 ?y))
  (unify '(1 ?x) '(?y (?y 2))))

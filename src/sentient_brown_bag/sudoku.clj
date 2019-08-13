(ns sentient-brown-bag.sudoku
  (:require [clojure.pprint :as p]
            [clojure.set    :as set]
            [clojure.core.logic :as logic]
            [clojure.core.logic.fd :as fd]))

;; all from: https://github.com/lfborjas/clojure-playground/blob/master/src/clojure_playground/sudoku.clj

;; For ease of manipulation, we expect board to be a single
;; vector of 81 values, with blank cells represented as a
;; hyphen.
(def example-board
  '[3 - - - - 5 - 1 -
    - 7 - - - 6 - 3 -
    1 - - - 9 - - - -
    7 - 8 - - - - 9 -
    9 - - 4 - 8 - - 2
    - 6 - - - - 5 - 1
    - - - - 4 - - - 6
    - 4 - 7 - - - 2 -
    - 2 - 6 - - - - 3])


;; A couple of convenience functions for REPL printing

(defn prep
  "Will create a list with lists representing each row,
  and each row will contain 3-tuples,
  e.g. (((3 - -) (- - 5) (- 1 -))...)"
  [board]
  (map #(partition 3 %)
       (partition 9 board)))

(comment ;; in emacs, you can use `C-c C-e` to eval:
  (p/pprint (prep example-board)))


(defn print-board
  "Pretty-prints a prepped board adding separators around sub-grids"
  [board]
  (let [row-sep (apply str (repeat 31 "-"))]
    (println row-sep)
    (dotimes [row (count board)]
      (print "| ")
      (doseq [subrow (nth board row)]
        (doseq [cell (butlast subrow)]
          (print (str cell "  ")))
        (print (str (last subrow) " | ")))
      (println)
      (when (zero? (mod (inc row) 3))
        (println row-sep)))))

(def pprint-board
  (comp print-board prep))

(comment ;; in emacs, try using `C-c C-m` to macroexpand:
  (print (-> example-board prep print-board))
  (pprint-board example-board))


;; GET READY FOR SOME BRUTE FORCE!!

;; first, some utility functions for navigation:
;; given a vector of rows:
;; get the row, column or subgrid
;; containing a given index in the original 1d board-vector
;; (e.g. the subgrid containing the 80th number is
;; (- - 6 - 2 - - - 3)
;; i.e. the last one, same with the 79th num, etc.)

(defn rows [board sz]
  (partition sz board))

(defn row-for [board index sz]
  (nth (rows board sz) (/ index 9)))

(defn column-for [board index sz]
  (let [col (mod index sz)]
    (map #(nth % col)
         (rows board sz))))

(defn subgrid-for [board i]
  (let [rows    (rows board 9)
        sgcol   (/ (mod i 9) 3)
        sgrow   (/ (/ i 9) 3)
        grp-col (column-for (mapcat #(partition 3 %) rows) sgcol 3)
        grp     (take 3 (drop (* 3 (int sgrow)) grp-col))]
    (flatten grp)))

(defn index
  "Generate a sequence of index/value pairs"
  [coll]
  (cond
   (map? coll) (seq coll)
   (set? coll) (map vector coll coll)
   :else (map vector (iterate inc 0) coll)))

(defn pos
  "Given a predicate and a collection, find all indices
  that satisfy the predicate"
  [pred coll]
  (for [[i v] (index coll) :when (pred v)]
    i))

(comment
  (p/pprint (rows example-board 9))
  (p/pprint (row-for example-board 0 9))
  (p/pprint (column-for example-board 7 9))
  (p/pprint (subgrid-for example-board 1))
  (index (range 1 10))
  (pos #{2} (range 1 10)))

;; now, the big functions for solving a puzzle:
;; Figure out the state of a given board, and the options that are legal

(defn numbers-present-for
  "Given a cell's index, inspect all the numbers
  in its column, row and subgrid; sans duplicates"
  [board i]
  (set
   (concat (row-for board i 9)
           (column-for board i 9)
           (subgrid-for board i))))

(defn possible-placements
  "Given a cell, find the numbers in its
  col, row and grid that are missing from 1-9"
  [board index]
  (set/difference #{1 2 3 4 5 6 7 8 9}
                  (numbers-present-for board index)))

(comment
  (numbers-present-for example-board 10)
  (possible-placements example-board 10))

(defn solve-brute-force
  "Given a board, find the first of all instances of an
  empty cell, and map over all possible numbers that could fit
  it legally; recurring over the results until there's no empty cells"
  [board]
  (if-let [[i & _]
           (and (some '#{-} board)
                (pos  '#{-} board))]
    (flatten (map #(solve-brute-force (assoc board i %))
                  (possible-placements board i)))
    board))

(comment
  (time (-> example-board solve-brute-force pprint-board)))


;; LOGIC TIME

;; The following three functions define "views" of the board
;; as lists of flat sequences: all rows, all columns, all grids.
;; That way we can reason about the puzzle being solved if
;; the constraints of Sudoku all met in all views

(defn rowify
  "Returns a vector of vectors that correspond to rows"
  [board]
  (->> board
       (partition 9)
       (map vec)
       vec))

(defn colify
  "Returns a list of vectors corresponding to columns, given rows
  e.g. (-> example-board rowify colify)"
  [rows]
  (apply map vector rows))

(defn subgrid
  "Returns a list of lists representing the 3x3 subgrids as linear vectors"
  [rows]
  (partition 9
             (for [row (range 0 9 3)
                   col (range 0 9 3)
                   x (range row (+ row 3))
                   y (range col (+ col 3))]
               (get-in rows [x y]))))

(comment
  (print example-board)
  (p/pprint (rowify example-board))
  (p/pprint (-> example-board rowify colify))
  (p/pprint (-> example-board  rowify subgrid)))

;; some setup for the logic stuff:

;;; Create a fresh logic board (i.e. a bunch of unknowns to begin with)

(def logic-board #(repeatedly 81 logic/lvar))

(defn init
  "Takes a blank logic board and a puzzle board and recursively
  binds logic variables to the latter's knowns and unknowns"
  [[lv & lvs] [cell & cells]]
  (if lv
    (logic/fresh []
      (if (= '- cell)
        logic/succeed
        (logic/== lv cell))
      (init lvs cells))
    logic/succeed))

(comment
  (p/pprint (take 9 (logic-board)))
  ;; TODO: how to print `init`?
  #_(p/pprint (init (logic-board) example-board)))

(defn solve-logically [board]
  (let [legal-nums (fd/interval 1 9)
        lvars (logic-board)
        rows  (rowify lvars)
        cols  (colify rows)
        grids (subgrid rows)]
    (logic/run 1 [q]
      (init lvars board)
      (logic/everyg #(fd/in % legal-nums) lvars)
      (logic/everyg fd/distinct rows)
      (logic/everyg fd/distinct cols)
      (logic/everyg fd/distinct grids)
      (logic/== q lvars))))

(comment
  (pprint-board example-board)
  (time (-> example-board solve-logically first pprint-board))
  (time (-> example-board solve-brute-force pprint-board)))




;; Starting board:
;; -------------------------------
;; | 3  -  - | -  -  5 | -  1  - | 
;; | -  7  - | -  -  6 | -  3  - | 
;; | 1  -  - | -  9  - | -  -  - | 
;; -------------------------------
;; | 7  -  8 | -  -  - | -  9  - | 
;; | 9  -  - | 4  -  8 | -  -  2 | 
;; | -  6  - | -  -  - | 5  -  1 | 
;; -------------------------------
;; | -  -  - | -  4  - | -  -  6 | 
;; | -  4  - | 7  -  - | -  2  - | 
;; | -  2  - | 6  -  - | -  -  3 | 
;; -------------------------------
;; BRUTE FORCE:
;; -------------------------------
;; | 3  8  6 | 2  7  5 | 4  1  9 | 
;; | 4  7  9 | 8  1  6 | 2  3  5 | 
;; | 1  5  2 | 3  9  4 | 8  6  7 | 
;; -------------------------------
;; | 7  3  8 | 5  2  1 | 6  9  4 | 
;; | 9  1  5 | 4  6  8 | 3  7  2 | 
;; | 2  6  4 | 9  3  7 | 5  8  1 | 
;; -------------------------------
;; | 8  9  3 | 1  4  2 | 7  5  6 | 
;; | 6  4  1 | 7  5  3 | 9  2  8 | 
;; | 5  2  7 | 6  8  9 | 1  4  3 | 
;; -------------------------------
;; "Elapsed time: 933.055016 msecs"
;;
;; LOGICALLY:
;; -------------------------------
;; | 3  8  6 | 2  7  5 | 4  1  9 | 
;; | 4  7  9 | 8  1  6 | 2  3  5 | 
;; | 1  5  2 | 3  9  4 | 8  6  7 | 
;; -------------------------------
;; | 7  3  8 | 5  2  1 | 6  9  4 | 
;; | 9  1  5 | 4  6  8 | 3  7  2 | 
;; | 2  6  4 | 9  3  7 | 5  8  1 | 
;; -------------------------------
;; | 8  9  3 | 1  4  2 | 7  5  6 | 
;; | 6  4  1 | 7  5  3 | 9  2  8 | 
;; | 5  2  7 | 6  8  9 | 1  4  3 | 
;; -------------------------------
;; "Elapsed time: 49.425434 msecs"

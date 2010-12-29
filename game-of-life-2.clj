;
; Missing features:
; - error control (ensure input is correct).
; - make it work for non-square matrices

; The namespace
(ns GameOfLife)

; Return the value of the cell at coordinates [x y] in the matrix.
; If the coordinates are outside of the matrix, then the value 0 is returned.
;
(defn get-value[x y matrix]
  (if 
    (or (< x 0) (< y 0) (>= x (count matrix)) (>= y (count matrix)) ) 
    0
    ((matrix x) y))
  )

; Brute-force algorithm to calculate the number of living neighbours to a cell
; with coordinates [x y] in matrix. We assume (for the moment) that the value
; in a cell is either 0 or 1. (Need to control this at some stage!).
;
(defn count-living-neighbours [x y matrix]
  (+
    (get-value (- x 1) (- y 1) matrix)
    (get-value x (- y 1) matrix)
    (get-value (+ x 1) (- y 1) matrix)
    (get-value (- x 1) y matrix)
    (get-value (+ x 1) y matrix)
    (get-value (- x 1) (+ y 1) matrix)
    (get-value x (+ y 1) matrix)
    (get-value (+ x 1) (+ y 1) matrix)
    )
  )

; Determine if a cell lives or dies in the next generation. Straight forward
; implementation of the algorithm on the blackboard.
;
(defn live-or-die? [x y matrix]
  (cond 
    ( < (count-living-neighbours x y matrix) 2) 0 
    ( == (count-living-neighbours x y matrix) 2) (get-value x y matrix) 
    ( == (count-living-neighbours x y matrix) 3) 1 
    ( > (count-living-neighbours x y matrix) 3) 0 
  )
  )

; Define a matrix for testing purposes.
(def test-matrix [ [0 1 0] [1 1 1] [1 0 0] ])

; Helper to visualize what's happening
(defn to-matrix-string [matrix]
  (let [pretty-print-form (apply str (map #(str % \newline) matrix))]
    pretty-print-form)
  )

; This function creates a square matrix of dimension size x size.
; An entry in the matrix is a vector [rowNumber colNumber].
;
; Thus for the moment, the matrix of the game of life needs to be a square :-(
;
(defn get-coords [size]
  (let 
    ; The indices is a list of numbers in the range
    [indices (range 0 size)
    ; row is a function that computes the vector of coordinates of the row
    row (fn [y col] (vec (map #(vector y %) col)))]
   (vec (map #(row % indices) (range 0 size))))
  )


; The main() of the application.
; To solve the game of life, we .....
; - generate a matrix where each cell (value) is replaced by [x y] vector, the
; application of the function get-coords to the matrix size
; - generate a second new matrix result matrix where each coordinate matrix [x y]
; is replaced the value if live-or-die? for the original matrix, x and y

(defn game-of-life [matrix]
  (let 
    [map-row (fn [col] 
              (vec  (map #(live-or-die? (first %) (last %) matrix) 
                 ((get-coords (count matrix)) col))))]
    (do
      (print "Input matrix" \newline)
      (print (to-matrix-string matrix))
      (print "Output matrix" \newline)
      (print (to-matrix-string (vec (map #(map-row %) (range 0 (count matrix))))))
      "Au revoir ..."     
   )
  )
)

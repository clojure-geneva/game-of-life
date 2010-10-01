; left as of at the end of first "Geneva clojurians meeting"
(def test-grid
	[
	[0 0 0 0 0]
	[0 0 1 0 0]
	[0 0 1 0 0]
	[0 0 1 0 0]
	[0 0 0 0 0] ])

(defn print-grid [grid]
	(doseq [line grid]
		(println line))
	)

(print-grid test-grid)

(defn cell-at [grid x y]
	((grid y) x)
	)

(cell-at test-grid 2 1)
(cell-at test-grid 3 1)

(defn around-and-self [grid x y]
	(let [
		begin-x (max (- x 1) 0) 
	 	end-x (min (+ x 1) (- (count (grid y)) 1)) 
		begin-y (max (- y 1) 0) 
	 	end-y (min (+ y 1) (- (count grid) 1))]
	
	(for 
		[i (range begin-x (+ 1 end-x))
		j (range begin-y (+ 1 end-y))
		]
	[ i j ] )))

(defn around [grid x y]
	(filter #(not (= [x y] %)) (around-and-self grid x y)))
		
(around test-grid 2 1)
;(around test-grid 0 0)
;(around test-grid 4 4)

(defn next [current neighbours]
	(cond
		(< neighbours 2) 0
		(= neighbours 2) current
		(= neighbours 3) 1
		(> neighbours 3) 0
	))

(defn count-alive [grid x y] 
	(reduce + 0 (map (fn [[x y]] (cell-at grid x y)) (around grid x y))))

(count-alive test-grid 0 0)
(count-alive test-grid 4 4)
(count-alive test-grid 2 2)

(defn vectorize [flat n]	
	)

(defn next-gen [grid]
	(partition (count (grid 0))
		(map (fn [[x y]] 
			(next ((grid y) x) (count-alive grid x y)))

				(for [j (range (count grid))
					i (range (count (grid j)))]			
				[i j] ))))


(print-grid test-grid)
(print-grid (next-gen test-grid))
;
;(defn count-alive-neighbours [grid x y]
;	(reduce + 0 (grid x y))
;	)
	

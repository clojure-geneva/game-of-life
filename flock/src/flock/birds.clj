;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; ant sim ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
;   which can be found in the file CPL.TXT at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns flock.birds
  (:require clojure.contrib.math))

                                        ;dimensions of square world
(def dim 80)
                                        ;number of ants = nbirds-sqrt^2
(def nbirds-sqrt 100)

(def animation-sleep-ms 50)
(def bird-sleep-ms 100)

(def number-of-birds 70)

(def running true)

(defstruct cell :empty) ;may also have :bird

(defn create-world
  [dim]
  (apply vector
         (map (fn [_]
                (apply vector (map (fn [_] (ref (struct cell 0)))
                                   (range dim))))
              (range dim))))


                                        ;world is a 2d vector of refs to cells
(def world
     (create-world dim))


(defn place
  ([loc]
     (place loc world))

  ([[x y] my-world]
     (-> my-world (nth x) (nth y))))


(defstruct bird :dir) ;may also have :food

(defn create-bird
  "create an ant at the location, returning an ant agent on the location"
  ([loc dir]
     (create-bird loc dir world))

  ([loc dir world]
     (sync nil
       (let [p (place loc world)
             a (struct bird dir)]
         (alter p assoc :bird a)
         (agent loc)))))



(def home-off (/ dim 4))
(def home-range (range home-off (+ nbirds-sqrt home-off)))

(defn setup
  []
  (sync nil
    ;; (doall
    ;;  (for [x (range 79) y (range 15)]
    ;;    (create-bird [x y] (rand-int 8))))
    (doall
     (for [x (range 0 79 2) y (range 60 70 2)]
       (create-bird [x y] (rand-int 8)))
     )))
;; (doall
;;  [(create-bird [10 10] 6)
;;   (create-bird [11 10] 6)
;;   (create-bird [12 10] 6)
;;   (create-bird [5 15] 0)
;;  ])
;; ))

;; (for [i (range number-of-birds)]
;;   (let [x 10 y 0]
;;     (do
;;       (if (= (mod i 2) 0)
;;         (create-bird [i y] 0)
;;         (create-bird [(- i 1) (+ y 70)] 4))))))))


(defn bound
  "returns n wrapped into range 0-b"
  [b n]
  (let [n (rem n b)]
    (if (neg? n)
      (+ n b)
      n)))

(defn wrand
  "given a vector of slice sizes, returns the index of a slice given a
  random spin of a roulette wheel with compartments proportional to
  slices."
  [slices]
  (let [total (reduce + slices)
        r (rand total)]
    (loop [i 0 sum 0]
      (if (< r (+ (slices i) sum))
        i
        (recur (inc i) (+ (slices i) sum))))))

                                        ;dirs are 0-7, starting at north and going clockwise
                                        ;these are the deltas in order to move one step in given dir
(def dir-delta {0 [0 -1]
                1 [1 -1]
                2 [1 0]
                3 [1 1]
                4 [0 1]
                5 [-1 1]
                6 [-1 0]
                7 [-1 -1]})

(def dir-delta-inv {[0 -1] 0
                    [1 -1] 1
                    [1 0] 2
                    [1 1] 3
                    [0 1] 4
                    [-1 1] 5
                    [-1 0] 6
                    [-1 -1] 7})

(defn delta-loc
  "returns the location one step in the given dir. Note the world is a torus"
  [[x y] dir]
  (let [[dx dy] (dir-delta (bound 8 dir))]
    [(bound dim (+ x dx)) (bound dim (+ y dy))]))

                                        ;(defmacro dosync [& body]
                                        ;  `(sync nil ~@body))

                                        ;ant agent functions
                                        ;an ant agent tracks the location of an ant, and controls the behavior of
                                        ;the ant at that location

(defn turn
  "turns the ant at the location by the given amount"
  [loc amt]
  (dosync
   (let [p (place loc)
         bird (:bird @p)]
     (alter p assoc :bird (assoc bird :dir (bound 8 (+ (:dir bird) amt))))))
  loc)

(defn reorient
  "change the dir of the bird at loc"
  [loc new-dir]
  (let [p (place loc)
        bird (:bird @p)]
    (alter p assoc :bird (assoc bird :dir new-dir)))
  loc)

(defn clear-ahead?
  [loc]
   (let [p (place loc)
         bird (:bird @p)
         dir (:dir bird)
         ahead-loc  (delta-loc loc (:dir bird))
         ahead-p (place ahead-loc)]
     (not (:bird @ahead-p))))

;; (defn avoid
;;   "turns the ant at the location by the given amount"
;;   [loc]
;;   (dosync
;;    (if (not (clear-ahead? loc))
;;      (turn loc 1))))

(defn dirs-in-view [dir]
  (map #(bound 8 %) (range (- dir 2) (+ dir 3))))

(defn locs-in-view [loc]
  (let [p (place loc)
        bird (:bird @p)
        dir (:dir bird)]
    (map #(delta-loc loc %) (dirs-in-view dir))))

(defn filter-birds [locs]
  (filter #(:bird @(place %)) locs))

(defn birds-dir-in-view [loc]
  "Returns the dirs of birds ahead of us"
  (map #(:dir (:bird @(place %))) (filter-birds (locs-in-view loc))))


(defn reduce-dirs
  [dirs]
  (reduce (fn [acc curr]
            (let [v (get acc curr 0)]
              (assoc acc curr (inc v))))
          {}
          dirs))

(defn most-common-dir
  [dirs]
  (when (seq dirs)
    (key (first (sort-by val > (reduce-dirs dirs))))))

;;     last key name Integer/parseInt))

(defn average-dir [dirs]
  "Takes a sequence of directions given as values between 0 and 7"
  (let [dir-coord (map #(dir-delta %) dirs)
        _size (count dirs)]
    (cond (= _size 0) nil
          :else (map / (reduce
                        (fn [acc val] [(+ (acc 0) (val 0)) (+ (acc 1) (val 1))])
                        [0 0]
                        dir-coord)
                     [_size _size]))))

(defn normalize-dir-coord [dir-coord]
  " "
  (map #(clojure.contrib.math/round %) dir-coord))


(defn move
  "moves the ant in the direction it is heading. Must be called in a
  transaction that has verified the way is clear"
  [loc]
  (let [oldp (place loc)
        bird (:bird @oldp)
        newloc (delta-loc loc (:dir bird))
        p (place newloc)
        ]
                                        ;move the ant
    (alter p assoc :bird bird)
    (alter oldp dissoc :bird)
    newloc))


(defn next-dir
  [dir]
  (let [rand-val (rand-int 1000)]
    (cond (= 4 rand-val) (bound 8 (inc dir))
          (= 3 rand-val) (bound 8 (dec dir))
          :else dir)))

(defn behave
  "the main function for the ant agent"
  [loc]
  (dosync
   (let [p (place loc)
         bird (:bird @p)
         candidate-dir (or (most-common-dir (birds-dir-in-view loc))
                           (next-dir (:dir bird)))]
     ;;(. Thread (sleep (+ 90 (rand-int 20)))) ;;bird-sleep-ms))
     (when running
       (send *agent* #'behave)
       )
     (if bird
       (do
         (reorient loc candidate-dir)
         (if (clear-ahead? loc)
           (move loc)
           (do
             ;;(println "ouch" loc)
             loc))
         ;;         (attempt-move loc)
         )
       loc))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; UI ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(import
 '(java.awt Color Graphics Dimension)
 '(java.awt.image BufferedImage)
 '(javax.swing JPanel JFrame))

                                        ;pixels per world cell
(def scale 5)

(defn fill-cell [#^Graphics g x y c]
  (doto g
    (.setColor c)
    (.fillRect (* x scale) (* y scale) scale scale)))

(defn render-bird [bird #^Graphics g x y]
  (let [black (. (new Color 0 0 0 255) (getRGB))
        gray (. (new Color 100 100 100 255) (getRGB))
        red (. (new Color 255 0 0 255) (getRGB))
        [hx hy tx ty] ({0 [2 0 2 4]
                        1 [4 0 0 4]
                        2 [4 2 0 2]
                        3 [4 4 0 0]
                        4 [2 4 2 0]
                        5 [0 4 4 0]
                        6 [0 2 4 2]
                        7 [0 0 4 4]}
                       (:dir bird))]
    (doto g
      (.setColor (if (:bird bird)
                   (new Color 255 0 0 255)
                   (new Color 0 0 0 255)))
      (.drawLine (+ hx (* x scale)) (+ hy (* y scale))
                 (+ tx (* x scale)) (+ ty (* y scale))))))

(defn render-place [g p x y]
                                        ;  (when (pos? (:pher p))
                                        ;    (fill-cell g x y (new Color 0 255 0
                                        ;                          (int (min 255 (* 255 (/ (:pher p) pher-scale)))))))
                                        ;  (when (pos? (:food p))
                                        ;    (fill-cell g x y (new Color 255 0 0
                                        ;                          (int (min 255 (* 255 (/ (:food p) food-scale)))))))
  (when (:bird p)
    (render-bird (:bird p) g x y)))


(defn render [g]
  (let [v (dosync (apply vector (for [x (range dim) y (range dim)]
                                  @(place [x y]))))
        img (new BufferedImage (* scale dim) (* scale dim)
                 (. BufferedImage TYPE_INT_ARGB))
        bg (. img (getGraphics))
        ]
    (->> v (filter :bird) count println)
    (doto bg
      (.setColor (. Color white))
      (.fillRect 0 0 (. img (getWidth)) (. img (getHeight))))
    (dorun
     (for [x (range dim) y (range dim)]
       (render-place bg (v (+ (* x dim) y)) x y)))
    (. g (drawImage img 0 0 nil))
    (. bg (dispose))))

(def panel (doto (proxy [JPanel] []
                   (paint [g] (render g)))
             (.setPreferredSize (new Dimension
                                     (* scale dim)
                                     (* scale dim)))))

(def frame (doto (new JFrame) (.add panel) .pack .show))

(def animator (agent nil))

(defn animation [x]
  (when running
    (send-off *agent* animation))
  (. panel (repaint))
  (. Thread (sleep animation-sleep-ms))
  nil)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; use ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
                                        ;demo
  (load-file "/Users/chrislain/dev/myClojureTests/flock/src/flock/birds.clj")
  (def birds (setup))
  (send-off animator animation)
  (dorun (map #(send-off % behave) birds))

  )

(defn -main [& args]
    (def birds (setup))
    (send-off animator animation)
    (dorun (map #(send % behave) birds)))


;; start -main
;; lein run -m flock.birds
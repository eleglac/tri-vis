(ns tri-vis.core
  (:require [delaunay-triangulation.core :refer [triangulate]] 
            [quil.core :as q]
            [quil.middleware :as m]))

(def fr       60)
(def hsb-max  100)
(def tri-size 40)

(defn intensity 
  "Given an input representing time as either frames (from frame-count) or 
  milliseconds (millis), generate a float representing a percentage of some 
  maximal value.  Currently the float is generated using
  a sin(x)^2 function but any continuous periodic function should work."
  [t]

  (let [duration 30000 ;remember to scale correctly, currently assumes ms
        scale    (/ duration q/PI)]
    (Math/pow (Math/sin (/ t scale)) 2)))

(defn skew 
  "Utility function to be used by define-color.  Takes a scaling factor
  and some number of coordinate points and returns the float representing
  the Perlin noise at that point.  Maybe not that useful but ehhh"
  
  ([] 0.0)
  ([scale] (* (q/noise 0) scale))
  ([scale & points] (apply q/noise (map #(* %1 scale) (take 3 points)))))

(defn define-color 
  "Given three points (which ostensibly represent the corners of a triangle),
  generate a color (hue, saturation, value, alpha). Set the fill to that color.
  This can be done in many ways."
  [[x1 y1] [x2 y2] [x3 y3]]
  
  (let [position-delta (skew 0.001 x1 x2 y3)
        time-delta     (intensity (q/millis))
        hue            (mod (+ (* time-delta hsb-max) (* position-delta hsb-max)) 100)
        saturation     (+ (* 80 position-delta) (* 20 time-delta)) 
        brightness     (+ (* 20 position-delta) (* 80 time-delta)) 
        alpha          hsb-max]
  (q/color hue saturation brightness alpha)))

(defn get-corners
  "Create a list of [x y] corresponding to the corners of equilateral 
  triangles which tile the visible window. n is one-half the base length of 
  a single triangle (in pixels), and in effect sets the scale of the tiling."
  [n]
  
  (let [offset-x (* 2 n) 
        offset-y (* n (Math/sqrt 3))

        cols (+ 3 (* 2 (/ (q/width) offset-x)))
        rows (Math/floor (/ (q/height) offset-y))]

    (for [i (range (inc cols)) 
          j (range (inc rows))]
      [(- (* i n) offset-x)                            
       (* (+ (* j 2) (mod i 2)) offset-y)])))

(defn draw-tri 
  "Sets fill color (using define-color to create a fill color based on
  the triangle points) and then draws a triangle."
  [[x1 y1] [x2 y2] [x3 y3]]

  (q/fill (define-color [x1 y1] [x2 y2] [x3 y3]))
  (q/triangle x1 y1 x2 y2 x3 y3))

(defn draw-tris 
  "Essentially just a wrapper for repeated calls to draw-tri."
  [tris]

  (doseq [[p1 p2 p3] (map identity (:triangles tris))] 
    (draw-tri p1 p2 p3)))

(defn setup 
  "Besides establishing basic environmental parameters (color-mode, frame-rate,
  and background), a list of the screen-tiling triangles is generated"
  []

  (q/color-mode :hsb hsb-max)
  (q/frame-rate 30) 
  (q/background hsb-max)
  (q/no-stroke))

(defn update-state
  "Currently does nothing but pass through current state, which is the list of 
  triangles as created in (setup).  
  update-state could potentially change the position of the triangle
  corners to allow for more interesting visual effects."
  [state]

;; In previous versions, all triangles were calculated in (setup).
;; However, since this means (draw) is delayed until the calcs are finished,
;; JOGL would often assume that the thread was blocked and throw an exception.
;; I suppose I could just catch the exception, but we'll try this first.
;; ref: https://github.com/processing/processing/issues/4468

  (if (empty? state)
    {:tris (triangulate (get-corners tri-size))}
    state))

(defn draw-state
  "Actually render the triangles!"
  [state]

  (q/background 100)
  (q/fill 0)

;; the following check stems from the fact that state will not be
;; initialized until after draw-state completes its first pass.
;; see note with (update-state) for more info about why this hacky
;; bullshit is here.

  (if (empty? state)
    (q/text "loading" (/ (q/width) 2) (/ (q/height) 2))
    (draw-tris (:tris state))))

(q/defsketch image-site
  :title      "Triangles"
  :setup      setup
  :update     update-state
  :draw       draw-state  
  :size       :fullscreen  ;error may be due to thread blocking????
  ;:size       [640 480]
  :renderer   :p2d
  :features   [:keep-on-top]
  :middleware [m/fun-mode]
  

  )

(defn -main [] nil)

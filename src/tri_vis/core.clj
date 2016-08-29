(ns tri-vis.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def fr         60)
(def hsb-max    100)
(def tri-base   27)
(def tri-height (* tri-base (Math/sqrt 3)))

(defn intensity 
  "Given an input representing time as either frames (from frame-count) or 
  milliseconds (millis), generate a float representing a percentage of some 
  maximal value.  Currently the float is generated using
  a sin(x)^2 function but any continuous function should work."
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
  
  (let [;; periodic change based on time elapsed
        time-delta-p   (intensity (q/millis))
        ;; continuously variable change based on time elapsed 
        time-delta-cv  (skew 0.004   (q/frame-count)) 
        ;; static change based on location of triangle
        position-delta (skew 0.00045 (+ x1 x2 x3) 
                                     (+ y1 y2 y3))
        ;; composite change, based both on position and time  
        cvt-pos-delta  (skew 0.00035 (q/frame-count) 
                                     (+ x1 x2 x3) 
                                     (+ y1 y2 y3))
        ;; actual parameter construction - keep between 0 and 100
        hue            (mod (+ (* hsb-max cvt-pos-delta) 
                               (* hsb-max time-delta-cv) 
                               (* hsb-max time-delta-p)) 100)
        saturation     (+ (* 80 position-delta) (* 20 time-delta-p)) 
        brightness     (+ (* 20 position-delta) (* 80 time-delta-p)) 
        alpha          hsb-max]

  (q/color hue saturation brightness alpha)))

(defn triangulate
  "Rolled my own triangulation function based on the assumption that I'll
  always want equilateral triangles.  For other kinds of triangle tilings,
  need more math (or Delaunay triangulation!)
  Note: f should be either (+) or (-) for best results."
  [f [x y]]

  [[x                       y] 
   [(+ x tri-base)          (f y tri-height)] 
   [(+ x tri-base tri-base) y]])

(defn get-corners
  "Create a list of [x y] corresponding to the corners of equilateral 
  triangles which tile the visible window. n is one-half the base length of 
  a single triangle (in pixels), and in effect sets the scale of the tiling."
  []
  
  (let [offset-x (* 2 tri-base) 
        offset-y tri-height

        cols (+ 3 (* 2 (/ (q/width) offset-x)))
        rows (Math/floor (/ (q/height) offset-y))]

    (for [i (range (inc cols)) 
          j (range (inc rows))]
      [(- (* i tri-base) offset-x)                            
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

  (doseq [[p1 p2 p3] (map identity tris)] 
    (draw-tri p1 p2 p3)))

(defn setup 
  "Establishes basic environmental parameters (color-mode, frame-rate,
  and background)."
  []

  (q/color-mode :hsb hsb-max)
  (q/frame-rate fr) 
  (q/background hsb-max)
  (q/no-stroke)

;; All triangles are calculated in (setup).
;; However, since this means (draw) is delayed until the calcs are finished,
;; JOGL might assume that the thread is blocked and throw an exception.
;; I suppose I could just catch the exception, but we'll try this first.
;; ref: https://github.com/processing/processing/issues/4468
  
  (let [corners (get-corners)
        up-tris (map (partial triangulate -) corners)
        dn-tris (map (partial triangulate +) corners)]
    {:tris (concat up-tris dn-tris)}))

(defn update-state
  "Pass-through.

  update-state could potentially change the position of the triangle
  corners to allow for more interesting visual effects."
  [state]

  state)

(defn draw-state
  "Actually render the triangles!"
  [state]

  (q/background 100)
  (draw-tris (:tris state)))

(q/defsketch image-site
  :title      "Triangles"
  :setup      setup
  :update     update-state
  :draw       draw-state  
  :size       :fullscreen  
  :renderer   :p2d
  :features   [:keep-on-top]
  :middleware [m/fun-mode])

(defn -main [] nil)

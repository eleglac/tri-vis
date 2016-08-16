(defproject tri-vis "0.1-alpha"

  :description "Tri-Vis: Inspired by a Macbook cover."

  :url "http://pontingdynamics.org/tri-vis"

  :license {:name "GNU GPL v3"
            :url "https://www.gnu.org/licenses/gpl-3.0.en.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [trystan/delaunay-triangulation "1.0.1"]
                 [quil "2.4.0"]]

  :main tri-vis.core )

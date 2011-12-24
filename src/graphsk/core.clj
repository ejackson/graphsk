(ns graphsk.core
  (use [clojure.core.match :only [match]]))

;; -----------------------------------------------------------------------------
;;  First we define the Protocol, which is much like an Interface from OOP-land

(defprotocol Chart
  "Anything that wants to call itself a chart must implement these functions"
  (valid? [chart])
  (docs   [chart])
  (render [chart])
  (foo    [chart])
  (bar    [chart]))

;; -----------------------------------------------------------------------------
;;  Now our actual Types.  This is only one of the possible syntaxes.
(defrecord BarChart [x y]
  Chart
  (render [chart] "I am a rendered Bar Chart, short and stout")
  (docs   [chart] "I am the documentation for a Bar Chart"))

(defrecord PieChart [x y]
  Chart
  (render [chart] "I am rendered PieChart.")
  (docs   [chart] "I am the documentation for a Bar Chart"))

;; -----------------------------------------------------------------------------
;;  JSON stuff
(defn- valid-json?
  "Put in your json validation here"
  [json-data]
  (and
   (string? json-data)
   (> (count json-data) 0)))

(defn- parse-json [json-data]
  {:pre [(valid-json? json-data)]}
  ;; Do you json stuff here.  I'm returning a dummy vector for x and y.
  [[1 2 3] [4 5 6]])

;; -----------------------------------------------------------------------------
;;  Interface to this stuff.  Factory Function.
(defn- create-chart
  "Passed in a map containing a key, :type, specifying the type of chart as well
   as other, type specific, keys.  This is the only 'hardwired' part, everything
   else is completely generic.  This might use some work to pull out the json from
   the construction of the Charts.  Dunno, unhappy."
  [{type :chart-type json-data :json-data :as chart}]
  (let [[x y] (parse-json json-data)]
       (match [type]
              [:bar-chart] (BarChart. x y)
              [:pie-chart] (PieChart. x y)
              [_]          (throw (Exception. "Unknown Chart Type")))))

;; -----------------------------------------------------------------------------
;;  Example of how you would use this.  You'd want to make this generic, so that
;;  everything except (render chart) is a template and you pass in (render chart)
;;  or (docs chart).  Perhaps a macro is good for this ?
(defn my-web-renderer [params]
  (try
    (let [chart (create-chart params)]
      (render chart))
    
    (catch Error e
      ;; Here you'd package up the message up for Compojure or whatewer
      (str "A Report that bad shit happened: " (.getMessage e)))
    (catch Exception e
      (str "Report that bad shit happened: " (.getMessage e)))))

(comment
  ;; Unknown Chart
  (my-web-renderer {:chart-type :pie-charto :json-data ":a 1 etc"})

  ;; Missing JSON
  (my-web-renderer {:chart-type :pie-chart})

  ;; Peachy
  (my-web-renderer {:chart-type :pie-chart :json-data ":y 1 :x 3"})
  (my-web-renderer {:chart-type :bar-chart :json-data ":y 1 :x 3"})

  )
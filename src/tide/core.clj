(ns tide.core
  (:require [clojure.core.matrix.stats :refer [mean sd]])
  (:import com.github.brandtg.stl.StlDecomposition))

(defn box-cox
  [lambda x]
  (if (zero? lambda)
    (Math/log x)
    (/ (dec (Math/pow x lambda)) lambda)))

(defn box-cox-inverse
  [lambda x]
  (if (zero? lambda)
    (Math/exp x)
    (Math/pow (inc (* lambda x)) (/ lambda))))

(defn guerrero
  ([xs]
   (guerrero 2 xs))
  ([length xs]
   (let [sections (partition-all length xs)
         cv (fn [lambda]
              (let [cvs (for [section sections]
                          (/ (sd section) 
                             (Math/pow (mean section) (- 1 lambda))))]
                (/ (sd cvs) 
                   (mean cvs))))]
     (apply min-key cv (range 0 1 0.1)))))

(def ^:private setters
  {:inner-loop-passes (memfn setNumberOfInnerLoopPasses n)
   :robustness-iterations (memfn setNumberOfRobustnessIterations n)
   :trend-bandwidth (memfn setTrendComponentBandwidth bw)
   :seasonal-bandwidth (memfn setSeasonalComponentBandwidth bw)
   :loess-robustness-iterations (memfn setLoessRobustnessIterations n)
   :periodic? (memfn setPeriodic periodic?)})

(defn decompose
  ([period ts]
   (decompose period {} ts))
  ([period opts ts]
   (let [xs (map first ts)
         ys (map second ts)                   
         preprocess (if-let [transform (:transform opts)]
                      (partial map transform)
                      identity)
         postprocess (if-let [transform (:reverse-transform opts)]
                       (partial map transform)
                       vec)
         decomposer (StlDecomposition. period)
         _ (reduce-kv (fn [config k v]
                        (when-let [setter (setters k)]
                          (setter config v))
                        config)
                      (.getConfig decomposer)
                      (merge {:inner-loop-passes 100}
                             opts))
         decomposition (.decompose decomposer xs (preprocess ys))]
     {:trend (postprocess (.getTrend decomposition))
      :seasonal (postprocess (.getSeasonal decomposition))
      :reminder (postprocess (.getRemainder decomposition))
      :xs xs
      :ys ys})))

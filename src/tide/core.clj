(ns tide.core
  (:require [clojure.core.matrix.stats :refer [mean sd]])
  (:import com.github.brandtg.stl.StlDecomposition
           com.fastdtw.dtw.FastDTW
           (com.fastdtw.timeseries TimeSeriesBase TimeSeriesPoint TimeSeriesItem)
           (com.fastdtw.util Distances DistanceFunction)))

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

(defn ensure-seq
  [x]
  (if (sequential? x)
    x
    [x]))

(defn- build-timeseries
  [ts]
  (if (sequential? (first ts))
    (.build (reduce (fn [builder [x y]]
                      (->> y
                           ensure-seq
                           double-array
                           TimeSeriesPoint.
                           (TimeSeriesItem. x)
                           (.add builder)))
                    (TimeSeriesBase/builder)
                    ts))
    (build-timeseries (map-indexed vector ts))))

(defn dtw
  ([ts1 ts2]
   (dtw {} ts1 ts2))
  ([opts ts1 ts2]
   (let [{:keys [distance search-radius]
          :or {distance Distances/EUCLIDEAN_DISTANCE
               search-radius 1}} opts   
         distance-fn (if (instance? DistanceFunction distance-fn)
                       distance-fn
                       (reify
                         DistanceFunction
                         (calcDistance [this a b]
                           (distance-fn a b))))
         tw (FastDTW/compare (build-timeseries ts1) (build-timeseries ts2)
                             search-radius distance-fn)
         path (.getPath tw)]
     {:path (for [i (range (.size path))]
              (let [cell (.get path i)]
                [(.getCol cell) (.getRow cell)]))
      :distance (.getDistance tw)})))

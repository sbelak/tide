(ns tide.dtw
  (:import com.fastdtw.dtw.FastDTW
           (com.fastdtw.timeseries TimeSeriesBase TimeSeriesPoint TimeSeriesItem)
           (com.fastdtw.util Distances DistanceFunction)))

(defn- ensure-seq
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
         distance (if (instance? DistanceFunction distance)
                    distance
                    (reify
                      DistanceFunction
                      (calcDistance [this a b]
                        (distance a b))))
         tw       (FastDTW/compare (build-timeseries ts1)
                                   (build-timeseries ts2)
                                   search-radius distance)
         path     (.getPath tw)]
     {:path     (for [i (range (.size path))]
                  (let [cell (.get path i)]
                    [(.getCol cell) (.getRow cell)]))
      :distance (.getDistance tw)})))

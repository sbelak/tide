(ns tide.core
  (:import com.github.brandtg.stl.StlDecomposition))

(def setters {:inner-loop-passes (memfn setNumberOfInnerLoopPasses n)
              :robustness-iterations (memfn setNumberOfRobustnessIterations n)
              :trend-bandwidth (memfn setTrendComponentBandwidth bw)
              :seasonal-bandwidth (memfn setSeasonalComponentBandwidth bw)
              :loess-robustness-iterations (memfn setLoessRobustnessIterations n)
              :periodic? (memfn setPeriodic p?)})

(defn decompose
  ([period ts]
   (decompose period {} ts))
  ([period opts ts]
   (let [xs (map first ts)
         ys (map second ts)
         preprocess (if (= (:decomposition opts) :multiplicative)
                      (partial map #(Math/log %))
                      identity)
         postprocess (if (= (:decomposition opts) :multiplicative)
                       (partial map #(Math/exp %))
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

;; TODO
;;
;; Box-Cox

(ns tide.core
  (:import com.github.brandtg.stl.StlDecomposition))

(defn decompose
  [period ts]
  (let [decomposition (.decompose (StlDecomposition. period)
                                  (map (comp keyfn first) ts)
                                  (map second ts))]
    {:trend (vec (.getTrend decomposition))
     :seasonal (vec (.getSeasonal decomposition))
     :reminder (vec (.getRemainder decomposition))}))

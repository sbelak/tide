(ns tide.core
  (:import com.github.brandtg.stl.StlDecomposition))

(defn decompose
  [period ts]
  (let [xs (map first ts)
        ys (map second ts) 
        decomposition (.decompose (StlDecomposition. period) xs ys)]
    {:trend (vec (.getTrend decomposition))
     :seasonal (vec (.getSeasonal decomposition))
     :reminder (vec (.getRemainder decomposition))
     :xs xs
     :ys ys}))

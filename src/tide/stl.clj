(ns tide.stl
  (:require [kixi.stats.core :as stats]
            [redux.core :as redux])
  (:import com.github.servicenow.ds.stats.stl.SeasonalTrendLoess$Builder))

(defn box-cox
  ([lambda]
   (partial box-cox lambda))
  ([lambda x]
   (if (zero? lambda)
     (Math/log x)
     (/ (dec (Math/pow x lambda)) lambda))))

(defn box-cox-inverse
  ([lambda]
   (partial box-cox-inverse lambda))
  ([lambda x]
   (if (zero? lambda)
     (Math/exp x)
     (Math/pow (inc (* lambda x)) (/ lambda)))))

(defn guerrero
  ([xs]
   (guerrero 2 xs))
  ([length xs]
   (let [sections (partition-all length xs)
         cv       (fn [lambda]
                    (let [cvs (for [section sections]
                                (transduce
                                 identity
                                 (redux/post-complete
                                  (redux/fuse {:sd   stats/standard-deviation
                                               :mean stats/mean})
                                  (fn [{:keys [sd mean]}]
                                    (/ sd (Math/pow mean (- 1 lambda)))))
                                 section))]
                      (transduce identity
                                 (redux/post-complete
                                  (redux/fuse {:sd   stats/standard-deviation
                                               :mean stats/mean})
                                  (fn [{:keys [sd mean]}]
                                    (/ sd mean)))
                                 cvs)))]
     (apply min-key cv (range 0 1 0.1)))))

(def ^:private setters
  {:seasonal-width (memfn setSeasonalWidth w)
   :seasonal-degree (memfn setSeasonalDegree d)
   :seasonal-jump (memfn setSeasonalJump j)
   :trend-width (memfn setTrendWidth w)
   :trend-degree (memfn setTrendDegree d)
   :trend-jump (memfn setTrendJump j)
   :lowpass-width (memfn setLowpassWidth w)
   :lowpass-degree (memfn setLowpassDegree w)
   :lowpass-jump (memfn setLowpassJump j)
   :inner-iterations (memfn setInnerIterations n)
   :robustness-iterations (memfn setRobustnessIterations n)
   :robust? (memfn setRobustFlag r?)
   :periodic? (fn [builder periodic?]
                (if periodic?
                  (.setPeriodic builder)
                  builder))
   :flat-trend? (fn [builder flat-trend?]
                  (if flat-trend?
                    (.setFlatTrend builder)
                    builder))
   :linear-trend (fn [builder linear-trend?]
                   (if linear-trend?
                     (.setLinearTrend builder)
                     builder))})

(defn decompose
  ([period ts]
   (decompose period {} ts))
  ([period opts ts]
   (let [ys            (map second ts)
         preprocess    (if-let [transform (:transform opts)]
                         (partial map transform)
                         identity)
         postprocess   (if-let [transform (:reverse-transform opts)]
                         (partial mapv transform)
                         vec)]
     (transduce identity
                (fn
                  ([]
                   (-> (SeasonalTrendLoess$Builder.)
                       (.setPeriodLength period)))
                  ([builder]
                   (let [decomposition (->> ys
                                            preprocess
                                            double-array
                                            (.buildSmoother builder)
                                            (.decompose))]
                     {:trend    (postprocess (.getTrend decomposition))
                      :seasonal (postprocess (.getSeasonal decomposition))
                      :residual (postprocess (.getResidual decomposition))
                      :xs       (map first ts)
                      :ys       ys}))
                  ([builder [k v]]
                   (if-let [setter (setters k)]
                     (setter builder v)
                     builder)))
                opts))))

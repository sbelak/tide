(defproject tide "0.3.0-SNAPSHOT"
  :description "Clojure library for seasonal-trend decomposition"
  :url "https://github.com/sbelak/tide"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.github.servicenow.stl4j/stl-decomp-4j "1.0.2"
                  :exclusions [org.apache.commons/commons-math3]]
                 [com.github.davidmoten/fastdtw "0.1"]
                 [kixi/stats "0.3.8"
                  :exclusions [org.clojure/test.check]]
                 [org.apache.commons/commons-math3 "3.6.1"]
                 [redux "0.1.4"] ])

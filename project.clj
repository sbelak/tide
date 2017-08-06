(defproject tide "0.2.0-SNAPSHOT"
  :description "Clojure library for seasonal-trend decomposition"
  :url "https://github.com/sbelak/tide"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [com.github.servicenow.stl4j/stl-decomp-4j "1.0.2"]
                 [com.github.davidmoten/fastdtw "0.1"]
                 [net.mikera/core.matrix "0.60.3"]])

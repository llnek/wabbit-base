;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defproject io.czlab/wabbit-base "1.0.0"

  :license {:url "http://www.eclipse.org/legal/epl-v10.html"
            :name "Eclipse Public License"}

  :description ""
  :url "https://github.com/llnek/wabbit-base"

  :dependencies [[org.apache.commons/commons-lang3 "3.5"]
                 [io.czlab/basal "1.0.0"]
                 [commons-io/commons-io "2.5"]]

  :plugins [[cider/cider-nrepl "0.14.0"]
            [lein-javadoc "0.3.0"]
            [lein-codox "0.10.3"]
            [lein-czlab "1.0.0"]
            [lein-cprint "1.2.0"]]
  :hooks [leiningen.lein-czlab]

  :profiles {:provided {:dependencies
                        [[org.clojure/clojure "1.8.0" :scope "provided"]
                         [net.mikera/cljunit "0.6.0" :scope "test"]
                         [junit/junit "4.12" :scope "test"]]}
             :run {:global-vars ^:replace {*warn-on-reflection* false}}
             :uberjar {:aot :all}}

  :javadoc-opts {:package-names ["czlab.wabbit"]
                 :output-dir "docs"}

  :global-vars {*warn-on-reflection* true}
  :target-path "out/%s"
  :aot :all

  :coordinate! "czlab/wabbit/base"
  :omit-source true

  :java-source-paths ["src/main/java" "src/test/java"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :resource-paths ["src/main/resources"]

  :jvm-opts ["-Dlog4j.configurationFile=file:attic/log4j2.xml"]
  :javac-options ["-source" "8"
                  "-Xlint:unchecked" "-Xlint:-options" "-Xlint:deprecation"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


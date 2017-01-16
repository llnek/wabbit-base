;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defproject io.czlab/wabbit-base "0.1.0"

  :license {:url "http://www.eclipse.org/legal/epl-v10.html"
            :name "Eclipse Public License"}

  :description ""
  :url "https://github.com/llnek/wabbit-base"

  :dependencies [[org.apache.commons/commons-lang3 "3.5"]
                 [io.czlab/xlib "0.1.0"]
                 [commons-io/commons-io "2.5"]]

  :plugins [[lein-codox "0.10.2"]
            [lein-czlab "0.1.1"]
            [lein-pprint "1.1.2"]]
  :hooks [leiningen.lein-czlab]

  :profiles {:provided {:dependencies
                        [[net.mikera/cljunit "0.6.0" :scope "test"]
                         [junit/junit "4.12" :scope "test"]
                         [org.clojure/clojure "1.8.0" :scope "provided"]
                         [codox/codox "0.10.2" :scope "provided"]]}
             :run {:global-vars ^:replace {*warn-on-reflection* false}}
             :uberjar {:aot :all}}

  :global-vars {*warn-on-reflection* true}
  :target-path "out/%s"
  :aot :all
  ;;:jar-exclusions [#"(?:^|/).svn/"]
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


;; Copyright (c) 2013-2017, Kenneth Leung. All rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns ^{:doc ""
      :author "Kenneth Leung"}

  czlab.wabbit.base

  (:require [czlab.basal.resources :refer [rstr]]
            [czlab.basal.logging :as log]
            [clojure.walk :as cw]
            [clojure.string :as cs]
            [clojure.java.io :as io])

  (:use [czlab.basal.format]
        [czlab.basal.core]
        [czlab.basal.meta]
        [czlab.basal.io]
        [czlab.basal.str])

  (:import [org.apache.commons.lang3.text StrSubstitutor]
           [org.apache.commons.io FileUtils]
           [czlab.jasal Muble I18N]
           [czlab.basal Cljrt]
           [java.io IOException File]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;(set! *warn-on-reflection* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^String c-verprops "czlab/wabbit/base/version.properties")
(def ^String c-rcb "czlab.wabbit.base/Resources")

;(def ^:private ^String sys-devid-pfx "system.####")
;(def ^:private ^String sys-devid-sfx "####")

;(def sys-devid-regex #"system::[0-9A-Za-z_\-\.]+" )
;(def shutdown-devid #"system::kill_9" )
;(def ^String pod-protocol  "pod:" )

(def ^String shutdown-uri "/kill9")
(def ^String dft-dbid "default")

(def ^String meta-inf  "META-INF" )
(def ^String web-inf  "WEB-INF" )

(def ^String dn-target "target")
(def ^String dn-build "build")

(def ^String dn-classes "classes" )
(def ^String dn-bin "bin" )
(def ^String dn-conf "conf" )
(def ^String dn-lib "lib" )

(def ^String dn-cfgapp "etc/app" )
(def ^String dn-cfgweb "etc/web" )
(def ^String dn-etc "etc" )

(def ^String en-rcprops  "Resources_en.properties" )
(def ^String c-rcprops  "Resources_%s.properties" )
(def ^String dn-templates  "templates" )

(def ^String dn-logs "logs" )
(def ^String dn-tmp "tmp" )
(def ^String dn-dbs "dbs" )
(def ^String dn-dist "dist" )
(def ^String dn-views  "htmls" )
(def ^String dn-pages  "pages" )
(def ^String dn-patch "patch" )
(def ^String dn-media "media" )
(def ^String dn-scripts "scripts" )
(def ^String dn-styles "styles" )
(def ^String dn-pub "public" )

(def ^String web-classes  (str web-inf  "/" dn-classes))
(def ^String web-lib  (str web-inf  "/" dn-lib))
(def ^String web-log  (str web-inf  "/logs"))
(def ^String web-xml  (str web-inf  "/web.xml"))

(def ^String mn-rnotes (str meta-inf "/" "RELEASE-NOTES.txt"))
(def ^String mn-readme (str meta-inf "/" "README.md"))
(def ^String mn-notes (str meta-inf "/" "NOTES.txt"))
(def ^String mn-lic (str meta-inf "/" "LICENSE.txt"))

(def ^String pod-cf  "pod.conf" )
(def ^String cfg-pod-cf  (str dn-conf "/" pod-cf))
(def ^String cfg-pub-pages  (str dn-pub "/" dn-pages))

;(def jslot-flatline :____flatline)
;(def jslot-last :____lastresult)

(def evt-opts :____eventoptions)
(def jslot-cred :credential)
(def jslot-user :principal)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defmacro gtid "typeid of component" [obj] `(.toString ~obj))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defmacro logcomp "Internal" [pfx co]
  `(log/info "%s: {%s}#<%s>" ~pfx (gtid ~co) (.id ~co)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn getProcDir "" ^File []
  (io/file (System/getProperty "wabbit.user.dir")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn expandSysProps
  "Expand system properties found in value"
  ^String [^String value]
  (if (nichts? value) value (StrSubstitutor/replaceSystemProperties value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn expandEnvVars
  "Expand env vars found in value"
  ^String [^String value]
  (if (nichts? value) value (-> (System/getenv)
                                StrSubstitutor. (.replace value))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn expandVars
  "Replaces all variables in value"
  ^String
  [^String value]
  (if (or (nichts? value)
          (< (.indexOf value "${") 0))
    value
    (-> (cs/replace value "${pod.dir}" "${wabbit.user.dir}")
        expandSysProps expandEnvVars)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn expandVarsInForm "" [form]
  (cw/postwalk #(if (string? %) (expandVars %) %) form))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn readConf
  "Parse a edn configuration file" {:tag String}

  ([podDir confile]
   (readConf (io/file podDir dn-conf confile)))

  ([file]
   (doto->>
     (-> (io/file file)
         (changeContent #(expandVars %)))
     (log/debug "[%s]\n%s" file))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;asserts that the directory is readable & writable.
(defn ^:no-doc precondDir
  "Assert dir(s) are read-writeable?" [f & dirs]
  (do->true
    (doseq [d (cons f dirs)]
      (test-cond (rstr (I18N/base)
                       "dir.no.rw" d) (dirReadWrite? d)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;asserts that the file is readable
(defn ^:no-doc precondFile
  "Assert file(s) are readable?" [ff & files]
  (do->true
    (doseq [f (cons ff files)]
      (test-cond (rstr (I18N/base)
                       "file.no.r" f) (fileRead? f)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn ^:no-doc maybeDir
  "If the key maps to a File"
  ^File
  [^Muble m kn]
  (let [v (.getv m kn)]
    (condp instance? v
      String (io/file v)
      File v
      (trap! IOException (rstr (I18N/base)
                               "wabbit.no.dir" kn)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn expandXXXConf
  "" [cfgObj] (-> (writeEdnStr cfgObj) expandVars readEdn ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn slurpXXXConf
  "Parse config file"
  ([podDir conf]
   (slurpXXXConf podDir conf false))
  ([podDir conf expVars?]
   (let [f (io/file podDir conf)
         s (str "{\n" (slurpUtf8 f) "\n}")]
     (->
       (if expVars?
         (-> (cs/replace s
                         "${pod.dir}"
                         "${wabbit.user.dir}")
             expandVars)
         s)
       readEdn ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn spitXXXConf
  "Write out config file"
  [podDir conf cfgObj]
  (let [f (io/file podDir conf)
        s (strim (writeEdnStr cfgObj))]
    (->>
      (if (wrapped? s "{" "}")
        (-> (drophead s 1)
            (droptail 1))
        s)
      (spitUtf8 f))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn varit
  "From this name, find the var" [^String n]

  (let-try
    [clj (Cljrt/newrt (getCldr) "x")]
    (if (hgl? n) (.varIt clj n))
    (finally (.close clj))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn prevarCfg ""

  ([cfg] (prevarCfg cfg :handler))
  ([cfg kee]
   (if-some+ [n (strKW (get cfg kee))]
     (let [v (varit n)]
       (assoc cfg kee v))
     cfg)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn deleteDir "" [dir]
  (try! (FileUtils/deleteDirectory (io/file dir))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn cleanDir "" [dir]
  (try! (FileUtils/cleanDirectory (io/file dir))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF


(ns com.latacora.just-never-do-it
  (:require [insn.core :as insn] [clojure.string :as str])
  (:import
   (java.lang.instrument
    Instrumentation ClassFileTransformer UnmodifiableClassException))
  (:gen-class
   :methods
   [^:static [agentmain [String java.lang.instrument.Instrumentation] void]
    ^:static [premain [String java.lang.instrument.Instrumentation] void]]))

(def base "org.apache.logging.log4j.core")
(def JndiLookup (str base ".lookup.JndiLookup"))
(def AbstractLookup (str base ".lookup.AbstractLookup"))
(def LogEvent (str base ".LogEvent"))

(def zero-vulns-bytecode
  (-> {:name JndiLookup
       :super AbstractLookup
       :methods (for [desc [[LogEvent String String] [String String]]]
                  {:name :lookup
                   :desc desc
                   :emit [[:ldc "JNDI stands for just never do it"]
                          [:areturn]]})}
      insn/visit
      :bytes))

(def transformer
  (let [replace {(str/replace JndiLookup "." "/") zero-vulns-bytecode}]
    (reify ClassFileTransformer
      (transform [_ _ cls-name _ _ _] (replace cls-name)))))

(defn -premain
  [^String args ^Instrumentation inst]
  (println "adding just-never-do-it transformer")
  (.addTransformer inst transformer true))

(defn -agentmain
  [^String args ^Instrumentation inst]
  (-premain nil inst)
  (loop [[c & cs] (.getAllLoadedClasses inst)]
    (if (= (.getName c) JndiLookup)
      (try
        (.retransformClasses inst c)
        (catch UnmodifiableClassException e (println e)))
      (recur cs))))

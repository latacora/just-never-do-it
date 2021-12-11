(ns com.latacora.just-never-do-it
  (:require [insn.core :as insn] [clojure.string :as str])
  (:import (java.lang.instrument ClassFileTransformer UnmodifiableClassException))
  (:gen-class))

(def base "org.apache.logging.log4j.core")
(def JndiLookup (str base ".lookup.JndiLookup"))
(def AbstractLookup (str base "lookup.AbstractLookup"))
(def LogEvent (str base "LogEvent"))

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

(defn -agentmain
  [_ inst]
  (let [replace {(str/replace JndiLookup "." "/") zero-vulns-bytecode}
        xf (reify ClassFileTransformer
             (transform [_ _ cls-name _ _ _] (replace cls-name)))]
    (.addTransformer inst xf true)
    (doseq [c (.getAllLoadedClasses inst)
            :when (= (.getName c) JndiLookup)]
      (try
        (.retransformClasses inst c)
        (catch UnmodifiableClassException e
          (println e))))
    (.removeTransformer xf)))
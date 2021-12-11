(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.string :as str]))

(def lib 'com.latacora/just-never-do-it)
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/%s.jar" (name lib)))

(defn clean
  [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (clean nil)
  (b/compile-clj {:basis basis :src-dirs ["src"] :class-dir class-dir})
  (let [agent-class (-> lib str (str/replace "/" ".") (str/replace "-" "_"))]
    (b/jar
     {:class-dir class-dir
      :jar-file jar-file
      :basis basis
      :manifest {"Premain-Class" agent-class
                 "Agent-Class" agent-class
                 "Can-Redefine-Classes" "true"
                 "Can-Retransform-Classes" "true"}})))

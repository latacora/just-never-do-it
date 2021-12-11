(ns com.latacora.inject
  (:import
   (com.sun.tools.attach VirtualMachine)
   (sun.jvmstat.monitor MonitoredHost)
   (java.io IOException File))
  (:gen-class))

(defn -main
  [jar-path & _]
  (let [host (MonitoredHost/getMonitoredHost "local://localhost")]
    (println host)
    (doseq [pid (.activeVms host)]
      (println "vaccinating" pid)
      (try
        (-> pid str VirtualMachine/attach (.loadAgent jar-path))
        (catch IOException e
          (println "failed; maybe this is this process itself?"))))))

#_(-> "target/just-never-do-it.jar" File. .getCanonicalPath -main)

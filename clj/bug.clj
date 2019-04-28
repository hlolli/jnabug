(ns bug
  (:require [clojure.core.async :as async])
  (:import [com.kunstmusik.csoundjna Csound MessageCallback])
  (:gen-class))

(defn csound-create []
  (new Csound))

(defn set-message-callback [^Csound instance callback]
  (let [msg-cb ^MessageCallback
        (reify MessageCallback
          (invoke [this inst
                   attr msg]
            (callback attr msg)))]
    (.setMessageCallback instance msg-cb)))

(defn set-option [^Csound instance ^String option]
  (.setOption instance option))

(defn start [^Csound instance]
  (.start instance))

(defn stop [^Csound instance]
  (.stop instance))

(defn cleanup [^Csound instance]
  (.cleanup instance))

(defn perform-ksmps [^Csound instance]
  (.performKsmps instance))

(defn input-message-async [^Csound instance ^String sco]
  (.inputMessageAsync instance sco))

(defn compile-orc [^Csound instance ^String orc]
  (.compileOrc instance orc))

(defn spawn-csound-client
  []
  (let [csnd   (atom (csound-create))
        thread (agent nil)]
    (run! #(set-option @csnd %)
          ["-iadc:null" "-odac:null"
           "--messagelevel=35" ;; 35
           "-B 4096"
           "-b 512"
           "--0dbfs=1"
           "--sample-rate=48000"])
    (start @csnd)
    (set-message-callback @csnd (fn [attr msg] (print msg)))
    {:instance csnd
     :start    #(send-off thread
                          (fn [& r]
                            (while (zero? (perform-ksmps @csnd)))
                            (doto @csnd cleanup)))
     :stop     #(doto @csnd stop)
     :send (fn [msg] (input-message-async @csnd msg))
     :compile (fn [orc] (compile-orc @csnd orc))}))

(defn -main [& args]
  (let [client-interface (spawn-csound-client)]
    ((:start client-interface))
    (async/go-loop []
      ((:compile client-interface) "prints \"csound heartbeat\n\"")
      (async/timeout (* 1 1000))
      (recur))
    (async/<!! (async/timeout (* 60 60 1000)))))

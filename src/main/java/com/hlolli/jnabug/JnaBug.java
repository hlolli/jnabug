package com.hlolli.jnabug;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.Agent;
import clojure.lang.Atom;
import com.kunstmusik.csoundjna.Csound;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class JnaBug {

    public static void main(String[] args) {
        // Clojure functions
        IFn agent = Clojure.var("clojure.core", "agent");
        IFn atom = Clojure.var("clojure.core", "agent");
        IFn fn = Clojure.var("clojure.core", "fn");
        IFn cljget = Clojure.var("clojure.core", "get");
        IFn cljwhile = Clojure.var("clojure.core", "while");
        IFn vector = Clojure.var("clojure.core", "vector");
        IFn deref = Clojure.var("clojure.core", "deref");
        IFn send_off = Clojure.var("clojure.core", "send-off");
        IFn hash_map = Clojure.var("clojure.core", "hash-map");
        // Thread via agent
        Object thread = agent.invoke(null);
        // csound interface
        Object csound_interface = hash_map.invoke("instance", atom.invoke(new Csound()));
        ((Csound) deref.invoke(cljget.invoke(csound_interface, "instance", null))).setOption("-odac:null");
        ((Csound) deref.invoke(cljget.invoke(csound_interface, "instance", null)))
            .compileOrc("sr=48000\nksmps=64\nnchnls=2\n0dbfs=1\n"
                        + "instr 1\n"
                        + "prints \"Csound Started!\" \n"
                        + "endin\n"
                        + "instr 2\n"
                        + "prints \"csound heartbeat....\" \n"
                        + "endin\n"
                        + "event_i(\"i\",1,0,3600000)\n"
                        );
        ((Csound) deref.invoke(cljget.invoke(csound_interface, "instance", null)))
            .setMessageCallback((cs,attr,msg) -> {
                    System.out.print(">> " + msg);
            });
        ((Csound) deref.invoke(cljget.invoke(csound_interface, "instance", null))).start();
        ScheduledExecutorService newScheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
        newScheduledThreadPool.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    new Csound();
                    ((Csound) deref.invoke(cljget.invoke(csound_interface, "instance", null))).compileOrc("prints \"csound heartbeat....\\n\"");
                }
            }, 0L, 5L, TimeUnit.SECONDS);

        send_off.invoke(thread,
                        fn.invoke(null, null, vector.invoke(), null,
                                  cljwhile.invoke(true, true, ((Csound) deref.invoke(cljget.invoke(csound_interface, "instance", null))).performKsmps())));

    }

}

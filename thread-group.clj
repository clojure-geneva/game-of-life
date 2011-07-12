(ns thread-group)
 
 (import '(java.util.concurrent Executors))

 (def *pool* (Executors/newFixedThreadPool
(+ 2 (.availableProcessors (Runtime/getRuntime)))))
 
(defn dothreads! [f & {thread-count :threads
exec-count :times
:or {thread-count 1 exec-count 1}}]
(dotimes [t thread-count]
(.submit *pool* #(dotimes [_ exec-count] (f)))))


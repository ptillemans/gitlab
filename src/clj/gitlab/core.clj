(ns gitlab.core
  [:require
   [clj-ssh.ssh :as ssh]
   [clojure.spec.alpha :as s]]
  (:require [clojure.string :as str]))

(s/def ::host-chain (s/coll-of string?))
(s/def ::config (s/keys :req [::host-chain]))
(s/def ::command string?)

(defn run-command
  [config command]
  {:pre [(s/valid? ::command command)
         (s/valid? ::config config)]}
  (let [jump-hosts (vec (map #(do {:hostname %}) (::host-chain config)))
        s (ssh/jump-session
           (ssh/ssh-agent {})
           jump-hosts
           {})]
    (ssh/with-connection s
      (ssh/ssh-exec (ssh/the-session s) command "" "" {}))))

(defn with-command-runner
  "Creates a  context to chain multiple remote commands in a session."
  [config f]
  (let [s (ssh/jump-session
           (ssh/ssh-agent {})
           (vec (map #(do {:hostname %}) (::host-chain config)))
           {})]
    (ssh/with-connection s
      (f #(ssh/ssh-exec (ssh/the-session s) % "" "" {})))))


(def PS_COMMAND "ps --no-header -axo pid,ppid,state,user,pcpu,time,rss,size,vsize,start_time,wchan:32,cmd")

(defn list_processes
  [cmd]
  (->> (cmd PS_COMMAND)
       (:out)
       (str/split-lines)
       (map #(str/split % #" +" 13))
       (map #(zipmap [:pid :ppid :state :user :pcpu :time :rss :size :vsize :start-time :wchan :cmd] %))))

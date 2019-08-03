(ns gitlab.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [gitlab.core-test]))

(doo-tests 'gitlab.core-test)

(ns flux-challenge-reframe.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [flux-challenge-reframe.slots-test]))

(doo-tests 'flux-challenge-reframe.slots-test)

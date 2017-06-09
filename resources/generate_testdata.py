# Copyright 2015 The TensorFlow Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================
"""Generate some standard test data for debugging TensorBoard.
"""

from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import bisect
import math
import os
import os.path
import random
import shutil

import numpy as np

# Hardcode a start time and reseed so script always generates the same data.
_start_time = 0
random.seed(0)


def _MakeHistogramBuckets():
  v = 1E-12
  buckets = []
  neg_buckets = []
  while v < 1E20:
    buckets.append(v)
    neg_buckets.append(-v)
    v *= 1.1
  # Should include DBL_MAX, but won't bother for test data.
  return neg_buckets[::-1] + [0] + buckets


def _MakeHistogram(values):
  """Convert values into a histogram proto using logic from histogram.cc."""
  limits = _MakeHistogramBuckets()
  counts = [0] * len(limits)
  for v in values:
    idx = bisect.bisect_left(limits, v)
    counts[idx] += 1

  limit_counts = [(limits[i], counts[i]) for i in xrange(len(limits))
                  if counts[i]]
  bucket_limit = [lc[0] for lc in limit_counts]
  bucket = [lc[1] for lc in limit_counts]
  sum_sq = sum(v * v for v in values)
  return sum_sq, bucket_limit, bucket

#data = [random.normalvariate(0,20) for _ in xrange(20)]
#print( _MakeHistogram(data))
a=[1E-9,1E-3,1,2,3,10,11,12,2000,1E10]
#a=range(2,12)

#for v in a:
#  print(bisect.bisect_left(a,(v + 0))," \n")
print(" ",_MakeHistogram(a))

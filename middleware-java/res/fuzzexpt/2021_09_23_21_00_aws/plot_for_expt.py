#!/usr/bin/env python3
import matplotlib.pyplot as plt
from matplotlib.path import Path
import matplotlib.patches as patches
import numpy as np
from parallel_results import parallel_coordinate_plot

parallel_coordinate_plot('finalPopulationNonDom.res', 'Non-dominated elements in final population', 'fuzzing_pop_multitopic.pdf');

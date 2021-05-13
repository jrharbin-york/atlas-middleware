#!/usr/bin/env python3
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

from matplotlib.ticker import MaxNLocator

def load_file_expt1(filename):
    df = pd.read_csv(filename, header=None, skiprows=0)
    df.columns = ['ModelName', 'CIName', 'MissedDetections', 'SweepTimeComplete', 'Empty']
    standard = df[df['CIName'] == "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_standard"]
    advanced = df[df['CIName'] == "atlascollectiveint.expt.casestudy1.ComputerCIshoreside_advanced"]
    return { 'standard' : standard, 'advanced' : advanced }

def load_file_expt2(filename):
    df = pd.read_csv(filename, header=None, skiprows=0)
    df.columns = ['ModelName', 'CIName', 'ObstacleAvoidance', 'InterrobotAvoidance', 'TotalEnergyAtEnd', 'MeanEnergyAtEnd', 'TotalFinalDistanceAtEnd', 'MeanFinalDistanceAtEnd', 'TotalSweepSwitchCount', 'Empty']
    standard = df[df['CIName'] == "atlascollectiveint.expt.casestudy2.ComputerCIshoreside_standard"]
    energytracking = df[df['CIName'] == "atlascollectiveint.expt.casestudy2.ComputerCIshoreside_energytracking"]
    return { 'standard' : standard, 'energytracking' : energytracking }

def plot_hist(ax, data, col, xlabel, ylabel, max_x, colour):
    ax.hist(data[col], color = colour)
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.set_xlim([-0.5, max_x+0.5])
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))

def plot_integer_hist(ax, data, col, xlabel, ylabel, max_x, colour):
    labels, counts = np.unique(data[col], return_counts=True)
    ax.bar(labels, counts, align='center', color = colour)
    ax.set_xticks(labels)
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.set_xlim([-0.5, max_x+0.5])
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))

def plot_expt1_both_ci_combined(expt1_data, filename_pdf):
    fig, ax = plt.subplots(nrows=2, ncols=1)
    plt.suptitle("Missed detection count for both collective intelligences");
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])
    plot_integer_hist(ax[0], expt1_data['standard'], 'MissedDetections', 'Missed detection count for standard CI', 'Frequency', 7, 'green')
    plot_integer_hist(ax[1], expt1_data['advanced'], 'MissedDetections', 'Missed detection count for advanced CI', 'Frequency', 7, 'blue')
    plt.savefig(filename_pdf)

def plot_expt1_standard_only(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    plot_integer_hist(ax, expt1_data['standard'], 'MissedDetections', 'Missed detection count', 'Frequency', 7, 'green')
    plt.title("Missed detection count for standard collective intelligence");
    plt.savefig(filename_pdf)

def plot_expt1_scatter_plot(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    std = expt1_data['standard']
    adv = expt1_data['advanced']
    ax.plot(std['MissedDetections'], std['SweepTimeComplete'], 'gx', adv['MissedDetections'], adv['SweepTimeComplete'], 'bo')
    ax.set_xlabel("Missed detections");
    ax.set_ylabel("Sweep completion time");
    plt.legend(["Standard CI", "Advanced CI"]);
    plt.title("Missed detections vs sweep completion timing for case study 1");
    plt.savefig(filename_pdf)
    
def plot_expt1_scatter_plot_standardonly(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    std = expt1_data['standard']
    ax.plot(std['MissedDetections'], std['SweepTimeComplete'], 'gx')
    ax.set_xlabel("Missed detections");
    ax.set_ylabel("Sweep completion time");
    plt.title("Missed detections vs sweep completion timing for case study 1");
    plt.savefig(filename_pdf)

def plot_expt2_standardonly(expt2_data, filename_base_pdf):
    ""
    

def plot_expt2(expt2_data, filename_base_pdf):
    fig, ax = plt.subplots(nrows=2, ncols=1)
    plt.suptitle("Number of completed sweeps for both collective intelligences");
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])
    plot_integer_hist(ax[0], expt2_data['standard'], 'TotalSweepSwitchCount', 'Number of completed sweeps for standard CI', 'Frequency', 7, 'green')
    plot_integer_hist(ax[1], expt2_data['energytracking'], 'TotalSweepSwitchCount', 'Number of completed sweeps for energy tracking CI', 'Frequency', 7, 'red')
    plt.savefig(filename_base_pdf + "_completed_sweeps.pdf");

    fig, ax = plt.subplots(nrows=2, ncols=1)
    plt.suptitle("Total distance at end for both collective intelligences");
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])
    plot_hist(ax[0], expt2_data['standard'],       'TotalFinalDistanceAtEnd',  'Total final distance at end for standard CI', 'Frequency', 300, 'green')
    plot_hist(ax[1], expt2_data['energytracking'], 'TotalFinalDistanceAtEnd',  'Total final distance at end for energy tracking CI', 'Frequency', 300, 'red')
    plt.savefig(filename_base_pdf + "_final_distance.pdf");

    fig, ax = plt.subplots(nrows=2, ncols=1)
    plt.suptitle("Total energy at end for both collective intelligences");
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])
    plot_hist(ax[0], expt2_data['standard'], 'TotalEnergyAtEnd', 'Total energy at end for standard CI', 'Frequency', 2500, 'green')
    plot_hist(ax[1], expt2_data['energytracking'], 'TotalEnergyAtEnd', 'Total energy at end for energy tracking CI', 'Frequency', 2500, 'red')
    plt.savefig(filename_base_pdf + "_final_energy.pdf"); 
    

def plot_all_expt1():
    # Expt 1 all configurations
    expt1_allconfigs = load_file_expt1("ciexpt-casestudy1.res")
    plot_expt1_standard_only(expt1_allconfigs, "casestudy1_missed_detections_standardonly.pdf")
    plot_expt1_both_ci_combined(expt1_allconfigs, "casestudy1_missed_detections.pdf")
    plot_expt1_scatter_plot(expt1_allconfigs, 'casestudy1_missed_vs_timing.pdf');
    plot_expt1_scatter_plot_standardonly(expt1_allconfigs, 'casestudy1_missed_vs_timing_standardonly.pdf');
    # Expt 1 optimal repeated
    expt1_optimal = load_file_expt1("ciexpt-casestudy1-optimal-repeated.res")
    plot_expt1_standard_only(expt1_optimal, "casestudy1_optimal_repeated_missed_detections_standardonly.pdf")
    plot_expt1_both_ci_combined(expt1_optimal, "casestudy1_optimal_repeated_missed_detections.pdf")
    plot_expt1_scatter_plot(expt1_optimal, 'casestudy1_optimal_repeated_missed_vs_timing.pdf');
    plot_expt1_scatter_plot_standardonly(expt1_allconfigs, 'casestudy1_optimal_repeated_missed_vs_timing_standardonly.pdf');

def plot_all_expt2():
    expt2_allconfigs = load_file_expt2("ciexpt-casestudy2.res")
    plot_expt2(expt2_allconfigs, "casestudy2")

plot_all_expt1()
plot_all_expt2()

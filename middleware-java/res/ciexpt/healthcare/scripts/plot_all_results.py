#!/usr/bin/env python3
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

from mpl_toolkits import mplot3d
from matplotlib.ticker import MaxNLocator

max_rooms = 12
TIME_OFFSET = 115

def load_file_expt1(filename):
    df = pd.read_csv(filename, header=None, skiprows=0)
    df.columns = ['ModelName','CompletedRooms','TotalEnergyRemaining','CompletionTimeWorstCase','CIName']
    standard = df[df['CIName'] == "atlascollectiveint.expt.healthcare.ComputerCIshoreside_healthcare"]
    energytracking = df[df['CIName'] == "atlascollectiveint.expt.healthcare.ComputerCIshoreside_healthcare_dynamicenergy"]
    return { 'standard' : standard, 'energytracking' : energytracking }

#def plot_hist(ax, data, col, xlabel, ylabel, max_x, colour):
#    ax.hist(data[col], color = colour)
#    ax.set_xlabel(xlabel)
#    ax.set_ylabel(ylabel)
#    ax.set_xlim([-0.5, max_x+0.5])
#    ax.yaxis.set_major_locator(MaxNLocator(integer=True))

def plot_integer_hist(ax, data, col, xlabel, ylabel, max_x, colour):
    ndata = pd.to_numeric(data[col])
    min_d = min(ndata)
    labels, counts = np.unique(ndata, return_counts=True)
    print(labels)
    print(counts)
    ax.bar(labels, counts, align='center', color = colour)
    ax.set_xticks(labels)
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.set_xlim([min_d-0.5, max_x+0.5])
    ax.yaxis.set_major_locator(MaxNLocator(integer=True))

def plot_expt1_both_ci_combined(expt1_data, filename_pdf):
    fig, ax = plt.subplots(nrows=2, ncols=1)
    plt.suptitle("Completed room count for both collective intelligences");
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])
    plot_integer_hist(ax[1], expt1_data['energytracking'], 'CompletedRooms', 'Completed rooms count for energy-tracking CI', 'Frequency', max_rooms, 'blue')
    plot_integer_hist(ax[0], expt1_data['standard'], 'CompletedRooms', 'Completed rooms count for standard CI', 'Frequency', max_rooms, 'green')
    plt.savefig(filename_pdf)

def plot_expt1_standard_only(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    plot_integer_hist(ax, expt1_data['standard'], 'CompletedRooms', 'Completed rooms count for standard CI', 'Frequency', max_rooms, 'green')
    plt.title("Completed room count for standard collective intelligence");
    plt.savefig(filename_pdf)

# plot example: https://colab.research.google.com/drive/1PQ6ZRQ9WWbu3-DfzE9Nq8IM6vAMfFfml?usp=sharing
    
def plot_expt1_scatter_plot(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    std = expt1_data['standard']
    adv = expt1_data['energytracking']
    ax.plot(std['CompletedRooms'], std['TotalEnergyRemaining'], 'gx', adv['CompletedRooms'], adv['TotalEnergyRemaining'], 'bo', markerfacecolor="none")
    ax.set_xlabel("Completed Rooms");
    ax.set_ylabel("Total energy remaining on robots at end");
    plt.legend(["Standard CI", "Energy Tracking CI"]);
    plt.title("Completed rooms and total energy remaining at the end");
    plt.savefig(filename_pdf)

def plot_expt1_scatter_plot_3d(expt1_data, filename_pdf):
    ax = plt.axes(projection="3d")
    std = expt1_data['standard']
    adv = expt1_data['energytracking']
    std_time_corrected = std['CompletionTimeWorstCase'] - TIME_OFFSET
    adv_time_corrected = adv['CompletionTimeWorstCase'] - TIME_OFFSET
    ax.plot3D(std['CompletedRooms'], std['TotalEnergyRemaining'], std_time_corrected, 'gx')
    ax.plot3D(adv['CompletedRooms'], adv['TotalEnergyRemaining'], adv_time_corrected, 'bo', markerfacecolor="none")
    ax.set_xlabel("Completed Rooms");
    ax.set_ylabel("Total energy remaining on robots at end");
    plt.legend(["Standard CI", "Energy Tracking CI"]);
    plt.title("Completed rooms and total energy remaining at the end");
    plt.savefig(filename_pdf)
    
def plot_expt1_scatter_plot_standardonly(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    std = expt1_data['standard']
    print(std['CompletedRooms'])
    print(std['TotalEnergyRemaining'])
    
    ax.plot(std['CompletedRooms'], std['TotalEnergyRemaining'], 'gx')
    ax.set_xlabel("Completed Rooms");
    ax.set_ylabel("Total energy remaining on robots at end");
    plt.title("Completed rooms and total energy remaining at the end");
    plt.savefig(filename_pdf)

def plot_expt1_scatter_plot_standardonly_3d(expt1_data, filename_pdf):
    ax = plt.axes(projection="3d")
    std = expt1_data['standard']
    print(std['CompletedRooms'])
    print(std['TotalEnergyRemaining'])
    std_time_corrected = std['CompletionTimeWorstCase'] - TIME_OFFSET
    ax.plot(std['CompletedRooms'], std['TotalEnergyRemaining'], std_time_corrected, 'gx')
    ax.set_xlabel("Completed Rooms");
    ax.set_ylabel("Total energy remaining on robots at end");
    plt.title("Completed rooms and total energy remaining at the end");
    plt.savefig(filename_pdf)

def plot_repeated_results_energy_finishtime(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    std = expt1_data['standard']
    adv = expt1_data['energytracking']
    print(std['TotalEnergyRemaining'])
    print(std['CompletionTimeWorstCase'])

    std_time_corrected = std['CompletionTimeWorstCase'] - TIME_OFFSET
    adv_time_corrected = adv['CompletionTimeWorstCase'] - TIME_OFFSET
    
    ax.plot(std['TotalEnergyRemaining'], std_time_corrected, 'gx', adv['TotalEnergyRemaining'], adv_time_corrected, 'bo')
    ax.set_xlabel("Total energy at end");
    ax.set_ylabel("Completion time for the worst case of all robots");
    plt.title("Energy reminaing on all robots \n versus worst case completion time of all robots over multiple runs of the same model", wrap=True)
    plt.savefig(filename_pdf)

def plot_repeated_results_rooms_energy(expt1_data, filename_pdf):
    fig, ax = plt.subplots()
    std = expt1_data['standard']
    adv = expt1_data['energytracking']
    print(std['CompletedRooms'])
    print(std['TotalEnergyRemaining'])

    ax.plot(std['CompletedRooms'], std['TotalEnergyRemaining'], 'gx', adv['CompletedRooms'], adv['TotalEnergyRemaining'], 'bo')
    ax.set_xlabel("Completed Rooms");
    ax.set_ylabel("Total energy remaing on all robots");
    plt.title("Completed rooms versus the total energy remaining on the robots over multiple runs of the same model", wrap=True)
    plt.savefig(filename_pdf)  

def plot_all_healthcare():
    # Healthcare all configurations
    expt1_allconfigs = load_file_expt1("../results_chosen/ciexpt-casestudy-healthcare.res")
    plot_expt1_standard_only(expt1_allconfigs, "casestudy_healthcare_rooms_standardonly.pdf")
    plot_expt1_both_ci_combined(expt1_allconfigs, "casestudy_healthcare_rooms.pdf")
    plot_expt1_scatter_plot(expt1_allconfigs, 'casestudy_healthcare_rooms_vs_energy.pdf')
    plot_expt1_scatter_plot_3d(expt1_allconfigs, 'casestudy_healthcare_rooms_vs_energy_3d.pdf')
    plot_expt1_scatter_plot_standardonly(expt1_allconfigs, 'casestudy_healthcare_rooms_vs_energy_standardonly.pdf')
    plot_expt1_scatter_plot_standardonly_3d(expt1_allconfigs, 'casestudy_healthcare_rooms_vs_energy_standardonly_3d.pdf')

def plot_repeated_healthcare():
    expt1_optimal = load_file_expt1("../results_chosen/ciexpt-casestudy-repeated-optimal.res")
    expt1_worst = load_file_expt1("../results_chosen/ciexpt-casestudy-repeated-worst.res")
    plot_repeated_results_energy_finishtime(expt1_optimal, "casestudy_repeated_optimal.pdf")
    plot_repeated_results_rooms_energy(expt1_worst, "casestudy_repeated_worst.pdf")
    
plot_all_healthcare()
plot_repeated_healthcare()

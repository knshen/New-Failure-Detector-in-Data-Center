import matplotlib.pyplot as plt
import numpy as np
from pylab import *

colors = ['k', 'w', 'r', 'c', 'b', 'm', 'g', 'y']
types = ['-', '/', '|', '\\', '.', '+', '*']


def readData(path):
    data = []
    file = open(path, 'r')

    for line in file.readlines():
        data.append(float(line))
    return data


# no crashes, having message loss
def draw_query(names):
    loss_rate = readData('z://plotData//crashes.txt')
    baseline = readData('z://plotData//baseline-c-q.txt')
    datak2 = readData('z://plotData//k2-c-q.txt')
    datak3 = readData('z://plotData//k3-c-q.txt')
    datak5 = readData('z://plotData//k5-c-q.txt')

    p1, = plt.plot(loss_rate, baseline, 'k.-', linewidth=0.8)  # baseline
    p2, = plt.plot(loss_rate, datak2, 'b.--', linewidth=0.8)  # k=2
    p3, = plt.plot(loss_rate, datak3, 'r.-.', linewidth=0.8)  # k=3
    p4, = plt.plot(loss_rate, datak5, 'g.:', linewidth=0.8)  # k=5

    plt.xlabel('Number of Crash Servers', fontsize=7)  # make axis labels
    plt.ylabel('Query Accuracy Probability', fontsize=7)

    # plt.xlim(0, 0.05)
    # plt.ylim(0.1, 1)

    leg = plt.legend((p1, p2, p3, p4), (names[0], names[1], names[2], names[3]), loc='upper left', prop={'size': 5})
    leg.draggable(state=True)

    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)
    plt.show()


def draw_rate(names):
    loss_rate = readData('z://plotData//crashes.txt')
    baseline = readData('z://plotData//baseline-c-r.txt')
    datak2 = readData('z://plotData//k2-c-r.txt')
    datak3 = readData('z://plotData//k3-c-r.txt')
    datak5 = readData('z://plotData//k5-c-r.txt')

    p1, = plt.plot(loss_rate, baseline, 'k.-', linewidth=0.8)  # baseline
    p2, = plt.plot(loss_rate, datak2, 'b.--', linewidth=0.8)  # k=2
    p3, = plt.plot(loss_rate, datak3, 'r.-.', linewidth=0.8)  # k=3
    p4, = plt.plot(loss_rate, datak5, 'g.:', linewidth=0.8)  # k=5

    plt.xlabel('Number of Crash Servers', fontsize=7)  # make axis labels
    plt.ylabel('Average Mistake Rate', fontsize=7)

    # plt.xlim(0, 0.05)
    # plt.ylim(0.1, 1)

    leg = plt.legend((p1, p2, p3, p4), (names[0], names[1], names[2], names[3]), loc='upper left', prop={'size': 5})
    leg.draggable(state=True)

    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)
    plt.show()


def draw_time(names):
    loss_rate = readData('z://plotData//crashes.txt')
    baseline = readData('z://plotData//baseline-c-t.txt')
    datak2 = readData('z://plotData//k2-c-t.txt')
    datak3 = readData('z://plotData//k3-c-t.txt')
    datak5 = readData('z://plotData//k5-c-t.txt')

    p1, = plt.plot(loss_rate, baseline, 'k.-', linewidth=0.8)  # baseline
    p2, = plt.plot(loss_rate, datak2, 'b.--', linewidth=0.8)  # k=2
    p3, = plt.plot(loss_rate, datak3, 'r.-.', linewidth=0.8)  # k=3
    p4, = plt.plot(loss_rate, datak5, 'g.:', linewidth=0.8)  # k=5

    plt.xlabel('Number of Crash Servers', fontsize=7)  # make axis labels
    plt.ylabel('Average Detection Time (s)', fontsize=7)

    plt.xlim(1, 60)
    # plt.ylim(0.1, 1)

    leg = plt.legend((p1, p2, p3, p4), (names[0], names[1], names[2], names[3]), loc='upper left', prop={'size': 5})
    leg.draggable(state=True)

    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)
    plt.show()


def draw_no(names):
    loss_rate = readData('z://plotData//crashes.txt')
    baseline = readData('z://plotData//baseline-c-no.txt')
    datak2 = readData('z://plotData//k2-c-no.txt')
    datak3 = readData('z://plotData//k3-c-no.txt')
    datak5 = readData('z://plotData//k5-c-no.txt')

    p1, = plt.plot(loss_rate, baseline, 'k.-', linewidth=0.8)  # baseline
    p2, = plt.plot(loss_rate, datak2, 'b.--', linewidth=0.8)  # k=2
    p3, = plt.plot(loss_rate, datak3, 'r.-.', linewidth=0.8)  # k=3
    p4, = plt.plot(loss_rate, datak5, 'g.:', linewidth=0.8)  # k=5

    plt.xlabel('Number of Crash Servers', fontsize=7)  # make axis labels
    plt.ylabel('Number of Undetected Crashes', fontsize=7)

    # plt.xlim(0, 0.05)
    plt.ylim(1, 40)

    leg = plt.legend((p1, p2, p3, p4), (names[0], names[1], names[2], names[3]), loc='upper left', prop={'size': 5})
    leg.draggable(state=True)

    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)
    plt.show()

####################################################

def draw_pre(labels, data):
    bar_width = 0.2
    size = len(data)
    index = np.arange(4)

    for i in range(size):
        plt.bar(index + i * bar_width, data[i], bar_width, alpha=1, color=colors[i], label=labels[i])

    plt.xlabel('Size of Link Failure Group', fontsize=7)
    plt.ylabel('Precision@k', fontsize=7)

    plt.xticks(index + bar_width / 1, ('size = 1', 'size = 2', 'size = 3', 'size = 4'))
    plt.ylim(0, 1.2)
    leg = plt.legend(prop={'size': 5})
    leg.draggable(state=True)
    # plt.tight_layout()
    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)

    plt.show()


def draw_mrr(labels, data):
    bar_width = 0.2
    size = len(data)
    index = np.arange(4)

    for i in range(size):
        plt.bar(index + i * bar_width, data[i], bar_width, alpha=1, color=colors[i], label=labels[i])

    plt.xlabel('Size of Link Failure Group', fontsize=7)
    plt.ylabel('MRR', fontsize=7)

    plt.xticks(index + bar_width / 1, ('size = 1', 'size = 2', 'size = 3', 'size = 4'))
    plt.ylim(0, 1.2)
    leg = plt.legend(prop={'size': 5})
    leg.draggable(state=True)
    # plt.tight_layout()
    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)

    plt.show()


def draw_ds(labels, data):
    bar_width = 0.2
    size = len(data)
    index = np.arange(4)

    for i in range(size):
        plt.bar(index + i * bar_width, data[i], bar_width, alpha=1, color=colors[i], label=labels[i])

    plt.xlabel('Size of Link Failure Group', fontsize=7)
    plt.ylabel('Discriminative Significance', fontsize=7)

    plt.xticks(index + bar_width / 1, ('size = 1', 'size = 2', 'size = 3', 'size = 4'))
    plt.ylim(0, 8)
    leg = plt.legend(prop={'size': 5})
    leg.draggable(state=True)
    # plt.tight_layout()
    plt.tick_params(axis='x', labelsize=5)
    plt.tick_params(axis='y', labelsize=5)

    plt.show()

names = ['Hierarchical Architecture',
         'K = 2',
         'K = 3',
         'K = 5']

# draw_time(names)
# draw_query(names)

###########################
labels = ['Herodotou et al.',
         'BPNN Based']

data_precision = [[0.8, 0.3, 0.6667, 0.6],
        [1, 1, 0.7333, 0.75]]

data_mrr = [[0.8667, 0.7167, 1, 0.9],
            [1,1,1,1]]

data_ds = [[5.4978, 6.2113, 6.1604, 6.2652],
           [0.1933, 1.0314, 0.7975, 1.4277]]

#draw_pre(labels, data_precision)
draw_mrr(labels, data_mrr)
#draw_ds(labels, data_ds)
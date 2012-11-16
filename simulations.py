#!/usr/bin/env python
import random
import networkx as nx
from numpy import random as nprand

class Individual(object):
    # infections = []
    # friends = []
    def __init__(self, infections=None, friends=None):
        self.infections = infections or set([])
        self.friends = friends or set([])

    @property
    def report(self):
        return ', '.join(infection.name for infection in self.infections)

# def infectiousness():
#     # samples from a distribution, returning a value between 0 and 1
#     # describes the infectiousness of a disease or probability of an idea being adopted
#     return random.random()

class Infection(object):
    # name = ''
    # infectiousness = 1.0
    def __init__(self, name, infectiousness=1.0):
        """
        An infectiousness of 1.0 means anyone who encounters it will be infected
        An infectiousness of 0.0 means it's not contagious
        """
        self.name = name
        self.infectiousness = infectiousness

def main():
    N = 10
    edges = N*2

    population = set([])
    for i in range(N):
        population.add(Individual())

    # randomly link up some of them
    for i in range(edges):
        a, b = random.sample(population, 2)
        a.friends.add(b)
        b.friends.add(a)

    # randomly infect one of them
    start, = random.sample(population, 1)
    infection = Infection('flu', 0.1)
    start.infections.add(infection)

    days = 100
    for day in range(days):

        for individual in population:
            for infection in individual.infections:
                for friend in individual.friends:
                    if random.random() < infection.infectiousness:
                        friend.infections.add(infection)

        print 'Day', day, \
            'report', ' '.join('%d:%s' % (i, individual.report) for i, individual in enumerate(population))


def blank():
G = nx.Graph()
G.add_nodes_from(['a', 'b', 'c', 'd', 'r'])
G.add_nodes_from([
    ('a', 'c'),
    ('b', 'c'),
    ('d', 'c'),
    ('d', 'r'),
    ('a', 'r'),
])
print(G.nodes())
print(G.edges())
nx.draw(G)

# main()
blank()

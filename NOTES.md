# 2012-10-30

Steps of the iteration:

1. Initialization
2. Network: N
3. Network type: T
4. Set of propositions: S
5. Priors: S -> P
6. Termination rule: R (e.g. 100 iterations)
7. Broadcast process: B
8. Initial transmission rule: I (Big Bang) -- e.g. pick 3 from S

# 2012-11-06

## Closed form density graph at timestamp t (from Joey)

- t is the time
- V is the number of people (vertices)
- J is the number of people infected at t = 0
- c is the threshold of number of friends who tell you a message, for you to believe it.
  - in the disease contagion model, this is just 1
- Y is the number of friends who
- p_i is the probability that your infected friend i will infect you.

Pr(bel_t) = \frac{J_{t-1}}{V} + Pois(\Sum{p_o}) Beta(a, b) * Pois(\lambda_{friends})(1 - \frac{J_{n-1}}{V})

J_t = V * Pr(bel_t)

Trying to figure out what it would mean to have both infectiousness of a disease, and probability of a person giving it to you. Or are those two different things?

- f(x) \prop f(x|q_i,p_i)f(q_i)f(p_i)

Can they be folded into one variable? Factor out q_i as \frac{1}{c}?



---

This assumes that if you know something:

1. you send it to all of your friends each iteration
2. you never forget it
3. that something is believed to be 100% true

Also assumes that I have the same probability of contracting a disease if I have 100 diseased friends with 1% contagiousness (reliability) as if I have 10 diseased friends with 10% contagiousness.

# Concerns:

- If my friend David tells me something every day for the last three years,
  I'm more likely to believe it than if he told me for the first time today
  (in general). Related related to parameter c above.

## Goals:

Show differences between belief contagion and disease contagion models.

Can assume that each person is either completely clean or completely infected.
Need parameters for each disease, though.

Infectiousness = probability of transmission.

## Measurements

1. Number of days required to reach some predefined threshold (of density of infection, say)
   - and at each day, how many were infected (graph with density on the Y-axis, Day on the X-axis)
   - also try to keep track of distribution with respect to connectivity of each individual

2. Number of infected

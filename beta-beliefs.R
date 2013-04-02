xs = seq(0, 1, 0.01)
# xs correspond to the prior probability of the situation

cf = function (reliabilities) {
  function (p_situation) {
    prod_x = 1
    for (i in 1:length(reliabilities)) {
      reliability = reliabilities[i]
      pH1r = p_situation * (1 - reliability)
      prod_x = prod_x * (pH1r / (reliability + pH1r))
    }
    p_situation / (p_situation + ((1 - p_situation) * prod_x))
  }
}

#ys = cf(2, 0.5)(xs)
test = function (N, a, b) {
  reliabilities = rbeta(N, a, b)
  plot(xs, cf(reliabilities)(xs), ylim=c(0.5, 1), type='l')
  reliabilities
}

#par(mar=c(5, 4, 4, 2) + 0.1)
par(mar=c(3, 4, 2, 1) + 0.1)

gray = rgb(0,0,0,10,maxColorValue=100)

run = function (trials, N, a, b) {
  par(mfrow=c(2,1))
  plot(xs, xs, ylim=c(0.5, 1), type='n')
  all = c()
  for (i in 1:trials) {
    reliabilities = rbeta(N, a, b)
    lines(xs, cf(reliabilities)(xs), col=gray)
    all = rbind(all, reliabilities)
  }
  hist(all)
}
run(1000, 3, 2, 5)

plot(xs, dbeta(xs, 0.2, 0.2), type='l', xmin=0)

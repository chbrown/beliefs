function pairSort(pair) {
  return (pair[0] < pair[1]) ? pair : [pair[1], pair[0]];
}

// give this a list of numbers (in a constant order)
function sampleFromPartitioning(partitions, sum) {
  var top = 0, rand = Math.random() * sum, i;
  for (i = 0; i < partitions.length; i++) {
    top += partitions[i];
    if (rand < top) {
      return i;
    }
  }
  return partitions.length - 1;
}

// WattsStrogatz
// assert(0 <= beta && beta <= 1, '0 ≤ β ≤ 1 violated!');
// BarabasiAlbert
// assert(m0 >= 2, 'm0 ≥ 2 violated!');
// assert(m >= 1, 'm ≥ 1 violated!');

function Graph(nodes, edges) {
  // nodes is always a list of integers
  this.nodes = nodes;
  // edges is always a list of pairs of integers
  this.edges = edges;
}

Graph.generators = {
  'watts-strogatz': function(opts) {
    var N = opts.N;
    var K = opts.K;
    var beta = opts.beta;
    // console.log('Generating Watts Strogatz small world graph', [N, K, Math.log(N), 1].join(' >> '), '| β =', beta);
    var nodes = _.range(N);
    var edgedict = {};

    // create ring lattice
    for (var a = 0; a < N; a++) {
      for (var j = 0; j < (K / 2); j++) {
        var b = (a + j + 1) % N;
        edgedict[pairSort([a, b])] = 1;
      }
    }

    var rewire = function(edge) {
      // a < b, guaranteed
      var a = edge[0], b = edge[1];
      // remove the a->b link
      delete edgedict[edge];
      // find a new edge to make, that doesn't already exist
      //   give up after 100 tries
      for (var i = 0, new_b, new_edge; i < 100; i++) {
        new_b = Math.random() * N | 0;
        new_edge = pairSort([a, new_b]);
        if (edgedict[new_edge] === undefined && a !== new_b) {
          edgedict[new_edge] = 1;
          break;
        }
      }
    };

    // do rewirings
    for (var i = 0; i < N; i++) {
      for (var j = 0; j < (K / 2); j++) {
        var k = (i + j + 1) % N;
        if (Math.random() < beta) {
          // rewire! i.e. replace k with some other node on the graph that i is not already connected to.
          rewire(pairSort([i, k]));
        }
      }
    }

    // edges is undirected, and edges[x] = [a, b] will have a < b for all x
    var edges = Object.keys(edgedict).map(function(edge_string) {
      return edge_string.split(',').map(function(part) {
        return parseInt(part, 10);
      });
    });

    return new Graph(nodes, edges);
  },
  'barabasi-albert': function(opts) {
    var N = opts.N;
    var m0 = opts.m0;
    var m = opts.m;

    var degrees = _.range(N).map(function() { return 0; });
    var degreesFor = function(x) { return degrees[x]; };
    var edge_dict = {};

    var degree_sum = 0;
    // make a complete network of all the nodes in the initial m0 network
    // xxx: add parameter for initial degree?
    for (var i = 0; i < m0; i++) {
      // candidates for friendship are all the nodes that have come before me.
      var candidates = _.range(i); // a list of indices (names) of nodes
      for (var c = 0; c < m && candidates.length; c++) {
        // partitions is a list of nodes describing
        var partitions = candidates.map(degreesFor);
        var j = sampleFromPartitioning(partitions, degree_sum);
        candidates.splice(j, 1);

        edge_dict[[i, j]] = 1;
        degrees[i]++;
        degrees[j]++;
        degree_sum += 2;
      }
    }

    for (var i = m0; i < N; i++) {
      // i is the new node
      var candidates = _.range(i);
      for (var c = 0; c < m && candidates.length; c++) {
        var partitions = candidates.map(degreesFor);
        var j = sampleFromPartitioning(partitions, degree_sum);
        candidates.splice(j, 1);

        edge_dict[[i, j]] = 1;
        degrees[i]++;
        degrees[j]++;
        degree_sum += 2;
      }
    }

    // edges is undirected, and edges[x] = [a, b] will have a < b for all x
    var edges = Object.keys(edge_dict).map(function(edge_string) {
      return edge_string.split(',').map(function(part) {
        return parseInt(part, 10);
      });
    });
    return new Graph(_.range(N), edges);
  }
};

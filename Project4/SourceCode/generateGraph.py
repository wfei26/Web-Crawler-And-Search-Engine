import networkx as nx
import nose

G = nx.read_edgelist("./result.txt", create_using=nx.DiGraph());

pr = nx.pagerank(G, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight', dangling=None);

f = open("./pagerank.txt", "w");
for key, value in pr.iteritems():
    newkey = "/Users/weifei/Dropbox/USC/2018Fall/CSCI572/HWs/HW4/data/nypost/" + key;
    f.write(newkey+'='+str(value)+"\n");

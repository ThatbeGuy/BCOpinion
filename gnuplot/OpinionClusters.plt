set terminal png
set output 'gnuplot/OpinionClusters_graph.png'
set xlabel "Sigma"
set ylabel "#Opinion Clusters, #Opinion Clusters per group"
#set title "Pade approximation" 0.000000,0.000000  ""
#set xrange [ 0 : 2 ] noreverse nowriteback
#set yrange [ 0 : 1 ] noreverse nowriteback
#set xtics border mirror norotate 1
#set ytics border mirror norotate 0.5
plot "OpinionClusters_final.txt" using 1:2 title "Opinion Clusters by Population" with lines, \
     "OpinionClusters_final.txt" using 1:3 title "Opinion Clusters by Group" with lines
#    EOF
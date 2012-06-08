set terminal png
set output 'gnuplot/NCRR_graph.png'
#set multiplot
set xlabel "Sigma"
set ylabel "Fraction of Non-Consensus Realizations Per Nodes N"
#set title "Pade approximation" 0.000000,0.000000  ""
set xrange [ 0 : 1 ] 
set yrange [ 0 : 1 ]
#set xtics border mirror norotate 1
#set ytics border mirror norotate 0.5
#plot "NCRR_N100_final.txt" using 1:2 title "N=100" with lines
#plot "NCRR_N200_final.txt" using 1:3 title "N=200" with lines
#plot "NCRR_N500_final.txt" using 1:4 title "N=500" with lines
plot "NCRR_N5000_final.txt" using 1:4 title "N=5000" with lines
#    EOF
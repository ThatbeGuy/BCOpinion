set terminal png
set output 'gnuplot/OCGroupDist_graph.png'
set xlabel "Sigma"
set ylabel "Opinion"
#set title "Pade approximation" 0.000000,0.000000  ""
set xrange [ 0 : 1 ]
#set yrange [ 0 : 1 ] noreverse nowriteback
#set zrange [ 0 : .7 ]
#set xtics border mirror norotate 1
#set ytics border mirror norotate 0.5
set view map
set palette defined (0 "white", 1 "black", 10 "red")
#set pointsize 3.5
set title "Opinion Clusters by Group"
splot "OCGroupDist_final.txt" using 1:2:3 with points pointtype 5 palette
#    EOF
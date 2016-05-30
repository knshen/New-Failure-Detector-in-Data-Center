for shname in `ls *.pcap`
do 
	#echo $shname
        name=`echo "$shname" | awk -F. '{print $1}'`       
	tcpdump -nn -tt -r $shname > /home/knshen/dump/$name	    
          
done

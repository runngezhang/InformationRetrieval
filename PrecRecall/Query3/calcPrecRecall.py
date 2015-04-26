#!/usr/bin/python

#Little python script that takes in the CSV files corresponding to 
#	search queries with the hyperparameter configurations that we evaluated
#Author(s): Vik Kamath
#	  : Antti Partanen
#In compliance with the requirements for the second assignment of 
#	the Information Retrieval Course at Aalto

from __future__ import division
import os
import sys
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

plotTitles = ['SA, NSW, VSM',
	      'SA, NSW, BM25',
	      'SA, SW, VSM',
	      'SA, SW, BM25',
              'SA, Stem, NSW, VSM',
              'SA, Stem, NSW, BM25', 
              'SA, Stem, SW, VSM',
              'SA, Stem, SW, BM25']

f, axarr = plt.subplots(4,2, sharex=True, sharey=True)
f.tight_layout()
row = 0 #Row index for subplots
col = 0 #Column index for subplots 
index = 0 #Index into the title array
#font = {'family' : 'normal',
#        'size'   : 12}
#matplotlib.rc('font', **font)



def compute_precRec(data, n_relevant):
    '''
    Computes the Precision and the Recall of the data
    for each point in the data. Returns an array 
    of size n_datapoints x 2 

    :param n_relevant: number of relevant documents in the corpus
                    for particular search task
    :type n_relevant: int
    '''
    precRec = np.empty(data.shape, dtype=float) #Create an empty array of same 
                                            #size as the data
    retrieved = 0 #Number of points we've retrieved so far
    relevant = 0 #Number of points retrieved that are relevant so far
    for datapoint in data:
        retrieved = retrieved + 1
        if int(datapoint[1]) == 1:
            relevant = relevant + 1
        #Precision
        precRec[retrieved - 1,0] = relevant/retrieved
        #Recall
        precRec[retrieved - 1,1] = relevant/n_relevant
#    print retrieved,relevant,data.shape[0]
    precision = relevant/retrieved
    recall = relevant/n_relevant
    print "Retrieved: ",retrieved
    print "Relevant: " ,relevant
    print "Precision: ", precision
    print "Recall: ", recall
    print "F-Score: ", (2*precision*recall)/(precision + recall)
    print "-----------------------"
    return precRec
            
def compute_means():
    pass

def plot_precRec(data):
    """
    Plot the precision Recall Curve
    """ 
    global col
    global row
    global index
    #Plot the data to the subplot
    print row, col, index
    axarr[row, col].plot(data[:,1], data[:,0])
    a = plotTitles[index]
    axarr[row, col].set_title(a)
    axarr[row, col].title.set_fontsize(12)
    #Some really naive index manipulation
    if col == 1:
	col = 0
	row = row + 1
    else:
        col = 1 #Next column
    index = index + 1 #The next element in the title array
    

def getData(n_relevant):
    '''
    Function that gets the data from the file and puts it
    in a numpy array

    :param n_relevant: number of relevant documents in the corpus
                    for particular search task
    :type n_relevant: int
    '''

    #Sorted - as a hack, along with a numbered
    # 		naming scheme for the files to get 
    #	    universally ordered plots (across all three queries)
    for filename in sorted(os.listdir(os.getcwd())):
        config, extension = os.path.splitext(filename)
        #Open the file, put it in a numpy array
        if extension == ".csv":
            #I only want to open csv files
            #I'm doing this via this convoluted logic because
            # some of the 'titles' contain commas and cannot be 
            # parsed without throwing up an error by a standard CSV lib
            data = []
            f = open(filename)
            print filename
            for line in f:
                line = line.split(',')
                data.append([line[0], line[-1]]) #Only append the 'score' 
                                                #the relevance of the result.
	        npdata = np.array(data, dtype=float) #Return a numpy array
	        
            ####
            #Compute the precision and the recall of the data
            ####
            #Compute the precision and recall of the data
            precRec = compute_precRec(npdata, n_relevant) 
            plot_precRec(precRec)
        
        
        
        

            
        

def main():
    #The number of documents that are relevant to our search task
    # viz. Video Tagging is 38. 
    getData(38)
    plt.setp([a.get_xticklabels() for a in axarr[0, :]], visible=False)
    plt.setp([a.get_xticklabels() for a in axarr[1, :]], visible=False)
    plt.setp([a.get_xticklabels() for a in axarr[2, :]], visible=False)
    plt.setp([a.get_yticklabels() for a in axarr[:, 1]], visible=False)
    f.text(0.5,0.995,'Query 3',horizontalalignment='center',
       verticalalignment='top',weight='bold')
    f.text(0.5, 0.015, 'Recall', ha='center', va='center',weight='bold')
    f.text(0.98, 0.5, 'Precision', ha='center', va='center', rotation=270,weight='bold')
    f.savefig('plot.png')
	


if __name__ == "__main__":
	main()



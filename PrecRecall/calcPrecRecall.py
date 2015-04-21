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
    print retrieved,relevant,data.shape[0]
    return precRec
            
def compute_means():
    pass

def plot_precRec(data):
    """
    Plot the precision Recall Curve
    """ 
    fig = plt.figure()
    plt.plot(data[:,1],data[:,0])
#    plt.plot(range(data.shape[0]),data[:,1])
    fig.savefig('plot.png')
    

def getData(n_relevant):
    '''
    Function that gets the data from the file and puts it
    in a numpy array

    :param n_relevant: number of relevant documents in the corpus
                    for particular search task
    :type n_relevant: int
    '''
    for filename in os.listdir(os.getcwd()):
        config, extension = os.path.splitext(filename)

        #Open the file, put it in a numpy array
        if extension == ".csv":
            #I only want to open csv files
            #I'm doing this via this convoluted logic because
            # some of the 'titles' contain commas and cannot be 
            # parsed without throwing up an error by a standard CSV lib
            data = []
            f = open(filename)
            for line in f:
                line = line.split(',')
                data.append([line[0], line[-1]]) #Only append the 'score' 
                                                #the relevance of the result.
        data = np.array(data, dtype=float) #Return a numpy array
        
        ####
        #Compute the precision and the recall of the data
        ####
        #Compute the precision and recall of the data
        precRec = compute_precRec(data, n_relevant) 
        print precRec 
        plot_precRec(precRec)
        break #TODO: Remove this line before production
        
        
        
        

            
        

def main():
    #The number of documents that are relevant to our search task
    # viz. Video Tagging is 38. 
    getData(38)
	


if __name__ == "__main__":
	main()



#!/usr/bin/python
#Program to parse the raw XML file to generate data that is specific to our
#specific task i.e. Search task #3: Video Tagging
#Author : Vikram Kamath
#	: Antti Partanen 
import xml.etree.ElementTree as ET

tree = ET.parse('corpus_part2.xml')
root = tree.getroot()
count = 0
countq1 = 0 
countq2 = 0 
countq3 = 0
for item in tree.findall('item'):
	count = count + 1
	print count
	if int(item.find('search_task_number').text) != 3:
		root.remove(item)
print count
count = 0

tree.write('data.xml')

#Sanity Check
tree = ET.parse('data.xml')
root = tree.getroot()
#for item in tree.iter('item'):
for item in tree.findall('item'):
	count = count + 1
	if item.find('query').text == "Content based video annotation":
	    countq1  = countq1 + 1
	if item.find('query').text == "Automatic or semiautomatic video tagging":
	    countq2  = countq2 + 1
	if item.find('query').text == "feature based Multimedia annotation":
		countq3 = countq3 + 1
print count 
print "Number of task docs " + str(count)
print "Number of 'Content based video annotation' Docs: " + str(countq1)
print "Number of 'Automatic or semiautomatic video tagging' " + str(countq2)
print "Number of 'feature based Multimedia annotation' docs " + str(countq3)
	

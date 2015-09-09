import re

class Parser():
	'''
	Get all unique genes from GeneNetwork candidate genes for AVM and compare them with
	candidate genes list gained from other resources. Write all unique genes to new file to 
	create new list of candidate genes.
	'''
	def __init__(self):
		self.readFile()

	def readFile(self):
		uniqueGenesFile = open("/Users/molgenis/Documents/graduation_project/uniqueGenesAVM.txt", "r")
		genesGeneNetworkFile = open("/Users/molgenis/Documents/graduation_project/candidateGenesAVMJuha.csv", "r")
		
		uniqueGenesList = []
		genesGeneNetworkList = []
		
		for line in uniqueGenesFile.readlines():
			line = line.replace('\n', "")
			line = line.replace('\t', "")	
			uniqueGenesList.append(line)
			
		for line in genesGeneNetworkFile.readlines():
			line = line.replace('\n', "")
			line = line.replace('\t', "")	
			genesGeneNetworkList.append(line)
		
		
		print("number of genes in uniqueGenesList: ",  len(uniqueGenesList))
		print("number of genes in genesGeneNetworkList: ", len(genesGeneNetworkList))
			
# 		make sure uniqueGenesList is unique and make genesGeneNetworkList unique
		uniqueGenesList = set(uniqueGenesList)
		genesGeneNetworkList = set(genesGeneNetworkList)
		
		print("number of genes in uniqueGenesList after making it unique: ", len(uniqueGenesList))
		print("number of genes in genesGeneNetworkList after making it unique: ", len(genesGeneNetworkList))
		
		newGenesList = []
		
		for gene in uniqueGenesList:
			newGenesList.append(gene)
		
		
		for gene in genesGeneNetworkList:
			newGenesList.append(gene)
			
			
		print("number of genes in newGenesList after combining both lists: ", len(newGenesList))
		
# 		make newGenesList unique
		newGenesList = set(newGenesList)
		
		print("number of genes in newGenesList after making it unique: ", len(newGenesList))
				
		newFile = open("/Users/molgenis/Documents/graduation_project/allUniqueCandiadteGenesAVM.txt", "w")
		
		for gene in newGenesList:
# 			print(gene)
			newFile.write(gene + "\n")

if __name__ == "__main__":
	p = Parser()

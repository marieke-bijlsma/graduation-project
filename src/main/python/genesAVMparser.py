import re

class Parser():
	'''
	Get all unique genes and candidate genes for AVM
	'''
	def __init__(self):
		self.readFile()

	def readFile(self):
		file = open("/Users/molgenis/Documents/genes_and_candidates_AOV.txt", "r")
		genesListAOV = []
		
		for line in file.readlines():
			line = line.replace('\n', "")
			line = line.replace('\t', "")	
			genesListAOV.append(line)
			
		genesListAOV = set(genesListAOV)
		newFile = open("/Users/molgenis/Documents/uniqueGenesAOV.txt", "w")
		
		for gene in genesListAOV:
# 			print(gene)
			newFile.write(gene + "\n")

if __name__ == "__main__":
	p = Parser()

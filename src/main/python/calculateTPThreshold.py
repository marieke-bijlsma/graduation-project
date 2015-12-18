class Parser():
	'''
	Calculate how many variants will pass and how many will be filtered according to several TP thresholds 
	and print results.
	'''
	def __init__(self):
		self.readFile()

	'''
	Read MV file and put transmission probabilities in a list.
	'''
	def readFile(self):
		file = "/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_allDiploid_Xadjusted.txt"
		
		transmissionProbabilities = []

		with open(file) as f:
			next(f)
			for line in f:
				if line != "":
					columns = line.split('\t')
					transmissionProbabilities.append(columns[4])
				else:
					next(line)	
					
		self.countVariants(transmissionProbabilities)

	'''
	Count number of variants that will be filtered and those that will pass.
	Put variants and counts for both cases in separate dictionaries.
	'''	
	def countVariants(self, transmissionProbabilities):	
		numberOfPassedVariants = {}
		numberOfFilteredVariants = {}
	
		for i in range(1,128): # max TP = 127
			countPassed = 0
			countFiltered = 0
			
			for tp in transmissionProbabilities:
				if int(tp) >= i:
					countPassed += 1	
					numberOfPassedVariants[i] = {i : countPassed}
				else:
					countFiltered += 1
					numberOfFilteredVariants[i] = {i : countFiltered}
					
		self.printCounts(numberOfPassedVariants, numberOfFilteredVariants)

	'''
	Print the results.
	'''						
	def printCounts(self, numberOfPassedVariants, numberOfFilteredVariants): 
		for count in numberOfPassedVariants:
			print(numberOfPassedVariants[count])
			
		for count in numberOfFilteredVariants:
			print(numberOfFilteredVariants[count])	

if __name__ == "__main__":
	p = Parser()
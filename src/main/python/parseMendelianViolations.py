class Parser():
	'''
	Calculate how many variants per member of a trio will be filtered according to the DP threshold.
	Also calculate how many variants do not meet the required TP threshold.
	'''
	def __init__(self):
		self.readFile()

	'''
	Read MV file and save the specific information in separate lists.
	'''
	def readFile(self):
		file = "/Users/molgenis/Documents/graduation_project/mendelianViolationFiles/mendelian_violation_allDiploid_Xadjusted.txt"
		
		mother_DP = []
		father_DP = []
		child_DP = []
		transmissionProbability = []

		with open(file) as f:
			next(f)
			for line in f:
				if line != "":
					columns = line.split('\t')
					
					mother_DP.append(columns[6])
					father_DP.append(columns[10])
					child_DP.append(columns[14])
					transmissionProbability.append(columns[4])
				else:
					next(line)
					
		self.countThresholdMother(mother_DP)
		self.countThresholdFather(father_DP)
		self.countThresholdChild(child_DP)
		self.countTP(transmissionProbability)

	'''
	Count number of variants of mother that meet and don't meet the specified DP threshold and print result.
	'''		
	def countThresholdMother(self, mother_DP):
		countGoodMother = 0
		countWrongMother = 0
			
		for element in mother_DP:
			if element == '':
				continue;
			elif int(element) >= 20:
				countGoodMother+=1
			else:
				countWrongMother+=1
	
		print("Total number of variants: ", len(mother_DP)+1)	# +1 because length starts at zero.
		print("\ncounts DP >= 20 mother: ", countGoodMother)
		print("counts DP < 20 mother: ", countWrongMother)

	'''
	Count number of variants of father that meet and don't meet the specified DP threshold and print result.
	'''		
	def countThresholdFather(self, father_DP):	
		countGoodFather = 0
		countWrongFather = 0
			
		for element in father_DP:
			if element == '':
				continue;
			elif int(element) >= 20:
				countGoodFather+=1
			else:
				countWrongFather+=1
		
		print("\ncounts DP >= 20 father: ", countGoodFather)
		print("counts DP < 20 father: ", countWrongFather)

	'''
	Count number of variants of child that meet and don't meet the specified DP threshold and print result.
	'''	
	def countThresholdChild(self, child_DP):		
		countGoodChild = 0
		countWrongChild = 0
			
		for element in child_DP:
			if element == '':
				continue;
			elif int(element) >= 20:
				countGoodChild+=1
			else:
				countWrongChild+=1
	
		print("\ncounts DP >= 20 child: ", countGoodChild)
		print("counts DP < 20 child: ", countWrongChild)

	'''
	Count number of variants of trios that meet and don't meet the specified TP threshold and print result.
	'''	
	def countTP(self, transmissionProbability):		
		countGoodTP = 0
		countWrongTP = 0
			
		for element in transmissionProbability:
			if element == '':
				continue;
			elif int(element) >= 20:
				countGoodTP+=1
			else:
				countWrongTP+=1
	
		print("\ncounts TP >= 20: ", countGoodTP)
		print("counts TP < 20: ", countWrongTP)


if __name__ == "__main__":
	p = Parser()

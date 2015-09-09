class Parser():
	'''
	Calculate how many variants will be filtered according to some threshold
	'''
	def __init__(self):
		self.readFile()

	def readFile(self):
		file = "/Users/molgenis/Documents/graduation_project/mendelian_violation.txt"
		
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
		
		countGoodMother = 0
		countWrongMother = 0
			
		for element in mother_DP:
			if int(element) >= 20:
				countGoodMother+=1
			else:
				countWrongMother+=1
	
		print("total: ", len(mother_DP))	# +1 because length starts at zero :)
		print("\ncounts DP>=10 mother: ", countGoodMother)
		print("counts DP<10 mother: ", countWrongMother)
		
		countGoodFather = 0
		countWrongFather = 0
			
		for element in father_DP:
			if int(element) >= 20:
				countGoodFather+=1
			else:
				countWrongFather+=1
	
		# print("total: ", len(father_DP))	
		print("\ncounts DP>=10 father: ", countGoodFather)
		print("counts DP<10 father: ", countWrongFather)
		
		countGoodChild = 0
		countWrongChild = 0
			
		for element in child_DP:
			if int(element) >= 20:
				countGoodChild+=1
			else:
				countWrongChild+=1
	
# 		print("total: ", len(child_DP))	
		print("\ncounts DP>=10 child: ", countGoodChild)
		print("counts DP<10 child: ", countWrongChild)
		
		countGoodTP = 0
		countWrongTP = 0
			
		for element in transmissionProbability:
			if int(element) >= 20:
				countGoodTP+=1
			else:
				countWrongTP+=1
	
# 		print("total: ", len(transmissionProbability))
		print("\ncounts TP>=20: ", countGoodTP)
		print("counts TP<20: ", countWrongTP)


if __name__ == "__main__":
	p = Parser()

class Parser():
	'''
	Calculate how many variants will be filtered according to several TP thresholds
	and what the best threshold would be
	'''
	def __init__(self):
		self.readFile()

	def readFile(self):
		file = "/Users/molgenis/Documents/graduation_project/mendelian_violation_adjusted_replicates.txt"
		
		transmissionProbability = []

		with open(file) as f:
			next(f)
			for line in f:
				if line != "":
					columns = line.split('\t')
					transmissionProbability.append(columns[4])
				else:
					next(line)
	
	#for every threshold, write to file!				

		countGoodTP = 0
		countWrongTP = 0
		
# 		tpThreshold = list(range(2,53))

		resultGood = []		
		resultWrong = []
		minTP = 2
		
		for tp in transmissionProbability:
			if int(tp) >= minTP:
				countGoodTP+=1	
								
			else:
				countWrongTP+=1
					
		resultGood.append(countGoodTP)
		resultWrong.append(countWrongTP)
			
				
		print(resultGood)
		print(resultWrong)


if __name__ == "__main__":
	p = Parser()
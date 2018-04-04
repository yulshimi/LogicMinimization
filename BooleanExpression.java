//Name: Phillip Jo
//Date: 11/20/2016
//Description: Implemented doTabulationMethod, doQuineMcCluskey, and other helper functions
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Raymond, Dylan
 *
 */
public class BooleanExpression 
{
	private List<Implicant> implicantList;
	private List<Implicant> dontcareList;
	private List<Long> mintermsNeededToCover;
	private int myNumVars;
	private long maxMinterm;
	public static final long maxVal = -1;
	public static final String alphabet = "abcdefghijklmnopqrstuvwxyz";
	
	private void initBooleanExpression(int numVars)
	{
		implicantList = new ArrayList<Implicant>();
		dontcareList = new ArrayList<Implicant>();
		myNumVars = numVars; 
		mintermsNeededToCover = new ArrayList<Long>();
	}
	
	public BooleanExpression(ArrayList<Long> minterms, ArrayList<Long> dontcares, int numVars)
	{
		initBooleanExpression(numVars);
		for (Long minterm : minterms)
		{
			implicantList.add(new Implicant(minterm, numVars));
			mintermsNeededToCover.add(minterm);
		}

		for (Long dontcare : dontcares)
		{
			dontcareList.add(new Implicant(dontcare, numVars, 0));
		}
		
		maxMinterm = maxMinterm(minterms) + 1;
	}
	
	public long maxMinterm(List<Long> mintermList)
	{
		long maxVal = mintermList.get(0);
		for(int i=1; i < mintermList.size(); ++i)
		{
			if(mintermList.get(i) > maxVal)
			{
				maxVal = mintermList.get(i);
			}
		}
		return maxVal;
	}
	
	public List<Implicant> getImplicantList()
	{
		return implicantList;
	}
	
	public Implicant mergeTwoImplicants(Implicant imp1, Implicant imp2)
	{
		//System.out.println("Merge happens");
		long newMSB = imp1.getMSB() | imp2.getMSB();
		long newLSB = imp1.getLSB() | imp2.getLSB();
		
		List<Long> tempList = new ArrayList<Long>();
		
		if(imp1.getMinterms() != null)
		{
			tempList.addAll(imp1.getMinterms());
		}
		
		if(imp2.getMinterms() != null)
		{
			tempList.addAll(imp2.getMinterms());
		}
		
		for(int i=0; i < tempList.size(); ++i) // getting rid of duplicate items
		{
			for(int j=i+1; j < tempList.size(); ++j)
			{
				if(tempList.get(i) == tempList.get(j))
				{
					//System.out.println("Remove happens");
					tempList.remove(j);
					--j;
				}
			}
		}
		
		List<Long> tempDontCare = new ArrayList<Long>();
		if(imp1.getDontCares() != null)
		{
			tempDontCare.addAll(imp1.getDontCares());
		}
		
		if(imp2.getDontCares() != null)
		{
			tempDontCare.addAll(imp2.getDontCares());
		}
		
		for(int i=0; i < tempDontCare.size(); ++i) // getting rid of duplicate items
		{
			for(int j=i+1; j < tempDontCare.size(); ++j)
			{
				if(tempDontCare.get(i) == tempDontCare.get(j))
				{
					tempDontCare.remove(j);
					--j;
				}
			}
		}
		
		Implicant newImplicant = new Implicant(newMSB, newLSB, imp1.getNumVars(), tempList, tempDontCare);
		return newImplicant;
	}
	
	public List<Implicant> removeRedundancy(List<Implicant> impList)
	{
		for(int i=0; i < impList.size(); ++i)
		{
			for(int j=i+1; j < impList.size(); ++j)
			{
				if(impList.get(i).equals(impList.get(j)))
				{
					impList.remove(j);
					--j;
				}
			}
		}
		return impList;
	}
	public boolean differBySingleVariable(Implicant imp1, Implicant imp2)
	{
		long newMSB;
		long newLSB;
		
		newMSB = imp1.getMSB() ^ imp2.getMSB();
		newLSB = imp1.getLSB() ^ imp2.getLSB();
		
		return ((Long.bitCount(newMSB) == 1) && (Long.bitCount(newLSB)) == 1 && (newMSB == newLSB));
	}
	
	public long groupNumber(Implicant imp)
	{
		return Long.bitCount((imp.getMSB() ^ imp.getLSB()) & imp.getLSB());
	}
	/**
	 * Update the implicantList to contain only prime implicants.
	 * The new implicantList will be used for Quine-McCluskey and the Branch and Bound algorithm
	 */
	public void doTabulationMethod()
	{
		List<Implicant> theTempList; 
		List<Implicant> theFinalList;
		List<Implicant> theRemovingList;
		
		int numOfMerge = 0;
		
		boolean isItTheFirst = true;
		
		theTempList = new ArrayList<Implicant>();
		theFinalList = new ArrayList<Implicant>();
		theRemovingList = new ArrayList<Implicant>();
		
		do
		{
			numOfMerge = 0;
			theRemovingList.addAll(implicantList); // theRemovingList is for sending the leftovers to the final list
			if(isItTheFirst)
			{
				for(int i=0; i < implicantList.size(); ++i)
				{
					for(int j=i+1; j < implicantList.size(); ++j)
					{
						long gapOfGroupNum = 0;
						gapOfGroupNum = groupNumber(implicantList.get(i)) - groupNumber(implicantList.get(j));
						if(gapOfGroupNum == -1 || gapOfGroupNum == 1)
						{
							if(differBySingleVariable(implicantList.get(i), implicantList.get(j)))
							{
							    theTempList.add(mergeTwoImplicants(implicantList.get(i), implicantList.get(j)));
								theRemovingList.remove(implicantList.get(i));
								theRemovingList.remove(implicantList.get(j));
								++numOfMerge;
							}
						}
					}
				}
				
				//From this point, it starts to take care of dontCareList
				for(int i=0; i < implicantList.size(); ++i)
				{
					for(int j=0; j < dontcareList.size(); ++j)
					{
						long gapOfGroupNum = 0;
						gapOfGroupNum = groupNumber(implicantList.get(i)) - groupNumber(dontcareList.get(j)); 
						if(gapOfGroupNum == -1 || gapOfGroupNum == 1)
						{
							if(differBySingleVariable(implicantList.get(i), dontcareList.get(j)))
							{
								theTempList.add(mergeTwoImplicants(implicantList.get(i), dontcareList.get(j)));
								theRemovingList.remove(implicantList.get(i));
								++numOfMerge;
							}
						}
					}
				}
				
				if(numOfMerge > 0) // be prepared for the next turn
				{
					implicantList.clear();
					implicantList.addAll(theTempList);
					//System.out.println(implicantList.size());
					implicantList = removeRedundancy(implicantList);
					//System.out.println(implicantList.size());
					theTempList.clear();
					if(theRemovingList.size() > 0)
					{
						theFinalList.addAll(theRemovingList);
					}
					theRemovingList.clear();
				}
				else // This is the end
				{
					implicantList.addAll(theFinalList);
				}
				isItTheFirst = false; // the end of the first trial
			}
			else // If it is not the first trial
			{
				for(int i=0; i < implicantList.size(); ++i)
				{
					for(int j=i+1; j < implicantList.size(); ++j)
					{
						long gapOfGroupNum = 0;
						gapOfGroupNum = groupNumber(implicantList.get(i)) - groupNumber(implicantList.get(j)); //fix it later
						if(gapOfGroupNum == -1 || gapOfGroupNum == 1)
						{
							if(differBySingleVariable(implicantList.get(i), implicantList.get(j)))
							{
								theTempList.add(mergeTwoImplicants(implicantList.get(i), implicantList.get(j)));
								theRemovingList.remove(implicantList.get(i));
								theRemovingList.remove(implicantList.get(j));
								++numOfMerge;
							}
						}
					}
				}
				
				if(numOfMerge > 0) // be prepare for the next turn
				{
					implicantList.clear();
					implicantList.addAll(theTempList);
					implicantList = removeRedundancy(implicantList);
					theTempList.clear();
					if(theRemovingList.size() > 0)
					{
						theFinalList.addAll(theRemovingList);
					}
					theRemovingList.clear();
				}
				else // This is the end
				{
					implicantList.addAll(theFinalList);
				}
			}
			
			
			
		}while(numOfMerge != 0);
	}
	
	/**
	 * Creates the final implicant cover using Quine McCluskey and Branch and Bound
	 */
	public void createFinalCover()
	{
		implicantList = (ArrayList<Implicant>)doQuineMcCluskey(implicantList, mintermsNeededToCover); 
	}
	
	/** 
	 * This method is used for the recursive calling of Quine-McCluskey 
	 * which is needed for the Branch and Bound algorithm
	 * @param implicants the implicants that are left in the chart
	 * @param minterms the minterms that are left in the chart
	 * @return a minimal prime implicant list for the from the implicants and minterms provided
	 */
	private List<Implicant> doQuineMcCluskey(List<Implicant> implicants, List<Long> minterms)
	{
		//System.out.println("size of implicants: " + implicants.size());
		//System.out.println("size of minterm: " + minterms.size());
		
		if(minterms.size() == 0) 
		{
			List<Implicant> theFinalCover = new ArrayList<Implicant>();
			return theFinalCover;
		}
		
		List<BitVector> rowBitVectors;
		List<BitVector> columnBitVectors;
		
		rowBitVectors = new ArrayList<BitVector>();
		columnBitVectors = new ArrayList<BitVector>();
		
		rowBitVectors = rowArrayGenerator(implicants);
		columnBitVectors = columnArrayGenerator(implicants);
		
		List<Implicant> theFinalCover;
		theFinalCover = new ArrayList<Implicant>();
		
		List<Implicant> theTempCover;
		theTempCover = new ArrayList<Implicant>();
		
		boolean isThereEssential = false;
		boolean isThereRowDominance = false;
		boolean isThereColumnDominance = false;
		
		int dominatingIndex = 0;
		int dominatedIndex = 0;
		
		for(int i=0; i < columnBitVectors.size(); ++i) // Searching for the essential prime implicant
		{
			if(columnBitVectors.get(i).getCardinality() == 1)  //which means it is the essential prime implicant
			{
				for(int j=0; j < columnBitVectors.get(i).getSize(); ++j)
				{
					if(columnBitVectors.get(i).exists(j)) //If j th bit is turned on
					{
						theFinalCover.add(implicants.get(j));
						List<Long> tempMinterms;
						tempMinterms = new ArrayList<Long>();
						tempMinterms.addAll(implicants.get(j).getMinterms());
						for(int k=0; k < tempMinterms.size(); ++k)
						{
							minterms.remove(tempMinterms.get(k));
						}
						implicants = pullOutCoveredMinterms(implicants, implicants.get(j));
						implicants.remove(j);
					
						break;
					}
				}
				theTempCover = doQuineMcCluskey(implicants, minterms);
				theFinalCover.addAll(theTempCover);
				isThereEssential = true;
				break;
			}
		}
		
		if(isThereEssential == false) // Row and column Dominance
		{
			for(int i=0; i < rowBitVectors.size(); ++i)
			{
				for(int j=0; j < rowBitVectors.size(); ++j)
				{
					if(j != i)
					{
						if(rowBitVectors.get(i).equals(rowBitVectors.get(i).union(rowBitVectors.get(j))))
						{
							for(int k=0; k < (int)maxMinterm; ++k)
							{
								rowBitVectors.get(j).unset(k);
							}
						
							List<Long> tempMinterm = new ArrayList<Long>();
							tempMinterm.addAll(implicants.get(j).getMinterms());
						
							for(int k=0; k < columnBitVectors.size(); ++k)
							{
								columnBitVectors.get(k).unset(j);
							}
							dominatingIndex = i;
							dominatedIndex = j;
							isThereRowDominance = true;
						}
					}
				}
				if(isThereRowDominance)
				{
					break;
				}
			}
			
			if(isThereRowDominance) // searching for Column Dominance
			{
				List<Long> dominatingMinterms;
				List<Long> dominatedMinterms;
				
				dominatingMinterms = new ArrayList<Long>();
				dominatingMinterms.addAll(implicants.get(dominatingIndex).getMinterms());
				
				dominatedMinterms = new ArrayList<Long>();
				dominatedMinterms.addAll(implicants.get(dominatedIndex).getMinterms());
				
				for(int i=0; i < dominatingMinterms.size(); ++i)
				{
					for(int j=0; j < dominatedMinterms.size(); ++j)
					{
						if(dominatingMinterms.get(i) != dominatedMinterms.get(j))
						{
							long superIndex;
							long subIndex;
							
							superIndex = dominatingMinterms.get(i);
							subIndex = dominatedMinterms.get(j);
							
							BitVector tempBitVector;
							tempBitVector = new BitVector(implicants.size());
							
							tempBitVector = columnBitVectors.get((int)superIndex).union(columnBitVectors.get((int)subIndex));
							if(columnBitVectors.get((int)superIndex).equals(tempBitVector))
							{
								minterms.remove(superIndex);
								implicants = pullOutMinterm(implicants, (Long)superIndex);
								isThereColumnDominance = true;
							}
						}
					}
				}
				implicants.remove(dominatedIndex);
				theTempCover = doQuineMcCluskey(implicants, minterms);
				theFinalCover.addAll(theTempCover);
			}
		}
		
		if(isThereEssential == false && isThereRowDominance == false && isThereColumnDominance == false) // cyclic core
		{
			List<Implicant> copiedImplicants = new ArrayList<Implicant>(implicants);
			List<Implicant> theFinalCover2 = new ArrayList<Implicant>();
			List<Implicant> theTempCover2 = new ArrayList<Implicant>();
			
			implicants.remove(0);
			theTempCover = doQuineMcCluskey(implicants, minterms);
			theFinalCover.addAll(theTempCover);
			
			theFinalCover2.add(copiedImplicants.get(0));
			List<Long> tempMinterms;
			tempMinterms = new ArrayList<Long>();
			tempMinterms.addAll(copiedImplicants.get(0).getMinterms());
			for(int k=0; k < tempMinterms.size(); ++k)
			{
				minterms.remove(tempMinterms.get(k));
			}
			copiedImplicants = pullOutCoveredMinterms(copiedImplicants, copiedImplicants.get(0));
			copiedImplicants.remove(0);
			theTempCover2 = doQuineMcCluskey(copiedImplicants, minterms);
			theFinalCover2.addAll(theTempCover2);
			
			if(theFinalCover2.size() >= theFinalCover.size())
			{
				return theFinalCover;
			}
			else
			{
				return theFinalCover2;
			}
		}
		return theFinalCover;
	}
	
	public List<BitVector> rowArrayGenerator(List<Implicant> impList)
	{
		List<BitVector> rowBitVector;
		rowBitVector = new ArrayList<BitVector>();
		for(int i=0; i < impList.size(); ++i)
		{
			List<Long> tempMinterms;
			tempMinterms = new ArrayList<Long>();
			tempMinterms.addAll(impList.get(i).getMinterms());
			
			BitVector tempBitVector;
			tempBitVector = new BitVector((int)maxMinterm);
			for(int j=0; j < tempMinterms.size(); ++j)
			{
				long setPoint = tempMinterms.get(j);
				tempBitVector.set((int)setPoint);
			}
			rowBitVector.add(tempBitVector);
		}
		return rowBitVector; 
	}
	
	public List<BitVector> columnArrayGenerator(List<Implicant> impList)
	{
		List<BitVector> columnBitVector;
		columnBitVector = new ArrayList<BitVector>();
		for(int i=0; i < (int)maxMinterm; ++i)
		{
			BitVector tempBitVector;
			tempBitVector = new BitVector(impList.size());
			columnBitVector.add(tempBitVector);
		}
		for(int i=0; i < impList.size(); ++i)
		{
			List<Long> tempMinterms;
			tempMinterms = new ArrayList<Long>();
			tempMinterms.addAll(impList.get(i).getMinterms());
			for(int j=0; j < tempMinterms.size(); ++j)
			{
				long index;
				index = tempMinterms.get(j);
				columnBitVector.get((int)index).set(i);
			}
			
		}
		return columnBitVector;
	}
	
	public List<Implicant> pullOutCoveredMinterms(List<Implicant> impList, Implicant imp)
	{
		List<Long> tempMinterms;
		tempMinterms = new ArrayList<Long>();
		tempMinterms.addAll(imp.getMinterms());
		
		for(int i=0; i < tempMinterms.size(); ++i)
		{
			for(int j=0; j < impList.size(); ++j)
			{
				impList.get(j).removeMinterm(tempMinterms.get(i));
			}
		}
		return impList;
	}
	
	public List<Implicant> pullOutMinterm(List<Implicant> impList, Long minterm)
	{
		for(int i=0; i < impList.size(); ++i)
		{
			impList.get(i).removeMinterm(minterm);
		}
		return impList;
	}
	/** 
	 * Generates a verilog file with the same name as the fileName. Do not change
	 * the input and output port names, as we will need these for testing. The 
	 * interior of the module, however, can be implemented as you see fit.
	 * 
	 * @param fileName
	 * @return true if the file was created successfuly
	 *  
	 */
	public boolean genVerilog(String fileName)
	{
		try {
			PrintWriter outputStream = new PrintWriter(new FileWriter(fileName + ".v"));
			outputStream.println("module " + fileName + "(");
			for (int i = 0; i < myNumVars; i++)
			{
				outputStream.println("input " + alphabet.charAt(i) + ",");
			}
			outputStream.println("output out");
			outputStream.println(");");
			
			//Code will be given after the late deadline
			outputStream.write("\tassign out = ");
			for (int i = 0; i < implicantList.size(); i++)
			{
				outputStream.write(implicantList.get(i).getVerilogExpression());
				if (i < implicantList.size()-1)
					outputStream.write("|");
			}
			outputStream.println(";");
			
			outputStream.println("endmodule");
			outputStream.close();
			return true;
		} catch (Exception e){
			return false;
		}
		
	}
	
}

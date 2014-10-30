package navis.transportation.support;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static navis.transportation.support.BinarySearch.binarySearch;

public class LongIntBTreeMap implements ItfBtreeMap {
	private Logger logger = LoggerFactory.getLogger(getClass());
    private final int noNumberValue = -1;
    private long size;
    private int maxLeafEntries;
    private int initLeafSize;
    private int splitIndex;
    private float factor;
    private int height;
    private BTreeEntry root;
    
    public LongIntBTreeMap( int maxLeafEntries )
    {
        this.maxLeafEntries = maxLeafEntries;
        if (maxLeafEntries < 1)
        {
            throw new IllegalArgumentException("illegal maxLeafEntries:" + maxLeafEntries);
        }
        if (maxLeafEntries % 2 == 0)
        {
            maxLeafEntries++;
        }

        splitIndex = maxLeafEntries / 2;
        factor = 1.7f;
        initLeafSize = maxLeafEntries / 10;
        init();
    }
    
    void init() {
    	size = 0;
    	height = 1;
    	root = new BTreeEntry(initLeafSize, true);
    }
    

	@Override
	public int put(long key, int value) {
		if (key == noNumberValue)
			 throw new IllegalArgumentException("Illegal key " + key);
		ReturnValue rv = root.put(key, value);
	    if (rv.tree != null) {
            height++;
            root = rv.tree;
        }
        if (rv.oldValue == noNumberValue) {
            // successfully inserted
            size++;
            if (size % 1000000 == 0)
                optimize();
        }
        return rv.oldValue;
	}
	
	public int getHeight() {
		return height;
	}
	
	public long getSize() {
		return size;
	}

	/**
	 * @return value of specified node
	 */
	@Override
	public int get(long key) {
		return root.get(key);
	}


	@Override
	public void optimize() {
		root.compact();		
	}
	
	public String toString() {
		return "Size: " + getSize() + ", height: " + getHeight();
	}
	
	
	static class ReturnValue {
		   int oldValue;
		   BTreeEntry tree;
		   
		   public ReturnValue() {
			   
		   }
		   public ReturnValue(int oldValue) {
			   this.oldValue = oldValue;
		   }
    }
	
	
	class BTreeEntry {
	    int entrySize; 
	    long keys[];
	    int values[];
        BTreeEntry children[];
        boolean isLeaf;
	    
		public BTreeEntry( int tmpSize, boolean leaf ) {
			this.isLeaf = leaf;
			keys = new long[tmpSize];
			values = new int[tmpSize];
			if ( !isLeaf ) { 
				children = new BTreeEntry[tmpSize + 1];
			}
		}
		
		 /**
         * @return the old value which was associated with the specified key or if no update it
         * returns noNumberValue
         */
		ReturnValue put( long key, int newValue ) {
			int index = binarySearch(keys, 0, entrySize, key);
		
			//old value = new value 
			if (index >= 0) { 
				int oldValue = values[index];
				values[index] = newValue;
				return new ReturnValue(oldValue);
			}
			
			//If key don't exist on keys array
			index = ~index;
			ReturnValue downTreeRV; 
			if (isLeaf || children[index] == null) {
				downTreeRV = new ReturnValue(noNumberValue);
				downTreeRV.tree = checkSplitEntry();
				if (downTreeRV.tree == null) {
					insertKeyValue(index, key, newValue);
				} else {
					if (index <= splitIndex)
                    {
                        downTreeRV.tree.children[0].insertKeyValue(index, key, newValue);
                    } else
                    {
                        downTreeRV.tree.children[1].insertKeyValue(index - splitIndex - 1, key, newValue);
                    }
				}
				return downTreeRV;
			}

			downTreeRV = children[index].put(key, newValue); 
            if (downTreeRV.oldValue != noNumberValue)
            {
                return downTreeRV;
            }
            
            // Cay con qua nhieu phan tu => bi cat thanh mot cay moi. ta chuyen dinh cay con moi len ngang hang voi cha cua no
            if (downTreeRV.tree != null) {
            	BTreeEntry returnTree, downTree = returnTree = checkSplitEntry();
            	if (downTree == null)
                {
                    insertTree(index, downTreeRV.tree); 
                } else {
                	if (index <= splitIndex)
                    {
                        downTree.children[0].insertTree(index, downTreeRV.tree);
                    } else
                    {
                        downTree.children[1].insertTree(index - splitIndex - 1, downTreeRV.tree);
                    }
                }
           	  downTreeRV.tree = returnTree;
            } 
			
            return downTreeRV;
		}
		
		//@Param: index la chi so phan tu trong mang keys can them gia tri
		void insertKeyValue(int index, long key, int newValue) {
			ensureSize(entrySize + 1); //vi keys chi duoc khoi tao truoc 20 phan tu, nen dung cai nay de mo rong size cho keys, neu entry > kich thuoc keys
			int count = entrySize - index;

			if (count > 0) { //gia tri o keys[index] la da co va khac gia tri can them vao => can chen vao vi tri moi sau gia tri bi trung
				System.arraycopy(keys, index, keys, index + 1, count);
                System.arraycopy(values, index, values, index + 1, count);
                if (!isLeaf)
                {
                    System.arraycopy(children, index + 1, children, index + 2, count);
                }
			}
			keys[index] = key;
			values[index] = newValue;
			entrySize++;
		}
		
		/**
		 * @return null if nothing to do or a new sub tree if this tree capacity is no longer  sufficient.
		 * Neu entrySize > maxLeafEntries thi khoi tao cay con moi
		 */
		BTreeEntry checkSplitEntry() {
			if (entrySize < maxLeafEntries)
				return null;
			 //System.out.println(getClass().getName() + " : Xu ly khi entrySize >  maxLeafEntries");
			 //Xu ly khi entrySize > maxLeafEntris. tao mot cay con moi
			
			//CHia nho mang ban dau thanh hai mang con trai, phai
			// right child: copy from this
			 int count = entrySize - splitIndex - 1;
			 BTreeEntry newRightChild = new BTreeEntry(Math.max(initLeafSize, count), isLeaf);
			 copy(this, newRightChild, splitIndex + 1, count);
			
			 //left child: copy from this
			 BTreeEntry newLeftChild = new BTreeEntry(Math.max(initLeafSize, splitIndex), isLeaf);
			 copy(this, newLeftChild, 0, splitIndex);
			 
			// new tree pointing to left + right tree only
			 BTreeEntry newTree = new BTreeEntry(1, false);
			 newTree.entrySize = 1;
			 newTree.keys[0] = this.keys[splitIndex];
             newTree.values[0] = this.values[splitIndex];
             newTree.children[0] = newLeftChild;
             newTree.children[1] = newRightChild;
			 return newTree;
		}
		
		void copy(BTreeEntry fromChild, BTreeEntry toChild, int from, int count) {
			System.arraycopy(fromChild.keys, from, toChild.keys, 0, count);
			System.arraycopy(fromChild.values, from, toChild.values, 0, count);
            if (!fromChild.isLeaf)
            {
                System.arraycopy(fromChild.children, from, toChild.children, 0, count + 1);
            }
            toChild.entrySize = count;
		}
		
		int get(long key) {
			int index = binarySearch(keys, 0, entrySize, key); //search key on Keys
			if (index >= 0)
				return values[index];
			index = ~index;
			if (isLeaf || children[index] == null) {
				return noNumberValue;
			}
			
			return children[index].get(key);
		}
		
		void ensureSize(int size) {
			if (size <= keys.length) {
				return;
			}
			int newSize = Math.min(maxLeafEntries, Math.max(size + 1, Math.round(size * factor))); 
			keys = Arrays.copyOf(keys, newSize);
			values = Arrays.copyOf(values, newSize);
			if (!isLeaf) {
				 children = Arrays.copyOf(children, newSize + 1); 
			}
		}
		
		void insertTree( int index, BTreeEntry tree ) {
			insertKeyValue(index, tree.keys[0], tree.values[0]);
	
            if (!isLeaf)
            {
                // overwrite children
                children[index] = tree.children[0];
                // set
                children[index + 1] = tree.children[1];
            } 
		}
	   
		void compact() {
			int offset = 1;
			if (entrySize + offset < keys.length) { 
				keys = Arrays.copyOf(keys, entrySize);
                values = Arrays.copyOf(values, entrySize);
                if (!isLeaf)
                {
                    children = Arrays.copyOf(children, entrySize + 1);
                }
			}
			
			 if (!isLeaf)
	            {
	                for (int i = 0; i < children.length; i++)
	                {
	                    if (children[i] != null)
	                    {
	                        children[i].compact();
	                    }
	                }
	            }
		}
   }
	
	
   
}

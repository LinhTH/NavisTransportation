package research.linhth.Storage;

/**
 * Defines how a DataAccess object is created.
 * @author Peter Karich
 */
public class DAType {
	
	/**
	 * The DA object is hold entirely in-memory. It will read load disc and flush to it if they
	 * equivalent methods are called. See RAMDataAccess
	 */
	public static final DAType RAM_STORE = new DAType(MemRef.HEAP, true, false);
	
	
	public enum MemRef {
		HEAP, MMAP, UNSAFE
	};
	private final MemRef memRef;
	private final boolean storing;
	private final boolean integ;
	private final boolean synched;
	
	public DAType( DAType type, boolean synched ) {
		this( type.getMemRef(), type.isStoring(), type.isInteg(), synched );
		
		if (!synched)
            throw new IllegalStateException("constructor can only be used with synched=true");
        if (type.isSynched())
            throw new IllegalStateException("something went wrong as DataAccess object is already synched!?");
	}
	
	public DAType( MemRef memRef, boolean storing, boolean integ, boolean synched ) {
		this.memRef  = memRef;
		this.storing = storing;
		this.integ = integ;
		this.synched = synched;
	}
	
	public DAType( MemRef memRef, boolean store, boolean integ ) {
		this(memRef, store, integ, false);
	}
	
	
	/*
	 *  Memory mapped or purely in memory? default is HEAP
	 */
	MemRef getMemRef() {
		return memRef;
	}
	
	/**
	 * @return true if data resides in the JVM heap
	 */
	public boolean isInMemory() {
		return memRef == MemRef.HEAP;
	}
	
	public boolean isMMap() {
		return memRef == MemRef.MMAP;
	}
	
	 /**
     * Temporary data or store (with loading and storing)? default is false
     */
	public boolean isStoring() {
		return storing;
	}
	
	/**
	 * Optimized for integer values? default is false
	 */
	public boolean isInteg() {
		return integ;
	}
	
	 /**
	  * ?????????
     * Synchronized access wrapper around DataAccess objects? default is false and so an in-memory
     * DataAccess object is only read-thread safe where a memory mapped one is not even
     * read-threadsafe!
     */
	public boolean isSynched() {
		return synched;
	}
}


















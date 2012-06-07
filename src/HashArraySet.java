
import java.util.*;

public class HashArraySet<E> implements Set<E> {
	final Map<E,Integer> inds;
	final List<E> elts;
	
	public HashArraySet() {
		inds = new HashMap<E,Integer>();
		elts = new ArrayList<E>();
	}
	
	public HashArraySet(Collection<E> toCopy) {
		// this needs some work...
		inds = new HashMap<E,Integer>(toCopy.size()*4/3+6);
		elts = new ArrayList<E>(toCopy.size()+5);
		addAll(toCopy);
	}
	
	public boolean add(E e) {
		if(inds.containsKey(e)) return false;
		inds.put(e,inds.size());
		elts.add(e);
		return true;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = false;
		for(E elt : c) result |= add(elt);
		return result;
	}
	
	@Override
	public void clear() {
		inds.clear();
		elts.clear();
	}
	
	@Override
	public boolean contains(Object o) {
		return inds.containsKey(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c) {
		return inds.keySet().containsAll(c);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Set)) return false;
		Set other = (Set)obj;
		if(this.size()!=other.size()) return false;
		for(Object o : other) {
			if(!this.contains(o)) return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return inds.keySet().hashCode();
	}
	
	@Override
	public boolean isEmpty() {
		return inds.isEmpty();
	}
	
	@Override
	public Iterator<E> iterator() {
		return elts.iterator();
	}
	
	@Override
	public boolean remove(Object o) {
		Integer ind = inds.remove(o);
		if(ind==null) return false;
		if(ind == elts.size()-1) {
			elts.remove(elts.size()-1);
		} else {
			Collections.swap(elts, ind, elts.size()-1);
			elts.remove(elts.size()-1);
			inds.put(elts.get(ind), ind);
		}
		return true;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		for(Object o : c) changed |= remove(o);
		return changed;
	}
	
	@Override
	public boolean retainAll(Collection<?> coll) {
		// this needs some work...
		boolean changed = elts.retainAll(coll);
		if(!changed) return false;
		inds.clear();
		for(int c=0;c<elts.size();c++) {
			inds.put(elts.get(c), c);
		}
		return true;
	}
	
	@Override
	public int size() {
		return inds.size();
	}
	
	@Override
	public Object[] toArray() {
		return elts.toArray();
	}
	
	public <T extends Object> T[] toArray(T[] a) {
		return elts.toArray(a);
	}
	
	public E randomElt(Random generator) {
		return elts.get(generator.nextInt(elts.size()));
	}
	
	@Override
	public String toString() {
		return elts.toString();
	}
	
	/**public static void main(String[] args) {
		Random generator = new Random();
		HashArraySet<Double> set = new HashArraySet<Double>();
		for(int c=0;c<5000;c++) {
			set.add(c+1.5);
		}
		for(int c=4800;c>=1;c--) {
			set.remove(c+1.5);
		}
		for(int c=4810;c<5000;c++) {
			set.remove(c+1.5);
		}
		set.add(5.0);
		System.out.println(set);
		System.out.println(set.randomElt(generator));
		System.out.println(set.randomElt(generator));
		System.out.println(set.randomElt(generator));
		System.out.println(set.randomElt(generator));
		System.out.println(set.randomElt(generator));
		System.out.println(set.remove(1.0));
		System.out.println(set.remove(4805.5));
		
		System.out.println(new HashArraySet<Integer>(Arrays.asList(new Integer[]{2,7,6,4,9,5,5,5,2,2,2,2,2,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49})));
	}**/
}

package Clustering;

public class Triplet<T, U, V> {

    private T first;
    private U second;
    private V third;

    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() { return first;}
    public U getSecond() { return second;}
    public V getThird() { return third;}
    
    public void setFirst(T f) 
    { 
    	first = f;
    }
    
    public void setSecond(U s) 
    { 
    	second = s;
    }
    
    public void setThird(V t) 
    { 
    	third = t;
    }
    
    public String toString()
    {
    	String s = "first is: " + first.toString() + "\n"
    			 + "second is: " + second.toString() + "\n"
    			+ "third is: " + third.toString();
		return s;
    	
    }
}
